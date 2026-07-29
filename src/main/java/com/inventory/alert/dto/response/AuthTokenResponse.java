package com.inventory.alert.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Placeholder auth response until JWT is implemented")
public class AuthTokenResponse {

    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private String message;
}
