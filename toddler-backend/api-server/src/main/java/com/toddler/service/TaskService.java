package com.toddler.service;

import com.toddler.controller.payload.CreateTaskPayload;
import com.toddler.controller.payload.UpdateTaskPayload;
import com.toddler.controller.payload.UpdateTaskStatusPayload;
import com.toddler.dto.CommentDto;
import com.toddler.dto.ProjectMemberDto;
import com.toddler.dto.StatusHistoryDto;
import com.toddler.dto.TaskDetailsDto;
import com.toddler.dto.TaskDetailsTimeLogDto;
import com.toddler.dto.TaskDto;
import com.toddler.dto.TaskReportDto;
import com.toddler.dto.TaskStatusHistoryDto;
import com.toddler.dto.TimeLogDto;
import com.toddler.entity.ProjectEntity;
import com.toddler.entity.TaskCommentEntity;
import com.toddler.entity.TaskEntity;
import com.toddler.entity.TaskStatusHistoryEntity;
import com.toddler.entity.TaskTimeLogsEntity;
import com.toddler.entity.UserEntity;
import com.toddler.exception.NotFoundException;
import com.toddler.exception.UnauthorizedException;
import com.toddler.mapper.TaskMapper;
import com.toddler.mapper.UserMapper;
import com.toddler.repository.ProjectMemberRepository;
import com.toddler.repository.ProjectsRepository;
import com.toddler.repository.TaskCommentRepository;
import com.toddler.repository.TaskRepository;
import com.toddler.repository.TaskStatusHistoryRepository;
import com.toddler.repository.TaskTimeLogRepository;
import com.toddler.repository.UserRepository;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectsRepository projectsRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final TaskCommentService taskCommentService;
    private final TaskStatusHistoryService taskStatusHistoryService;
    private final TaskTimeLogRepository taskTimeLogRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final NotificationService notificationService;
    private final ProjectService projectService;

    @Transactional
    public TaskDto createTask(UUID projectId, CreateTaskPayload payload, HttpServletRequest request) {
        ProjectEntity project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectId));

        UUID userUUID = extractUserUUIDFromRequest(request);
        validateProjectMember(project.getId(), userUUID);

        if (payload.executorId() != null) {
            userRepository.findById(payload.executorId())
                    .orElseThrow(() -> new NotFoundException("User not found with ID: " + payload.executorId()));
            validateProjectMember(project.getId(), payload.executorId());
        }

        TaskEntity task = TaskEntity.builder()
                .id(UUID.randomUUID())
                .title(payload.name())
                .description(payload.description())
                .status(payload.status())
                .priority(payload.priority())
                .projectId(projectId)
                .assignedTo(payload.executorId())
                .createdAt(LocalDateTime.now())
                .dueDate(payload.deadline())
                .build();

        taskRepository.save(task);

        TaskDto taskDto = taskMapper.fromEntityToDto(task);
        if (task.getAssignedTo() != null) {
            userRepository.findById(task.getAssignedTo())
                    .map(userMapper::fromEntityToDto)
                    .ifPresent(taskDto::setExecutor);
        }

        return taskDto;
    }

    @Transactional(readOnly = true)
    public long getActiveTimerElapsedTime(UUID taskId, HttpServletRequest request) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        UUID userUUID = extractUserUUIDFromRequest(request);
        validateProjectMember(task.getProjectId(), userUUID);

        TaskTimeLogsEntity timeLog = taskTimeLogRepository.findByTaskIdAndUserIdAndEndTimeIsNull(taskId, userUUID)
                .orElseThrow(() -> new NotFoundException("No active timer found for this task and user"));

        return ChronoUnit.SECONDS.between(timeLog.getStartTime(), LocalDateTime.now());
    }

    @Transactional
    public TaskDto updateTaskStatus(UUID taskId, UpdateTaskStatusPayload payload, HttpServletRequest request) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        UUID userUUID = extractUserUUIDFromRequest(request);
        validateProjectMember(task.getProjectId(), userUUID);

        String oldStatus = task.getStatus();
        String newStatus = payload.status();
        LocalDateTime now = LocalDateTime.now();

        taskStatusHistoryRepository.findTopByTaskIdAndStatusOrderByStatusEnteredAtDesc(taskId, oldStatus)
                .ifPresent(lastStatusHistory -> {
                    lastStatusHistory.setStatusLeftAt(now);
                    taskStatusHistoryRepository.save(lastStatusHistory);
                });

        task.setStatus(newStatus);
        taskRepository.save(task);

        TaskStatusHistoryEntity newHistoryEntry = TaskStatusHistoryEntity.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .status(newStatus)
                .statusEnteredAt(now)
                .build();
        taskStatusHistoryRepository.save(newHistoryEntry);

        TaskDto taskDto = taskMapper.fromEntityToDto(task);
        if (task.getAssignedTo() != null) {
            String message = String.format("Статус задачи '%s' изменен на '%s'", task.getTitle(), newStatus);
            notificationService.createNotification(task.getAssignedTo(), message);
        }

        if (task.getAssignedTo() != null) {
            userRepository.findById(task.getAssignedTo())
                    .map(userMapper::fromEntityToDto)
                    .ifPresent(taskDto::setExecutor);
        }

        return taskDto;
    }

    private void validateProjectMember(UUID projectId, UUID userId) {
        projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new UnauthorizedException("User is not a member of the project"));
    }

    private UUID extractUserUUIDFromRequest(HttpServletRequest request) {
        String token = Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .orElseThrow(() -> new UnauthorizedException("Invalid or missing JWT token"));

        String email = jwtService.getEmailFromToken(token);
        return userRepository.findByEmail(email)
                .map(UserEntity::getId)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public TaskDetailsDto getTaskDetails(UUID taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        TaskDetailsDto taskDto = taskMapper.fromEntityToDetailsDto(task);
        if (task.getAssignedTo() != null) {
            userRepository.findById(task.getAssignedTo())
                    .map(userMapper::fromEntityToDto)
                    .ifPresent(taskDto::setAssignee);
        }

        taskDto.setComments(taskCommentService.getCommentsByTaskId(taskId));
        taskDto.setStatusHistory(taskStatusHistoryService.getStatusHistoryByTaskId(taskId));
        taskDto.setTimeLogs(toTimeLogDtoList(taskTimeLogRepository.findByTaskId(taskId)));
        return taskDto;
    }

    List<TaskDetailsTimeLogDto> toTimeLogDtoList(List<TaskTimeLogsEntity> timeLogs) {
        return timeLogs.stream().map(log -> {
            String userName = userRepository.findById(log.getUserId())
                    .map(user -> user.getFirstName() + " " + user.getLastName() + "("+user.getUsername()+")")
                    .orElse("Неизвестно");
            return TaskDetailsTimeLogDto.builder()
                    .id(log.getId())
                    .taskId(log.getTaskId())
                    .userId(log.getUserId())
                    .userName(userName)
                    .startTime(log.getStartTime())
                    .endTime(log.getEndTime())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public TaskDetailsDto updateTask(UUID taskId, UpdateTaskPayload payload, HttpServletRequest request) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        UUID userUUID = extractUserUUIDFromRequest(request);
        validateProjectMember(task.getProjectId(), userUUID);

        task.setTitle(payload.title());
        task.setDescription(payload.description());

        if (payload.assigneeId() != null) {
            List<ProjectMemberDto> projectMembers = projectService.getProjectMembers(task.getProjectId());
            boolean isValidAssignee = projectMembers.stream()
                    .anyMatch(member -> member.getUser().getId().equals(payload.assigneeId()));
            if (!isValidAssignee) {
                throw new IllegalArgumentException("Assignee with ID " + payload.assigneeId() + " is not a member of the project");
            }
            task.setAssignedTo(payload.assigneeId());
        } else {
            task.setAssignedTo(null); // Allow unassigning the task
        }

        // Update priority
        if (payload.priority() != null) {
            // Assuming priority is validated against a set of allowed values (e.g., LOW, MEDIUM, HIGH)
            String priority = payload.priority().toUpperCase();
            if (!List.of("LOW", "MEDIUM", "HIGH").contains(priority)) {
                throw new IllegalArgumentException("Invalid priority value: " + payload.priority());
            }
            task.setPriority(priority);
        }

        // Update deadline
        if (payload.deadline() != null) {
            try {
                LocalDate deadline = LocalDate.parse(payload.deadline()); // Assuming YYYY-MM-DD format
                task.setDueDate(deadline);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid deadline format: " + payload.deadline());
            }
        } else {
            task.setDueDate(null);
        }

        taskRepository.save(task);

        return getTaskDetails(taskId);
    }

    @Transactional
    public void archiveTask(UUID taskId, HttpServletRequest request) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        UUID userUUID = extractUserUUIDFromRequest(request);
        validateProjectMember(task.getProjectId(), userUUID);

        task.setStatus("ARCHIVE");
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(UUID taskId, HttpServletRequest request) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        UUID userUUID = extractUserUUIDFromRequest(request);
        validateProjectMember(task.getProjectId(), userUUID);

        taskRepository.delete(task);
    }

    public TaskReportDto getTaskDetails(UUID taskId, HttpServletRequest request) {
        validateAuthorization(request);
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        // Optional: Check if user has access to the task (e.g., via project membership)
        return taskMapper.toReportDto(task, userRepository);
    }

    public List<StatusHistoryDto> getStatusHistory(UUID taskId, HttpServletRequest request) {
        validateAuthorization(request);
        List<TaskStatusHistoryEntity> history = taskStatusHistoryRepository.findByTaskId(taskId);
        return taskMapper.toStatusHistoryDtoList(history);
    }

    public List<TimeLogDto> getTimeLogs(UUID taskId, HttpServletRequest request) {
        validateAuthorization(request);
        List<TaskTimeLogsEntity> timeLogs = taskTimeLogRepository.findByTaskId(taskId);
        return taskMapper.toTimeLogDtoList(timeLogs, userRepository);
    }

    public List<CommentDto> getComments(UUID taskId, HttpServletRequest request) {
        validateAuthorization(request);
        List<TaskCommentEntity> comments = taskCommentRepository.findByTaskId(taskId);
        return taskMapper.toCommentDtoList(comments, userRepository);
    }

    @Transactional
    public void startTimer(UUID taskId, HttpServletRequest request) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        UUID userUUID = extractUserUUIDFromRequest(request);
        validateProjectMember(task.getProjectId(), userUUID);

        // Check if there's an active timer for this user and task
        Optional<TaskTimeLogsEntity> activeLog = taskTimeLogRepository.findByTaskIdAndUserIdAndEndTimeIsNull(taskId, userUUID);
        if (activeLog.isPresent()) {
            throw new IllegalStateException("Timer is already running for this task and user");
        }

        TaskTimeLogsEntity timeLog = TaskTimeLogsEntity.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userUUID)
                .startTime(LocalDateTime.now())
                .build();

        taskTimeLogRepository.save(timeLog);
    }

    @Transactional
    public void stopTimer(UUID taskId, HttpServletRequest request) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        UUID userUUID = extractUserUUIDFromRequest(request);
        validateProjectMember(task.getProjectId(), userUUID);

        // Find the active timer for this user and task
        TaskTimeLogsEntity timeLog = taskTimeLogRepository.findByTaskIdAndUserIdAndEndTimeIsNull(taskId, userUUID)
                .orElseThrow(() -> new IllegalStateException("No active timer found for this task and user"));

        timeLog.setEndTime(LocalDateTime.now());
        taskTimeLogRepository.save(timeLog);
    }

    private void validateAuthorization(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        try {
            String jwtToken = authHeader.substring(7);
            String email = jwtService.getEmailFromToken(jwtToken);
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Invalid email in token");
            }
            // Optional: Add logic to verify user access (e.g., check project membership)
        } catch (Exception e) {
            throw new SecurityException("Invalid JWT token", e);
        }
    }
}
