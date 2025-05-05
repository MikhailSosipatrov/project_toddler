package com.toddler.controller;

import com.toddler.service.InviteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invite")
public class InviteController {
    private final InviteService inviteService;

    @PostMapping("/join/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> joinProject(
            @PathVariable UUID projectId,
            @RequestParam String token,
            HttpServletRequest request) {
        inviteService.joinProject(projectId, token, request);
        return ResponseEntity.ok().build();
    }
}
