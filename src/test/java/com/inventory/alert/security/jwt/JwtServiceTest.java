package com.inventory.alert.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.inventory.alert.entity.User;
import com.inventory.alert.enums.Role;
import com.inventory.alert.security.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "aW52ZW50b3J5LWFsZXJ0LWRlbW8tc2VjcmV0LWtleS0zMmJ5dGVzLQ==",
                3600_000L);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateAndParseToken_roundTrip() {
        User user = new User("Admin", "admin@example.com", "hash", Role.ADMIN, true);
        user.setId(1L);
        CustomUserDetails details = new CustomUserDetails(user);

        String token = jwtService.generateToken(details);

        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.ADMIN);
        assertThat(jwtService.isTokenValid(token, details)).isTrue();
    }

    @Test
    void expiredToken_throwsExpiredJwtException() {
        JwtService shortLived = new JwtService(new JwtProperties(
                "aW52ZW50b3J5LWFsZXJ0LWRlbW8tc2VjcmV0LWtleS0zMmJ5dGVzLQ==",
                1L));
        User user = new User("Admin", "admin@example.com", "hash", Role.ADMIN, true);
        user.setId(1L);
        String token = shortLived.generateToken(new CustomUserDetails(user));

        try {
            Thread.sleep(5L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> shortLived.parseClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
