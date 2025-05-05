package com.toddler.service;

import com.toddler.controller.payload.UpdateMemberRolePayload;
import com.toddler.entity.ProjectEntity;
import com.toddler.entity.ProjectMemberEntity;
import com.toddler.entity.UserEntity;
import com.toddler.exception.NotFoundException;
import com.toddler.exception.UnauthorizedException;
import com.toddler.mapper.ProjectMemberMapper;
import com.toddler.repository.ProjectMemberRepository;
import com.toddler.repository.ProjectsRepository;
import com.toddler.repository.UserRepository;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectsRepository projectsRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ProjectMemberMapper projectMemberMapper;

    @Transactional
    public void removeMember(UUID projectId, UUID userId, HttpServletRequest request) {
        ProjectEntity project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectId));

        UUID requestingUserId = extractUserUUIDFromRequest(request);
        validateProjectOwner(project, requestingUserId);

        if (project.getOwnerId().equals(userId)) {
            throw new UnauthorizedException("Cannot remove the project owner");
        }

        ProjectMemberEntity member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Member not found in project"));

        projectMemberRepository.delete(member);
    }

    @Transactional
    public void updateMemberRole(UUID projectId, UUID userId, UpdateMemberRolePayload payload, HttpServletRequest request) {
        ProjectEntity project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectId));

        UUID requestingUserId = extractUserUUIDFromRequest(request);
        validateProjectOwner(project, requestingUserId);

        if (project.getOwnerId().equals(userId)) {
            throw new UnauthorizedException("Cannot change the role of the project owner");
        }

        ProjectMemberEntity member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Member not found in project"));

        member.setRole(projectMemberMapper.mapRoleToBackend(payload.role()));
        member.setUpdatedAt(LocalDateTime.now());
        projectMemberRepository.save(member);
    }

    @Transactional
    public void addMember(UUID projectId, UUID userId, HttpServletRequest request) {
        projectsRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectId));

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        Optional<ProjectMemberEntity> existingMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (existingMember.isPresent()) {
            throw new IllegalStateException("User is already a member of the project");
        }

        ProjectMemberEntity member = ProjectMemberEntity.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .userId(userId)
                .role(com.toddler.cosnts.Roles.WORKER.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        projectMemberRepository.save(member);
    }

    private void validateProjectOwner(ProjectEntity project, UUID userId) {
        if (!project.getOwnerId().equals(userId)) {
            throw new UnauthorizedException("Only the project owner can perform this action");
        }
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