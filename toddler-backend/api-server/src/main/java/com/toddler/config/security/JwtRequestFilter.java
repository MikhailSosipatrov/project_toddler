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

        log.info("JwtRequestFilter запущен для запроса: {}", request.getRequestURI());

        String authorizationHeader = request.getHeader("Authorization");
        String jwtToken = null;
        String email = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            log.info("Access token получен из заголовка Authorization");

            try {
                email = jwtService.getEmailFromToken(jwtToken);
                log.info("Извлечён email из access token: {}", email);
            } catch (ExpiredJwtException e) {
                log.info("Access token истёк, пробуем использовать refresh token...", e);

                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    log.info("Найдено {} cookies", cookies.length);

                    for (Cookie cookie : cookies) {
                        log.info("Обработка cookie: {}", cookie.getName());
                        if ("refreshToken".equals(cookie.getName())) {
                            log.info("Найден refresh token в cookies");
                            String refreshToken = cookie.getValue();
                            try {
                                email = jwtService.verifyRefreshToken(refreshToken);
                                log.info("Refresh token валиден. Извлечён email: {}", email);

                                jwtToken = jwtService.generateAccessToken(email);
                                response.setHeader("New-Access-Token", jwtToken);
                                log.info("Сгенерирован новый access token и установлен в заголовке");

                                break;
                            } catch (Exception ex) {
                                log.info("Refresh token невалиден: {}", ex.getMessage(), ex);
                            }
                        }
                    }
                } else {
                    log.info("Cookies отсутствуют в запросе");
                }
            } catch (Exception e) {
                log.info("Ошибка при извлечении email из access токена: {}", e.getMessage(), e);
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("Попытка аутентифицировать пользователя по email: {}", email);
            Optional<UserEntity> userEntityOpt = userRepository.findByEmail(email);
            if (userEntityOpt.isPresent() && jwtService.validateToken(jwtToken, email)) {
                UserEntity userEntity = userEntityOpt.get();
                log.info("Пользователь {} найден и токен валиден. Выполняется установка аутентификации", email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userEntity, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.info("Пользователь не найден или токен не прошёл валидацию");
            }
        } else if (email == null) {
            log.info("Email не был получен из токена");
        } else {
            log.info("Контекст безопасности уже содержит аутентификацию");
        }

        chain.doFilter(request, response);
        log.info("JwtRequestFilter завершён для запроса: {}", request.getRequestURI());
    }
}
