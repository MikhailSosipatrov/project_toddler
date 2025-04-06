package com.toddler.service.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtService {

    private final String secretKeyString = "mySuperSecretKeyThatIs256BitsLong" +
            "AndMakesItSecureEnoughForHMACSHA256";

    private final Key secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());


    public String generateAccessToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 604800000))
                .signWith(secretKey)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser().verifyWith((SecretKey) secretKey).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        final Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public boolean validateToken(String token, String email) {
        final String tokenEmail = getEmailFromToken(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    public String verifyRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);

            String email = claims.getSubject();
            if (email == null) {
                throw new IllegalArgumentException("Invalid refresh token: no subject");
            }

            return email;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Refresh token is invalid or expired", e);
        }
    }


    public void setRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}

