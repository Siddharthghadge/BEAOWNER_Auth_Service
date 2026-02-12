package com.carrental.authservice.service;

import com.carrental.authservice.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
   // UserResponse register(RegisterRequest request);
   String refreshAccessToken(String refreshToken);
    void generateResetToken(String email);
    void resetPassword(ResetPasswordRequest request);

}
