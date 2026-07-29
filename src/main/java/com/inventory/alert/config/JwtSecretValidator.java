package com.inventory.alert.config;

import com.inventory.alert.security.jwt.JwtProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Fails fast in production when JWT_SECRET is missing — avoids booting with an empty signing key.
 */
@Configuration
public class JwtSecretValidator {

    @Bean
    @Profile("prod")
    ApplicationRunner requireJwtSecret(JwtProperties jwtProperties) {
        return args -> {
            if (jwtProperties.secret() == null || jwtProperties.secret().isBlank()) {
                throw new IllegalStateException(
                        "JWT_SECRET must be set for the prod profile (Base64-encoded 256-bit+ key).");
            }
        };
    }
}
