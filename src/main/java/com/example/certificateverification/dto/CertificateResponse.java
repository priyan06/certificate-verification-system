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
public class CertificateResponse {
    private Long id;
    private String certificateNumber;
    private String verificationCode;
    private String verificationUrl;
    private Long studentId;
    private String studentName;
    private String studentRollNumber;
    private String studentCourse;
    private Long templateId;
    private String templateName;
    private String logoPath;
    private String signatoryOneName;
    private String signatoryOneDesignation;
    private String signatoryOneImage;
    private String signatoryTwoName;
    private String signatoryTwoDesignation;
    private String signatoryTwoImage;
    private String certificateTitle;
    private String description;
    private LocalDate issueDate;
    private String status;
    private String qrCodeData;
    private LocalDateTime revokedAt;
    private String revocationReason;
    private LocalDateTime createdAt;

    public String getSignatoryName() {
        return signatoryOneName;
    }

    public String getSignatoryDesignation() {
        return signatoryOneDesignation;
    }
}
