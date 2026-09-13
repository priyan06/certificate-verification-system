package com.example.certificateverification.service;

import com.example.certificateverification.dto.AuthResponse;
import com.example.certificateverification.dto.ChangePasswordRequest;
import com.example.certificateverification.dto.LoginRequest;
import com.example.certificateverification.entity.Role;
import com.example.certificateverification.entity.Student;
import com.example.certificateverification.entity.User;
import com.example.certificateverification.exception.InvalidCredentialsException;
import com.example.certificateverification.exception.ResourceNotFoundException;
import com.example.certificateverification.repository.StudentRepository;
import com.example.certificateverification.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        // Try username match, roll number match, or register number match
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> {
                    Optional<Student> studentOpt = studentRepository.findByRollNumber(request.getUsername());
                    if (studentOpt.isPresent()) {
                        return studentOpt.get().getUser();
                    }
                    Optional<Student> studentRegOpt = studentRepository.findByRegisterNumber(request.getUsername());
                    if (studentRegOpt.isPresent()) {
                        return studentRegOpt.get().getUser();
                    }
                    throw new InvalidCredentialsException("Invalid credentials provided");
                });

        if (!user.getEnabled()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials provided");
        }

        // Establish Spring Security context
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);

        // Store authentication context in HTTP session
        if (httpRequest != null) {
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        }

        String name = user.getUsername();
        Long studentId = null;

        if (user.getRole() == Role.STUDENT) {
            Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
            if (studentOpt.isPresent()) {
                name = studentOpt.get().getName();
                studentId = studentOpt.get().getId();
            }
        } else {
            name = "System Administrator";
        }

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(name)
                .role(user.getRole().name())
                .studentId(studentId)
                .passwordChangeRequired(Boolean.TRUE.equals(user.getPasswordChangeRequired()))
                .message("Login successful")
                .build();
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String name = user.getUsername();
        Long studentId = null;

        if (user.getRole() == Role.STUDENT) {
            Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
            if (studentOpt.isPresent()) {
                name = studentOpt.get().getName();
                studentId = studentOpt.get().getId();
            }
        } else {
            name = "System Administrator";
        }

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(name)
                .role(user.getRole().name())
                .studentId(studentId)
                .passwordChangeRequired(Boolean.TRUE.equals(user.getPasswordChangeRequired()))
                .message("User details retrieved")
                .build();
    }
}
