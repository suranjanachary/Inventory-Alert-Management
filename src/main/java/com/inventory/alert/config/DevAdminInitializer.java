package com.inventory.alert.config;

import com.inventory.alert.entity.User;
import com.inventory.alert.enums.Role;
import com.inventory.alert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a local ADMIN when none exists (dev profile only). Password is never logged.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevAdminInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@inventory.local";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }
        User admin = new User(
                "System Admin",
                ADMIN_EMAIL,
                passwordEncoder.encode("AdminPass123!"),
                Role.ADMIN,
                Boolean.TRUE);
        userRepository.save(admin);
        log.info("Seeded dev ADMIN user email={}", ADMIN_EMAIL);
    }
}
