package com.toddler.service;

import com.toddler.entity.NotificationEntity;
import com.toddler.entity.TaskEntity;
import com.toddler.entity.UserEntity;
import com.toddler.repository.NotificationRepository;
import com.toddler.repository.TaskRepository;
import com.toddler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public void createNotification(UUID userId, String message) {
        NotificationEntity notification = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        // Send email notification
        userRepository.findById(userId).ifPresent(user -> sendEmail(user.getEmail(), "Новое уведомление", message));
    }

    @Transactional(readOnly = true)
    public List<NotificationEntity> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Scheduled(cron = "0 0 9 * * *") // Run daily at 9 AM
    @Transactional
    public void checkExpiredDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        List<TaskEntity> overdueTasks = taskRepository.findByDueDateBeforeAndStatusNotIn(now, List.of("DONE", "ARCHIVE"));
        for (TaskEntity task : overdueTasks) {
            if (task.getAssignedTo() != null) {
                String message = String.format("Задача '%s' просрочена (дедлайн: %s)", task.getTitle(), task.getDueDate());
                createNotification(task.getAssignedTo(), message);
            }
        }
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("mikhailsosipatrov194@yandex.ru"); // Configure in application.properties
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log email sending failure but don't fail the transaction
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
