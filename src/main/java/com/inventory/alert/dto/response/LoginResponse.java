package com.inventory.alert.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Successful login response with JWT")
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private AuthUserResponse user;
}
