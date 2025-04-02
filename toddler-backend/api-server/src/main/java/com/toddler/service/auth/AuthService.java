package com.toddler.service.auth;

import com.toddler.controller.payload.RegisterPayload;
import com.toddler.entity.UserEntity;
import com.toddler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void registerUser(RegisterPayload authPayload) {
        if (userRepository.existsByEmail(authPayload.email())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        String encodedPassword = passwordEncoder.encode(authPayload.password());

        UserEntity user = new UserEntity();
        user.setEmail(authPayload.email());
        user.setUsername(authPayload.username());
        user.setFirstName(authPayload.firstName());
        user.setLastName(authPayload.lastName());
        user.setPasswordHash(encodedPassword);

        userRepository.save(user);
    }
}
