package com.example.certificateverification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private Long userId;
    private String rollNumber;
    private String registerNumber;
    private String name;
    private String email;
    private String mobileNumber;
    private String course;
    private String department;
    private String batch;
    private LocalDate dateOfBirth;
    private String address;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
