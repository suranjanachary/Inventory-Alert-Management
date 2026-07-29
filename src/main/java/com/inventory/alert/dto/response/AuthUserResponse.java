package com.inventory.alert.dto.response;

import com.inventory.alert.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Authenticated user summary (never includes password)")
public class AuthUserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
}
