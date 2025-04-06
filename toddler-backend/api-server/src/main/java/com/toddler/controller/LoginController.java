package com.toddler.controller;

import com.toddler.controller.payload.LoginPayload;
import com.toddler.dto.LoginResponseDto;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LoginController {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody LoginPayload loginRequest, HttpServletResponse response) throws Exception {

        authenticate(loginRequest.email(), loginRequest.password());

        final String accessToken = jwtService.generateAccessToken(loginRequest.email());
        final String refreshToken = jwtService.generateRefreshToken(loginRequest.email());

        jwtService.setRefreshTokenCookie(refreshToken, response);

        return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken));
    }

    private void authenticate(String email, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new Exception("Неверные учетные данные", e);
        }
    }
}
