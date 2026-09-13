package com.example.certificateverification.config;

import com.example.certificateverification.entity.User;
import com.example.certificateverification.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Ensure default passwords are standard BCrypt encoded
        userRepository.findByUsername("admin@cert.com").ifPresent(user -> {
            user.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(user);
        });

        userRepository.findByUsername("STU1001").ifPresent(user -> {
            user.setPassword(passwordEncoder.encode("student123"));
            userRepository.save(user);
        });

        userRepository.findByUsername("STU1002").ifPresent(user -> {
            user.setPassword(passwordEncoder.encode("student123"));
            userRepository.save(user);
        });

        userRepository.findByUsername("STU1003").ifPresent(user -> {
            user.setPassword(passwordEncoder.encode("student123"));
            userRepository.save(user);
        });
    }
}
