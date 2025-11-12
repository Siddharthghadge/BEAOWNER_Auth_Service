package com.carrental.authservice.dto;
import com.carrental.authservice.model.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private Role role;
}
