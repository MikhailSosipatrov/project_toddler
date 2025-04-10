package com.toddler.service;

import com.toddler.controller.payload.CreateProjectPayload;
import com.toddler.cosnts.Roles;
import com.toddler.entity.ProjectEntity;
import com.toddler.entity.ProjectMemberEntity;
import com.toddler.entity.UserEntity;
import com.toddler.exception.NotFoundException;
import com.toddler.repository.ProjectMemberRepository;
import com.toddler.repository.ProjectsRepository;
import com.toddler.repository.UserRepository;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.toddler.cosnts.Roles.OWNER;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectsRepository projectsRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public void createNewProject(CreateProjectPayload payload, HttpServletRequest httpServletRequest) {

        UUID userUUID = getUserUUID(httpServletRequest);
        UUID projectUUID = UUID.randomUUID();

        saveProject(userUUID, projectUUID, payload);
        saveProjectMember(userUUID, projectUUID);
    };

    private UUID getUserUUID(HttpServletRequest httpServletRequest) {
        String authorizationHeader = httpServletRequest.getHeader("Authorization");

        String jwtToken = authorizationHeader.substring(7);

        String email = jwtService.getEmailFromToken(jwtToken);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким email не найден"));
        return user.getId();
    }

    private void saveProject(UUID userUUID, UUID projectUUID, CreateProjectPayload payload) {
        ProjectEntity project = ProjectEntity.builder()
                .id(projectUUID)
                .name(payload.name())
                .description(payload.description())
                .ownerId(userUUID)
                .build();

        projectsRepository.save(project);
    }

    private void saveProjectMember(UUID userUUID, UUID projectUUID){
        ProjectMemberEntity projectMemberEntity = ProjectMemberEntity.builder()
                .id(UUID.randomUUID())
                .projectId(projectUUID)
                .userId(userUUID)
                .role(OWNER.getRole())
                .build();

        projectMemberRepository.save(projectMemberEntity);
    }
}
