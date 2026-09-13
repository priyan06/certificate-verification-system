package com.example.certificateverification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "certificate_title", nullable = false)
    private String certificateTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "signatory_one_name", nullable = false)
    private String signatoryOneName;

    @Column(name = "signatory_one_designation", nullable = false)
    private String signatoryOneDesignation;

    @Lob
    @Column(name = "signatory_one_image", columnDefinition = "LONGTEXT")
    private String signatoryOneImage;

    @Column(name = "signatory_two_name")
    private String signatoryTwoName;

    @Column(name = "signatory_two_designation")
    private String signatoryTwoDesignation;

    @Lob
    @Column(name = "signatory_two_image", columnDefinition = "LONGTEXT")
    private String signatoryTwoImage;

    @Lob
    @Column(name = "logo_path", columnDefinition = "LONGTEXT")
    private String logoPath;

    @Column(name = "background_path")
    private String backgroundPath;

    public String getSignatoryName() {
        return signatoryOneName;
    }

    public String getSignatoryDesignation() {
        return signatoryOneDesignation;
    }

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
