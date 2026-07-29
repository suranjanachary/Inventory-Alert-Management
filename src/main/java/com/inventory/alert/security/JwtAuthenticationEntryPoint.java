package com.inventory.alert.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.alert.dto.response.ErrorResponse;
import com.inventory.alert.exception.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Returns JSON 401 for unauthenticated / invalid JWT requests (instead of HTML/www-authenticate defaults).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        String jwtError = (String) request.getAttribute("jwt_error");
        String message = jwtError != null
                ? jwtError
                : (authException.getMessage() != null
                        ? authException.getMessage()
                        : "Authentication is required");

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(message)
                .path(request.getRequestURI())
                .errorCode(ErrorCodes.UNAUTHORIZED)
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
