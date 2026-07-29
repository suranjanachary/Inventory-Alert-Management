package com.inventory.alert.dto.request;

import com.inventory.alert.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Public registration. ADMIN role is rejected — seed admins separately.")
public class RegisterRequest {

    @NotBlank
    @Size(max = 120)
    @Schema(example = "Jane Manager")
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(example = "manager@example.com")
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(example = "password123", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @NotNull
    @Schema(allowableValues = {"MANAGER", "VIEWER"}, example = "VIEWER")
    private Role role;
}
