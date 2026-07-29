package com.inventory.alert.dto.request;

import com.inventory.alert.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Size(max = 120)
    private String fullName;

    @Email
    @Size(max = 255)
    private String email;

    /**
     * Optional password change. Null means leave unchanged. Never returned in responses.
     */
    @Size(min = 8, max = 100)
    private String password;

    private Role role;

    private Boolean enabled;
}
