package com.example.certificateverification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g. CERTIFICATE_REACTIVATED, CERTIFICATE_REVOKED, CERTIFICATE_ISSUED

    @Column(name = "certificate_id")
    private Long certificateId;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;
}
