package com.toddler.controller;

import com.toddler.controller.payload.UpdateMemberRolePayload;
import com.toddler.exception.NotFoundException;
import com.toddler.exception.UnauthorizedException;
import com.toddler.service.ProjectMemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/members")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            HttpServletRequest request) {
        projectMemberService.removeMember(projectId, userId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/role")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateMemberRolePayload payload,
            HttpServletRequest request) {
        projectMemberService.updateMemberRole(projectId, userId, payload, request);
        return ResponseEntity.ok().build();
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
