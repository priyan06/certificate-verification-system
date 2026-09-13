package com.example.certificateverification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String username;
    private String name;
    private String role;
    private Long studentId;
    private Boolean passwordChangeRequired;
    private String message;
}
