package com.example.certificateverification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentCreateRequest {

    @NotBlank(message = "Register number is required")
    private String registerNumber;

    @NotBlank(message = "Full name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String mobileNumber;

    @NotBlank(message = "Course is required")
    private String course;

    private String department;

    private String batch;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    private String address;
}
