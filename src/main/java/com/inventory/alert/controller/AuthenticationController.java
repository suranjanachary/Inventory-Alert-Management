package com.inventory.alert.controller;

import com.inventory.alert.dto.request.LoginRequest;
import com.inventory.alert.dto.request.UserCreateRequest;
import com.inventory.alert.dto.response.AuthTokenResponse;
import com.inventory.alert.dto.response.ErrorResponse;
import com.inventory.alert.exception.ErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth API contract only. JWT issuance and password verification land in Phase 9.
 * Passwords are never logged.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthenticationController {

    @PostMapping("/login")
    @Operation(
            summary = "Login (placeholder)",
            description = "Validates the request body shape. Returns 501 until JWT is implemented.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "501",
                    description = "JWT not implemented",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("API login placeholder email={}", request.getEmail());
        return notImplemented("/api/v1/auth/login", "JWT login is not implemented yet (Phase 9)");
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register (placeholder)",
            description = "Validates registration payload. Persistence + hashing deferred to Phase 9.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "501",
                    description = "Registration not implemented",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    })
    public ResponseEntity<ErrorResponse> register(@Valid @RequestBody UserCreateRequest request) {
        log.info("API register placeholder email={}", request.getEmail());
        return notImplemented("/api/v1/auth/register", "User registration is not implemented yet (Phase 9)");
    }

    @PostMapping("/token-preview")
    @Operation(
            summary = "Document expected token response shape",
            description = "Does not issue tokens. Shows the AuthTokenResponse schema for clients.")
    @ApiResponse(responseCode = "200", description = "Example payload only")
    public ResponseEntity<AuthTokenResponse> tokenPreview() {
        return ResponseEntity.ok(AuthTokenResponse.builder()
                .accessToken("<jwt-placeholder>")
                .tokenType("Bearer")
                .expiresInSeconds(3600)
                .message("Illustrative response; real tokens arrive in Phase 9")
                .build());
    }

    private static ResponseEntity<ErrorResponse> notImplemented(String path, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_IMPLEMENTED.value())
                .error("Not Implemented")
                .message(message)
                .path(path)
                .errorCode(ErrorCodes.NOT_IMPLEMENTED)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(body);
    }
}
