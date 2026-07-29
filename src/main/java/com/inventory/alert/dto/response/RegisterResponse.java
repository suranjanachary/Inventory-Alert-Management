package com.inventory.alert.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Successful registration response (login separately to obtain a token)")
public class RegisterResponse {

    private AuthUserResponse user;
    private String message;
}
