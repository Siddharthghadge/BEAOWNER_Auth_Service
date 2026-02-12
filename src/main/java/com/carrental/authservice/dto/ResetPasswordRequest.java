package com.carrental.authservice.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String token; // The 6-digit OTP
    private String newPassword;
}