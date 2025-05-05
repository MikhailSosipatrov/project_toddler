package com.toddler.controller;

import com.toddler.dto.NotificationDto;
import com.toddler.entity.UserEntity;
import com.toddler.mapper.NotificationMapper;
import com.toddler.repository.UserRepository;
import com.toddler.service.NotificationService;
import com.toddler.service.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping
    public List<NotificationDto> getUnreadNotifications(HttpServletRequest request) {
        UUID userId = extractUserIdFromRequest(request);
        return notificationMapper.toDtoList(notificationService.getUnreadNotifications(userId));
    }

    @PutMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
    }

    private UUID extractUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtService.getEmailFromToken(token);
        return userRepository.findByEmail(email)
                .map(UserEntity::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
