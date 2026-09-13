package com.example.certificateverification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalStudents;
    private long totalCertificates;
    private long totalCertificatesIssued;
    private long activeCertificates;
    private long revokedCertificates;
    private long totalTemplates;
}
