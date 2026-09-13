package com.example.certificateverification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateRequest {
    @NotBlank(message = "Template name is required")
    private String templateName;

    @NotBlank(message = "Certificate title is required")
    private String certificateTitle;

    private String description;

    private String logoPath;

    @NotBlank(message = "Signatory 1 name is required")
    private String signatoryOneName;

    @NotBlank(message = "Signatory 1 role/designation is required")
    private String signatoryOneDesignation;

    private String signatoryOneImage;

    private String signatoryTwoName;
    private String signatoryTwoDesignation;
    private String signatoryTwoImage;

    private String backgroundPath;
    private Boolean active;
}
