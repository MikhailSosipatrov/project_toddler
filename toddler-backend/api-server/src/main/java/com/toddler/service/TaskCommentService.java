package com.toddler.service;

import com.toddler.controller.payload.CreateTaskCommentPayload;
import com.toddler.dto.TaskCommentDto;
import com.toddler.entity.TaskCommentEntity;
import com.toddler.entity.TaskEntity;
import com.toddler.entity.UserEntity;
import com.toddler.exception.NotFoundException;
import com.toddler.exception.UnauthorizedException;
import com.toddler.mapper.TaskCommentMapper;
import com.toddler.repository.ProjectMemberRepository;
import com.toddler.repository.TaskCommentRepository;
import com.toddler.repository.TaskRepository;
import com.toddler.repository.UserRepository;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskCommentMapper taskCommentMapper;
    private final JwtService jwtService;

    @Transactional
    public TaskCommentDto addComment(UUID taskId, CreateTaskCommentPayload payload, HttpServletRequest request) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        UUID userId = extractUserUUIDFromRequest(request);
        validateProjectMember(task.getProjectId(), userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        TaskCommentEntity comment = TaskCommentEntity.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .content(payload.text())
                .createdAt(LocalDateTime.now())
                .build();

        taskCommentRepository.save(comment);

        TaskCommentDto commentDto = taskCommentMapper.fromEntityToDto(comment);
        commentDto.setAuthor(user.getFirstName() + " " + user.getLastName());
        return commentDto;
    }

    @Transactional(readOnly = true)
    public List<TaskCommentDto> getCommentsByTaskId(UUID taskId) {
        List<TaskCommentEntity> comments = taskCommentRepository.findByTaskId(taskId);
        return comments.stream()
                .map(comment -> {
                    TaskCommentDto dto = taskCommentMapper.fromEntityToDto(comment);
                    userRepository.findById(comment.getUserId())
                            .ifPresent(user -> dto.setAuthor(user.getFirstName() + " " + user.getLastName() + "("+ user.getUsername()+")"));
                    return dto;
                })
                .collect(Collectors.toList());
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
}