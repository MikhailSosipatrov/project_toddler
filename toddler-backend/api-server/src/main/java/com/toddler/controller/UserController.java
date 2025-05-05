package com.toddler.controller;

import com.toddler.controller.payload.UserProfileUpdateRequest;
import com.toddler.dto.UserProfileDto;
import com.toddler.service.UserService;
import com.toddler.service.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        UserProfileDto profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateUserProfile(HttpServletRequest request,
                                                  @RequestBody UserProfileUpdateRequest updateRequest) {
        String email = extractEmailFromRequest(request);
        userService.updateUserProfile(email, updateRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/avatar")
    public ResponseEntity<UserProfileDto> uploadAvatar(HttpServletRequest request,
                                                       @RequestParam("avatar") MultipartFile file) throws Exception {
        String email = extractEmailFromRequest(request);
        String avatarUrl = userService.uploadAvatar(email, file);
        UserProfileDto profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    private String extractEmailFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.getEmailFromToken(token);
    }
}