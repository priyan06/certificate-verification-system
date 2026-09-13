package com.example.certificateverification.service;

import com.example.certificateverification.dto.VerificationResponse;
import com.example.certificateverification.entity.Certificate;
import com.example.certificateverification.entity.CertificateStatus;
import com.example.certificateverification.repository.CertificateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VerificationServiceImpl implements VerificationService {

    private final CertificateRepository certificateRepository;

    public VerificationServiceImpl(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationResponse verifyCertificate(String verificationCode) {
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return VerificationResponse.builder()
                    .valid(false)
                    .status("INVALID")
                    .message("Invalid Certificate")
                    .build();
        }

        Optional<Certificate> certOpt = certificateRepository.findByVerificationCode(verificationCode.trim());
        if (certOpt.isEmpty()) {
            return VerificationResponse.builder()
                    .valid(false)
                    .status("INVALID")
                    .message("Invalid Certificate")
                    .build();
        }

        Certificate cert = certOpt.get();

        if (cert.getStatus() == CertificateStatus.REVOKED) {
            return VerificationResponse.builder()
                    .valid(false)
                    .status("REVOKED")
                    .message("Certificate Revoked")
                    .verificationCode(cert.getVerificationCode())
                    .certificateNumber(cert.getCertificateNumber())
                    .studentName(cert.getStudent().getName())
                    .studentRollNumber(cert.getStudent().getRollNumber())
                    .courseName(cert.getStudent().getCourse())
                    .certificateTitle(cert.getCertificateTitle())
                    .logoPath(cert.getTemplate().getLogoPath())
                    .issueDate(cert.getIssueDate())
                    .build();
        }

        if (cert.getStatus() == CertificateStatus.ACTIVE) {
            return VerificationResponse.builder()
                    .valid(true)
                    .status("ACTIVE")
                    .message("Certificate Verified")
                    .verificationCode(cert.getVerificationCode())
                    .certificateNumber(cert.getCertificateNumber())
                    .studentName(cert.getStudent().getName())
                    .studentRollNumber(cert.getStudent().getRollNumber())
                    .courseName(cert.getStudent().getCourse())
                    .certificateTitle(cert.getCertificateTitle())
                    .logoPath(cert.getTemplate().getLogoPath())
                    .signatoryOneName(cert.getTemplate().getSignatoryOneName())
                    .signatoryOneDesignation(cert.getTemplate().getSignatoryOneDesignation())
                    .signatoryTwoName(cert.getTemplate().getSignatoryTwoName())
                    .signatoryTwoDesignation(cert.getTemplate().getSignatoryTwoDesignation())
                    .issueDate(cert.getIssueDate())
                    .build();
        }

        return VerificationResponse.builder()
                .valid(false)
                .status("INVALID")
                .message("Invalid Certificate")
                .build();
    }
}
