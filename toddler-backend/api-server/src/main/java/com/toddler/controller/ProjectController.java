package com.toddler.controller;

import com.toddler.controller.payload.CreateProjectPayload;
import com.toddler.controller.payload.LoginPayload;
import com.toddler.dto.DashboardDto;
import com.toddler.service.DashboardService;
import com.toddler.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
