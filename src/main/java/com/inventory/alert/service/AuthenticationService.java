package com.inventory.alert.service;

import com.inventory.alert.dto.request.LoginRequest;
import com.inventory.alert.dto.request.RegisterRequest;
import com.inventory.alert.dto.response.LoginResponse;
import com.inventory.alert.dto.response.RegisterResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
}
