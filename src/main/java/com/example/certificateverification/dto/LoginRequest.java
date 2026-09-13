package com.example.certificateverification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username or Roll Number is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
