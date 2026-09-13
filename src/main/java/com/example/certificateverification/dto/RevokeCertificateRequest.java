package com.example.certificateverification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RevokeCertificateRequest {
    @NotBlank(message = "Revocation reason is required")
    private String reason;
}
