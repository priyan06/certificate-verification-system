package com.example.certificateverification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {
    private Long id;
    private String templateName;
    private String certificateTitle;
    private String description;
    private String logoPath;
    private String signatoryOneName;
    private String signatoryOneDesignation;
    private String signatoryOneImage;
    private String signatoryTwoName;
    private String signatoryTwoDesignation;
    private String signatoryTwoImage;
    private String backgroundPath;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getSignatoryName() {
        return signatoryOneName;
    }

    public String getSignatoryDesignation() {
        return signatoryOneDesignation;
    }
}
