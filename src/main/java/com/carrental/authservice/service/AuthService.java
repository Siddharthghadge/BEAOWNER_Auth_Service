package com.carrental.authservice.service;

import com.carrental.authservice.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
