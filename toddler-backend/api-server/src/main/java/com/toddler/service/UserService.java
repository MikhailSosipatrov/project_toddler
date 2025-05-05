package com.toddler.service;

import com.toddler.controller.payload.UserProfileUpdateRequest;
import com.toddler.dto.UserProfileDto;
import com.toddler.entity.UserEntity;
import com.toddler.exception.NotFoundException;
import com.toddler.mapper.UserMapper;
import com.toddler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.file.upload-dir}")
    private String uploadDir;

    @Value("${application.file.access-url}")
    private String accessUrl;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif"};

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return userMapper.toProfileDto(user);
    }

    @Transactional
    public void updateUserProfile(String email, UserProfileUpdateRequest request) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        userMapper.updateEntityFromRequest(request, user);
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        userRepository.save(user);
    }

    @Transactional
    public String uploadAvatar(String email, MultipartFile file) throws Exception {
        // Проверка файла
        if (file == null || file.isEmpty()) {
            throw new Exception("File is empty or not provided");
        }

        // Проверка размера файла
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new Exception("File size exceeds limit of 5MB");
        }

        // Проверка типа файла
        String contentType = file.getContentType();
        if (!Arrays.asList(ALLOWED_IMAGE_TYPES).contains(contentType)) {
            throw new Exception("Only JPEG, PNG, and GIF images are allowed");
        }

        // Поиск пользователя
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // Удаление старого аватара, если он существует
        if (user.getProfilePicture() != null) {
            deleteOldAvatar(user.getProfilePicture());
        }

        // Создание директории для загрузки, если она не существует
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Генерация уникального имени файла
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + (fileExtension != null ? fileExtension : ".jpg");
        Path filePath = uploadPath.resolve(fileName);

        // Сохранение файла
        Files.write(filePath, file.getBytes());

        // Формирование URL для доступа
        String avatarUrl = accessUrl + fileName;
        user.setProfilePicture(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    // Удаление старого аватара
    private void deleteOldAvatar(String avatarUrl) {
        try {
            String fileName = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Логируем ошибку, но не прерываем выполнение
            System.err.println("Failed to delete old avatar: " + e.getMessage());
        }
    }

    // Получение расширения файла
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    }
}