package com.toddler.service;

import com.toddler.entity.ProjectEntity;
import com.toddler.entity.UserEntity;
import com.toddler.exception.NotFoundException;
import com.toddler.exception.UnauthorizedException;
import com.toddler.repository.ProjectsRepository;
import com.toddler.repository.UserRepository;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final ProjectsRepository projectsRepository;
    private final ProjectMemberService projectMemberService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public void joinProject(UUID projectId, String token, HttpServletRequest request) {
        ProjectEntity project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectId));

        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Invalid invite token");
        }

        UUID userId = extractUserUUIDFromRequest(request);

        projectMemberService.addMember(projectId, userId, request);
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
