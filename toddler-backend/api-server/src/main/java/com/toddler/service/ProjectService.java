package com.toddler.service;

import com.toddler.controller.payload.CreateProjectPayload;
import com.toddler.controller.payload.UpdateProjectPayload;
import com.toddler.cosnts.ProjectStatus;
import com.toddler.cosnts.Roles;
import com.toddler.dto.ExtendedProjectDto;
import com.toddler.dto.ProjectDto;
import com.toddler.dto.ProjectMemberDto;
import com.toddler.dto.TaskDto;
import com.toddler.dto.UserDto;
import com.toddler.entity.ProjectEntity;
import com.toddler.entity.ProjectMemberEntity;
import com.toddler.entity.TaskEntity;
import com.toddler.entity.UserEntity;
import com.toddler.exception.NotFoundException;
import com.toddler.exception.UnauthorizedException;
import com.toddler.mapper.ProjectMapper;
import com.toddler.mapper.ProjectMemberMapper;
import com.toddler.mapper.TaskMapper;
import com.toddler.mapper.UserMapper;
import com.toddler.repository.ProjectMemberRepository;
import com.toddler.repository.ProjectsRepository;
import com.toddler.repository.TaskRepository;
import com.toddler.repository.UserRepository;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.toddler.cosnts.Roles.OWNER;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectsRepository projectsRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;

    @Transactional
    public ExtendedProjectDto createNewProject(CreateProjectPayload payload, HttpServletRequest request) {
        UUID userUUID = extractUserUUIDFromRequest(request);
        UUID projectUUID = UUID.randomUUID();

        ProjectEntity project = buildProjectEntity(payload, userUUID, projectUUID);
        ProjectMemberEntity member = buildProjectMemberEntity(userUUID, projectUUID);

        projectsRepository.save(project);
        projectMemberRepository.save(member);

        return getProject(projectUUID);
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

    private ProjectEntity buildProjectEntity(CreateProjectPayload payload, UUID userUUID, UUID projectUUID) {
        return ProjectEntity.builder()
                .id(projectUUID)
                .name(payload.name())
                .description(payload.description())
                .status(ProjectStatus.ACTIVE.name())
                .ownerId(userUUID)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ProjectMemberEntity buildProjectMemberEntity(UUID userUUID, UUID projectUUID) {
        return ProjectMemberEntity.builder()
                .id(UUID.randomUUID())
                .projectId(projectUUID)
                .userId(userUUID)
                .role(Roles.OWNER.getRole())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public ExtendedProjectDto updateProject(UUID projectUUID, UpdateProjectPayload payload, HttpServletRequest request) {
        ProjectEntity project = projectsRepository.findById(projectUUID)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectUUID));

        validateProjectOwner(project, extractUserUUIDFromRequest(request));

        project.setName(payload.name());
        project.setDescription(payload.description());
        project.setUpdatedAt(LocalDateTime.now());
        projectsRepository.save(project);

        return getProject(projectUUID);
    }

    @Transactional
    public void deleteProject(UUID projectUUID, HttpServletRequest request) {
        ProjectEntity project = projectsRepository.findById(projectUUID)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectUUID));

        validateProjectOwner(project, extractUserUUIDFromRequest(request));

        projectsRepository.delete(project);
    }

    @Transactional
    public void archiveProject(UUID projectUUID, HttpServletRequest request) {
        ProjectEntity project = projectsRepository.findById(projectUUID)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectUUID));

        validateProjectOwner(project, extractUserUUIDFromRequest(request));

        project.setStatus(ProjectStatus.ARCHIVED.name());
        project.setUpdatedAt(LocalDateTime.now());
        projectsRepository.save(project);
    }

    @Transactional(readOnly = true)
    public ExtendedProjectDto getProject(UUID projectUUID) {
        ProjectEntity project = projectsRepository.findById(projectUUID)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectUUID));

        ProjectDto projectDto = projectMapper.fromEntityToDto(project);
        List<TaskDto> taskDtos = getProjectTasksWithExecutors(project.getId());
        List<ProjectMemberDto> memberDtos = getProjectMembers(projectUUID);

        return ExtendedProjectDto.builder()
                .project(projectDto)
                .tasks(taskDtos)
                .members(memberDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getArchivedTasks(UUID projectUUID) {
        ProjectEntity project = projectsRepository.findById(projectUUID)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectUUID));

        List<TaskEntity> tasks = taskRepository.findByProjectId(projectUUID);
        Set<UUID> userIds = tasks.stream()
                .map(TaskEntity::getAssignedTo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, UserDto> userMap = userRepository.findAllByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, userMapper::fromEntityToDto));

        System.out.println("Я здесь");

        return tasks.stream()
                .filter(task -> task.getStatus().equals("ARCHIVE"))
                .map(task -> {
                    TaskDto dto = taskMapper.fromEntityToDto(task);
                    Optional.ofNullable(task.getAssignedTo())
                            .map(userMap::get)
                            .ifPresent(dto::setExecutor);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<TaskDto> getProjectTasksWithExecutors(UUID projectId) {
        List<TaskEntity> tasks = taskRepository.findByProjectId(projectId);
        Set<UUID> userIds = tasks.stream()
                .map(TaskEntity::getAssignedTo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, UserDto> userMap = userRepository.findAllByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, userMapper::fromEntityToDto));

        return tasks.stream()
                .map(task -> {
                    TaskDto dto = taskMapper.fromEntityToDto(task);
                    Optional.ofNullable(task.getAssignedTo())
                            .map(userMap::get)
                            .ifPresent(dto::setExecutor);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<ProjectMemberDto> getProjectMembers(UUID projectUUID) {
        List<ProjectMemberEntity> members = projectMemberRepository.findAllByProjectId(projectUUID);
        Set<UUID> userIds = members.stream()
                .map(ProjectMemberEntity::getUserId)
                .collect(Collectors.toSet());
        Map<UUID, UserDto> userMap = userRepository.findAllByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, userMapper::fromEntityToDto));

        return members.stream()
                .map(member -> projectMemberMapper.fromEntityToDto(member, userMap.get(member.getUserId())))
                .collect(Collectors.toList());
    }

    private void validateProjectOwner(ProjectEntity project, UUID userUUID) {
        if (!project.getOwnerId().equals(userUUID)) {
            throw new UnauthorizedException("Only the project owner can perform this action");
        }
    }
}
