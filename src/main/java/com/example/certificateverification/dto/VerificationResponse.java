package com.example.certificateverification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {
    private boolean valid;
    private String status; // ACTIVE, REVOKED, INVALID
    private String message; // "Certificate Verified", "Certificate Revoked", "Invalid Certificate"
    private String verificationCode;
    private String certificateNumber;
    private String studentName;
    private String studentRollNumber;
    private String courseName;
    private String certificateTitle;
    private String logoPath;
    private String signatoryOneName;
    private String signatoryOneDesignation;
    private String signatoryTwoName;
    private String signatoryTwoDesignation;
    private LocalDate issueDate;

    public String getSignatoryName() {
        return signatoryOneName;
    }

    public String getSignatoryDesignation() {
        return signatoryOneDesignation;
    }
}
