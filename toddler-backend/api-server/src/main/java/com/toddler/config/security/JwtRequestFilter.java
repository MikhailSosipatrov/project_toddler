package com.toddler.config.security;

import com.toddler.entity.UserEntity;
import com.toddler.repository.UserRepository;
import com.toddler.service.auth.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String jwtToken = null;
        String email = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            try {
                email = jwtService.getEmailFromToken(jwtToken);
            } catch (ExpiredJwtException e) {
                log.info("Access token истёк, пробуем использовать refresh token...", e);

                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    System.out.println("Куки не null");
                    for (Cookie cookie : cookies) {
                        System.out.println(cookie.getName());
                        if ("refreshToken".equals(cookie.getName())) {
                            System.out.println("I am here 4");
                            String refreshToken = cookie.getValue();
                            try {
                                System.out.println("I am here");
                                email = jwtService.verifyRefreshToken(refreshToken);
                                jwtToken = jwtService.generateAccessToken(email);

                                response.setHeader("New-Access-Token", jwtToken);

                                break;
                            } catch (Exception ex) {
                                log.error("Refresh token невалиден: " + ex.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка при извлечении email из access токена: " + e.getMessage());
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<UserEntity> userEntityOpt = userRepository.findByEmail(email);
            if (userEntityOpt.isPresent() && jwtService.validateToken(jwtToken, email)) {
                UserEntity userEntity = userEntityOpt.get();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userEntity, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }

}

