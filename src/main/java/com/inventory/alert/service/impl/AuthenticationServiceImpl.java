package com.inventory.alert.service.impl;

import com.inventory.alert.dto.request.LoginRequest;
import com.inventory.alert.dto.request.RegisterRequest;
import com.inventory.alert.dto.response.AuthUserResponse;
import com.inventory.alert.dto.response.LoginResponse;
import com.inventory.alert.dto.response.RegisterResponse;
import com.inventory.alert.entity.User;
import com.inventory.alert.enums.Role;
import com.inventory.alert.exception.DuplicateEmailException;
import com.inventory.alert.exception.InvalidCredentialsException;
import com.inventory.alert.exception.InvalidRegistrationRoleException;
import com.inventory.alert.repository.UserRepository;
import com.inventory.alert.security.CustomUserDetails;
import com.inventory.alert.security.jwt.JwtService;
import com.inventory.alert.service.AuthenticationService;
import com.inventory.alert.service.support.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RequestValidator requestValidator;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        requestValidator.validate(request);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            log.info("User logged in email={} role={}", principal.getEmail(), principal.getRole());
            return LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationSeconds())
                    .user(toAuthUser(principal))
                    .build();
        } catch (BadCredentialsException | DisabledException ex) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        requestValidator.validate(request);
        if (request.getRole() == Role.ADMIN) {
            throw new InvalidRegistrationRoleException();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        String hashed = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getFullName(),
                request.getEmail(),
                hashed,
                request.getRole(),
                Boolean.TRUE);
        User saved = userRepository.save(user);
        log.info("User registered id={} email={} role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return RegisterResponse.builder()
                .user(AuthUserResponse.builder()
                        .id(saved.getId())
                        .name(saved.getFullName())
                        .email(saved.getEmail())
                        .role(saved.getRole())
                        .build())
                .message("Registration successful. Please login to obtain an access token.")
                .build();
    }

    private static AuthUserResponse toAuthUser(CustomUserDetails principal) {
        return AuthUserResponse.builder()
                .id(principal.getId())
                .name(principal.getFullName())
                .email(principal.getEmail())
                .role(principal.getRole())
                .build();
    }
}
