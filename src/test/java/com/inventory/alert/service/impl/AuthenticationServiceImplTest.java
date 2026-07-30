package com.inventory.alert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.alert.dto.request.RegisterRequest;
import com.inventory.alert.dto.response.RegisterResponse;
import com.inventory.alert.entity.User;
import com.inventory.alert.enums.Role;
import com.inventory.alert.exception.DuplicateEmailException;
import com.inventory.alert.exception.InvalidCredentialsException;
import com.inventory.alert.exception.InvalidRegistrationRoleException;
import com.inventory.alert.repository.UserRepository;
import com.inventory.alert.security.CustomUserDetails;
import com.inventory.alert.security.jwt.JwtService;
import com.inventory.alert.service.support.RequestValidator;
import com.inventory.alert.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RequestValidator requestValidator;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void register_rejectsAdminRole() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Admin");
        request.setEmail("a@example.com");
        request.setPassword("password123");
        request.setRole(Role.ADMIN);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(InvalidRegistrationRoleException.class);
    }

    @Test
    void register_whenEmailExists_throws() {
        RegisterRequest request = viewerRequest();
        when(userRepository.existsByEmail("v@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void register_hashesPasswordAndSaves() {
        RegisterRequest request = viewerRequest();
        when(userRepository.existsByEmail("v@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });

        RegisterResponse response = authenticationService.register(request);

        assertThat(response.getUser().getId()).isEqualTo(7L);
        assertThat(response.getUser().getEmail()).isEqualTo("v@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.VIEWER);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void login_whenBadCredentials_throwsInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("v@example.com");
        request.setPassword("wrong-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("v@example.com");
        request.setPassword("password123");

        User user = new User("Viewer", "v@example.com", "HASH", Role.VIEWER, true);
        user.setId(7L);
        CustomUserDetails details = new CustomUserDetails(user);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(details);
        when(jwtService.generateToken(details)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var response = authenticationService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("v@example.com");
    }

    private static RegisterRequest viewerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Viewer");
        request.setEmail("v@example.com");
        request.setPassword("password123");
        request.setRole(Role.VIEWER);
        return request;
    }
}
