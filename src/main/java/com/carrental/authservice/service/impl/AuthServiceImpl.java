

package com.carrental.authservice.service.impl;

import com.carrental.authservice.client.UserServiceClient;
import com.carrental.authservice.config.JwtUtil;
import com.carrental.authservice.dto.*;
import com.carrental.authservice.model.User;
import com.carrental.authservice.repository.UserRepository;
import com.carrental.authservice.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    // -------- FIXED CONSTRUCTOR --------
    // All final fields MUST be in this constructor
    public AuthServiceImpl(UserServiceClient userServiceClient,
                           JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder,
                           UserRepository userRepository,
                           RestTemplate restTemplate) {
        this.userServiceClient = userServiceClient;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {

        // Debug: print raw password
        System.out.println("PASSWORD BEFORE ENCODING: " + request.getPassword());

        // Encode password
        String encoded = passwordEncoder.encode(request.getPassword());

        // Debug: print encoded password
        System.out.println("PASSWORD AFTER ENCODING: " + encoded);

        // Build DTO to send to user-service (no Lombok builder)
        CreateUserRequest createUser = new CreateUserRequest();
        createUser.setName(request.getName());
        createUser.setEmail(request.getEmail());
        createUser.setPassword(encoded);
        createUser.setPhone(request.getPhone());
        createUser.setAddress(request.getAddress());
        createUser.setRole(request.getRole());
        // created_at can be left null, user-service may set it

        // Save in user-service
        UserResponse created = userServiceClient.registerInternal(createUser);

        // Generate tokens
        String accessToken = jwtUtil.generateToken(created.getEmail(), created.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(created.getEmail(), created.getRole());

        return new AuthResponse(accessToken, created.getRole(), refreshToken);
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        UserResponse user = userServiceClient.getUserByEmail(request.getEmail());

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // ✅ ADD THIS CHECK: Block banned users from logging in
        if (user.getEnabled() != null && !user.getEnabled()) {
            throw new RuntimeException("Access Denied: Your account has been banned by the administrator.");
        }

        if (user.getPassword() == null) {
            throw new RuntimeException("Password not stored for user");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole());

        return new AuthResponse(accessToken, user.getRole(), refreshToken);
    }

    @Override
    public String refreshAccessToken(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtUtil.extractUsername(refreshToken);

        UserResponse user = userServiceClient.getUserByEmail(email);

        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }


    @Override
    @Transactional
    public void generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate 6-digit code
        String token = String.format("%06d", new Random().nextInt(999999));

        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send to Notification Service (Port 8086)
        try {
            Map<String, String> emailRequest = new HashMap<>();
            emailRequest.put("email", email);
            emailRequest.put("message", "Your password reset code is: " + token);

            restTemplate.postForObject("http://localhost:8086/api/notifications/send", emailRequest, String.class);
        } catch (Exception e) {
            // Log error but don't stop execution
            System.out.println("Notification failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validations
        if (user.getResetToken() == null || !user.getResetToken().equals(request.getToken())) {
            throw new RuntimeException("Invalid reset token.");
        }
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired.");
        }

        // Update & Clear token fields
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}



