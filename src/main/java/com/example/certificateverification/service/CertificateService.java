package com.example.certificateverification.service;

import com.example.certificateverification.dto.CertificateIssueRequest;
import com.example.certificateverification.dto.CertificateResponse;
import com.example.certificateverification.dto.DashboardStatsResponse;
import com.example.certificateverification.dto.RevokeCertificateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CertificateService {
    CertificateResponse issueCertificate(CertificateIssueRequest request);
    CertificateResponse getCertificateById(Long id);
    CertificateResponse getCertificateByVerificationCode(String verificationCode);
    Page<CertificateResponse> getAllCertificates(String query, Long templateId, String status, Pageable pageable);
    List<CertificateResponse> getCertificatesByStudentId(Long studentId);
    CertificateResponse revokeCertificate(Long id, RevokeCertificateRequest request);
    CertificateResponse reactivateCertificate(Long id);
    CertificateResponse updateCertificateStatus(Long id, com.example.certificateverification.dto.CertificateStatusUpdateRequest request);
    byte[] generateCertificatePdf(Long id);
    DashboardStatsResponse getDashboardStats();
    List<com.example.certificateverification.entity.ActivityLog> getRecentActivities();
}
