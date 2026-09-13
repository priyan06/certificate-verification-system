package com.example.certificateverification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CertificateIssueRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Template ID is required")
    private Long templateId;

    private String certificateTitle;
    private String description;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    private String certificateNumber; // Optional, auto-generated if null
}
