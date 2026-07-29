package com.inventory.alert.dto.response;

import com.inventory.alert.enums.Role;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

/**
 * Password is intentionally omitted — never expose credentials.
 */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
