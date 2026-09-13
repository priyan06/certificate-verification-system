package com.example.certificateverification.service;

import com.example.certificateverification.dto.AuthResponse;
import com.example.certificateverification.dto.ChangePasswordRequest;
import com.example.certificateverification.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);
    void changePassword(String username, ChangePasswordRequest request);
    AuthResponse getCurrentUser(String username);
}
