package com.toddler.controller;

import com.toddler.controller.payload.CreateProjectPayload;
import com.toddler.controller.payload.LoginPayload;
import com.toddler.controller.payload.UpdateProjectPayload;
import com.toddler.dto.DashboardDto;
import com.toddler.dto.ExtendedProjectDto;
import com.toddler.service.DashboardService;
import com.toddler.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/project")
    public ResponseEntity<?> createProject(@Valid @RequestBody CreateProjectPayload createProjectRequest, HttpServletRequest httpServletRequest) {
        try {
            projectService.createNewProject(createProjectRequest, httpServletRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable UUID projectId) {
        try {
            ExtendedProjectDto project = projectService.getProject(projectId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PutMapping("/project/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable UUID projectId, @Valid @RequestBody UpdateProjectPayload updateProjectRequest) {
        try {
            projectService.updateProject(projectId, updateProjectRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/project/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable UUID projectId) {
        try {
            projectService.deleteProject(projectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
