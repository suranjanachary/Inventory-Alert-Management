package com.inventory.alert.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Login credentials. JWT issuance is deferred to Phase 9.")
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(example = "admin@example.com")
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(example = "password123", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
}
