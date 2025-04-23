package com.toddler.service;

import com.toddler.controller.payload.CreateProjectPayload;
import com.toddler.controller.payload.UpdateProjectPayload;
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
import com.toddler.mapper.ProjectMapper;
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

    @Transactional
    public void createNewProject(CreateProjectPayload payload, HttpServletRequest request) {
        UUID userUUID = extractUserUUIDFromRequest(request);
        UUID projectUUID = UUID.randomUUID();

        ProjectEntity project = buildProjectEntity(payload, userUUID, projectUUID);
        ProjectMemberEntity member = buildProjectMemberEntity(userUUID, projectUUID);

        projectsRepository.save(project);
        projectMemberRepository.save(member);
    }

    private UUID extractUserUUIDFromRequest(HttpServletRequest request) {
        String token = Optional.ofNullable(request.getHeader("Authorization"))
                .map(header -> header.substring(7))
                .orElseThrow(() -> new NotFoundException("JWT token not found in header"));

        String email = jwtService.getEmailFromToken(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email not found"))
                .getId();
    }

    private ProjectEntity buildProjectEntity(CreateProjectPayload payload, UUID userUUID, UUID projectUUID) {
        return ProjectEntity.builder()
                .id(projectUUID)
                .name(payload.name())
                .description(payload.description())
                .status("active")
                .ownerId(userUUID)
                .build();
    }

    private ProjectMemberEntity buildProjectMemberEntity(UUID userUUID, UUID projectUUID) {
        return ProjectMemberEntity.builder()
                .id(UUID.randomUUID())
                .projectId(projectUUID)
                .userId(userUUID)
                .role(OWNER.getRole())
                .build();
    }

    public void updateProject(UUID projectUUID, UpdateProjectPayload payload) {
        ProjectEntity project = projectsRepository.findById(projectUUID)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        project.setName(payload.name());
        project.setDescription(payload.description());
    }

    public void deleteProject(UUID projectUUID) {
        ProjectEntity project = projectsRepository.findById(projectUUID)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        projectsRepository.delete(project);
    }

    public ExtendedProjectDto getProject(UUID projectUUID) {
        ProjectEntity project = projectsRepository.findById(projectUUID)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        ProjectDto projectDto = projectMapper.fromEntityToDto(project);
        List<TaskDto> taskDtos = getProjectTasksWithExecutors(project.getId());
        List<ProjectMemberDto> memberDtos = getProjectMembers(projectUUID);

        return ExtendedProjectDto.builder()
                .projectDto(projectDto)
                .tasks(taskDtos)
                .members(memberDtos)
                .build();
    }

    private List<TaskDto> getProjectTasksWithExecutors(UUID projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(task -> {
                    TaskDto dto = taskMapper.fromEntityToDto(task);
                    userRepository.findById(task.getAssignedTo())
                            .map(userMapper::fromEntityToDto)
                            .ifPresent(dto::setExecutor);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<ProjectMemberDto> getProjectMembers(UUID projectUUID) {
        return projectMemberRepository.findAllByProjectId(projectUUID).stream()
                .map(member -> {
                    UserDto userDto = userRepository.findById(member.getUserId())
                            .map(userMapper::fromEntityToDto)
                            .orElseThrow(() -> new NotFoundException("User not found for member"));

                    return ProjectMemberDto.builder()
                            .role(member.getRole())
                            .user(userDto)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
