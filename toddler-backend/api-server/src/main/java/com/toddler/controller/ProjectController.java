package com.toddler.controller;

import com.toddler.controller.payload.CreateProjectPayload;
import com.toddler.controller.payload.UpdateProjectPayload;
import com.toddler.dto.ExtendedProjectDto;
import com.toddler.dto.TaskDto;
import com.toddler.exception.NotFoundException;
import com.toddler.exception.UnauthorizedException;
import com.toddler.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ExtendedProjectDto> createProject(
            @Valid @RequestBody CreateProjectPayload payload,
            HttpServletRequest request) {
        ExtendedProjectDto project = projectService.createNewProject(payload, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ExtendedProjectDto> getProject(@PathVariable UUID projectId) {
        ExtendedProjectDto project = projectService.getProject(projectId);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ExtendedProjectDto> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectPayload payload,
            HttpServletRequest request) {
        ExtendedProjectDto project = projectService.updateProject(projectId, payload, request);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId,
            HttpServletRequest request) {
        projectService.deleteProject(projectId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/archive")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> archiveProject(
            @PathVariable UUID projectId,
            HttpServletRequest request) {
        projectService.archiveProject(projectId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{projectId}/archive")
    public ResponseEntity<List<TaskDto>> getArchivedTasks(@PathVariable UUID projectId) {
        List<TaskDto> tasks = projectService.getArchivedTasks(projectId);
        return ResponseEntity.ok(tasks);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}