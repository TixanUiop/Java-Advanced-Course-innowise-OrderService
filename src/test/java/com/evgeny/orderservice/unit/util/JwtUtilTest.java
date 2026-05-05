package com.evgeny.orderservice.unit.util;

import com.evgeny.orderservice.entity.Enums.AuthRole;
import com.evgeny.orderservice.exception.InvalidOrExpiredToken;
import com.evgeny.orderservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String secret = "01234567890123456789012345678901";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret);
    }

    @Test
    void generateAndValidateTokenSuccess() {
        Long userId = 123L;
        AuthRole role = AuthRole.USER;

        String token = jwtUtil.generateToken(userId, role);
        assertNotNull(token);

        Claims claims = jwtUtil.validateToken(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(role.toString(), claims.get("role", String.class));
    }

    @Test
    void generateRefreshTokenSuccess() {
        Long userId = 456L;

        String refreshToken = jwtUtil.generateRefreshToken(userId);
        assertNotNull(refreshToken);

        Claims claims = jwtUtil.validateToken(refreshToken);
        assertEquals(userId.toString(), claims.getSubject());
    }

    @Test
    void extractUserIdAndRoleSuccess() {
        Long userId = 789L;
        AuthRole role = AuthRole.ADMIN;

        String token = jwtUtil.generateToken(userId, role);

        Long extractedUserId = jwtUtil.extractUserId(token);
        String extractedRole = jwtUtil.extractRole(token);

        assertEquals(userId, extractedUserId);
        assertEquals(role.toString(), extractedRole);
    }

    @Test
    void validateTokenInvalidTokenThrows() {
        String invalidToken = "this.is.not.a.valid.jwt";

        assertThrows(InvalidOrExpiredToken.class, () -> jwtUtil.validateToken(invalidToken));
    }
}