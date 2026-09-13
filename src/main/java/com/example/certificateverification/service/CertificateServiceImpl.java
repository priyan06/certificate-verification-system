package com.example.certificateverification.service;

import com.example.certificateverification.dto.*;
import com.example.certificateverification.entity.*;
import com.example.certificateverification.exception.BadRequestException;
import com.example.certificateverification.exception.CertificateAlreadyRevokedException;
import com.example.certificateverification.exception.ResourceNotFoundException;
import com.example.certificateverification.repository.ActivityLogRepository;
import com.example.certificateverification.repository.CertificateRepository;
import com.example.certificateverification.repository.CertificateTemplateRepository;
import com.example.certificateverification.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final CertificateTemplateRepository templateRepository;
    private final QrCodeService qrCodeService;
    private final PdfService pdfService;
    private final com.example.certificateverification.repository.ActivityLogRepository activityLogRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                  StudentRepository studentRepository,
                                  CertificateTemplateRepository templateRepository,
                                  QrCodeService qrCodeService,
                                  PdfService pdfService,
                                  com.example.certificateverification.repository.ActivityLogRepository activityLogRepository) {
        this.certificateRepository = certificateRepository;
        this.studentRepository = studentRepository;
        this.templateRepository = templateRepository;
        this.qrCodeService = qrCodeService;
        this.pdfService = pdfService;
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    @Transactional
    public CertificateResponse issueCertificate(CertificateIssueRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        CertificateTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + request.getTemplateId()));

        // Generate unique UUID verification token
        String verificationCode = UUID.randomUUID().toString();

        // Generate readable unique certificate number e.g. CERT-2026-000005
        String certificateNumber = request.getCertificateNumber();
        if (certificateNumber == null || certificateNumber.isBlank()) {
            long count = certificateRepository.count() + 1;
            certificateNumber = String.format("CERT-2026-%06d", count);
            while (certificateRepository.existsByCertificateNumber(certificateNumber)) {
                count++;
                certificateNumber = String.format("CERT-2026-%06d", count);
            }
        }

        String title = (request.getCertificateTitle() != null && !request.getCertificateTitle().isBlank())
                ? request.getCertificateTitle()
                : template.getCertificateTitle();

        String desc = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription()
                : template.getDescription();

        String verificationUrl = baseUrl + "/verify.html?code=" + verificationCode;
        String qrBase64 = qrCodeService.generateQrCodeBase64(verificationUrl, 200, 200);

        Certificate certificate = Certificate.builder()
                .certificateNumber(certificateNumber)
                .verificationCode(verificationCode)
                .student(student)
                .template(template)
                .certificateTitle(title)
                .description(desc)
                .issueDate(request.getIssueDate())
                .status(CertificateStatus.ACTIVE)
                .qrCodeData(qrBase64)
                .build();

        certificate = certificateRepository.save(certificate);
        return mapToResponse(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateById(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with ID: " + id));
        return mapToResponse(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateByVerificationCode(String verificationCode) {
        Certificate certificate = certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with code: " + verificationCode));
        return mapToResponse(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CertificateResponse> getAllCertificates(String query, Long templateId, String statusStr, Pageable pageable) {
        CertificateStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = CertificateStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return certificateRepository.searchCertificates(query, templateId, status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> getCertificatesByStudentId(Long studentId) {
        return certificateRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CertificateResponse revokeCertificate(Long id, RevokeCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with ID: " + id));

        if (certificate.getStatus() == CertificateStatus.REVOKED) {
            throw new CertificateAlreadyRevokedException("Certificate is already revoked");
        }

        certificate.setStatus(CertificateStatus.REVOKED);
        certificate.setRevokedAt(LocalDateTime.now());
        certificate.setRevocationReason(request.getReason());

        certificate = certificateRepository.save(certificate);

        // Audit Logging
        String currentUsername = getCurrentUsername();
        activityLogRepository.save(ActivityLog.builder()
                .action("CERTIFICATE_REVOKED")
                .certificateId(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .performedBy(currentUsername)
                .timestamp(LocalDateTime.now())
                .details("Certificate " + certificate.getCertificateNumber() + " revoked")
                .build());

        return mapToResponse(certificate);
    }

    @Override
    @Transactional
    public CertificateResponse reactivateCertificate(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with ID: " + id));

        if (certificate.getStatus() == CertificateStatus.ACTIVE) {
            throw new BadRequestException("Certificate is already active.");
        }

        if (certificate.getStatus() != CertificateStatus.REVOKED) {
            throw new BadRequestException("Only revoked certificates can be reactivated.");
        }

        certificate.setStatus(CertificateStatus.ACTIVE);
        // Clear revokedAt so certificate is no longer marked as revoked, preserving revocationReason for audit
        certificate.setRevokedAt(null);

        certificate = certificateRepository.save(certificate);

        // Audit Logging
        String currentUsername = getCurrentUsername();
        activityLogRepository.save(ActivityLog.builder()
                .action("CERTIFICATE_REACTIVATED")
                .certificateId(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .performedBy(currentUsername)
                .timestamp(LocalDateTime.now())
                .details("Certificate " + certificate.getCertificateNumber() + " reactivated")
                .build());

        return mapToResponse(certificate);
    }

    @Override
    @Transactional
    public CertificateResponse updateCertificateStatus(Long id, CertificateStatusUpdateRequest request) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BadRequestException("Status is required");
        }
        String targetStatus = request.getStatus().trim().toUpperCase();
        if ("ACTIVE".equals(targetStatus)) {
            return reactivateCertificate(id);
        } else if ("REVOKED".equals(targetStatus)) {
            RevokeCertificateRequest revokeReq = new RevokeCertificateRequest();
            revokeReq.setReason(request.getReason() != null ? request.getReason() : "Administrative revocation");
            return revokeCertificate(id, revokeReq);
        } else {
            throw new BadRequestException("Unsupported status: " + request.getStatus());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentActivities() {
        return activityLogRepository.findByOrderByTimestampDesc(org.springframework.data.domain.PageRequest.of(0, 10));
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null && !auth.getName().isBlank())
                ? auth.getName()
                : "ADMIN";
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateCertificatePdf(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with ID: " + id));

        String verificationUrl = baseUrl + "/verify.html?code=" + certificate.getVerificationCode();
        return pdfService.generateCertificatePdf(certificate, verificationUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalStudents = studentRepository.count();
        long totalCertificates = certificateRepository.count();
        long activeCertificates = certificateRepository.countByStatus(CertificateStatus.ACTIVE);
        long revokedCertificates = certificateRepository.countByStatus(CertificateStatus.REVOKED);
        long totalTemplates = templateRepository.count();

        return DashboardStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalCertificates(totalCertificates)
                .totalCertificatesIssued(totalCertificates)
                .activeCertificates(activeCertificates)
                .revokedCertificates(revokedCertificates)
                .totalTemplates(totalTemplates)
                .build();
    }

    private CertificateResponse mapToResponse(Certificate cert) {
        String verificationUrl = baseUrl + "/verify.html?code=" + cert.getVerificationCode();
        return CertificateResponse.builder()
                .id(cert.getId())
                .certificateNumber(cert.getCertificateNumber())
                .verificationCode(cert.getVerificationCode())
                .verificationUrl(verificationUrl)
                .studentId(cert.getStudent().getId())
                .studentName(cert.getStudent().getName())
                .studentRollNumber(cert.getStudent().getRollNumber())
                .studentCourse(cert.getStudent().getCourse())
                .templateId(cert.getTemplate().getId())
                .templateName(cert.getTemplate().getTemplateName())
                .logoPath(cert.getTemplate().getLogoPath())
                .signatoryOneName(cert.getTemplate().getSignatoryOneName())
                .signatoryOneDesignation(cert.getTemplate().getSignatoryOneDesignation())
                .signatoryOneImage(cert.getTemplate().getSignatoryOneImage())
                .signatoryTwoName(cert.getTemplate().getSignatoryTwoName())
                .signatoryTwoDesignation(cert.getTemplate().getSignatoryTwoDesignation())
                .signatoryTwoImage(cert.getTemplate().getSignatoryTwoImage())
                .certificateTitle(cert.getCertificateTitle())
                .description(cert.getDescription())
                .issueDate(cert.getIssueDate())
                .status(cert.getStatus().name())
                .qrCodeData(cert.getQrCodeData())
                .revokedAt(cert.getStatus() == CertificateStatus.REVOKED ? cert.getRevokedAt() : null)
                .revocationReason(cert.getStatus() == CertificateStatus.REVOKED ? cert.getRevocationReason() : null)
                .createdAt(cert.getCreatedAt())
                .build();
    }
}
