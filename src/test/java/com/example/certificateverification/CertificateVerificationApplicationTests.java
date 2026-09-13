package com.example.certificateverification;

import com.example.certificateverification.dto.*;
import com.example.certificateverification.entity.CertificateStatus;
import com.example.certificateverification.exception.DuplicateResourceException;
import com.example.certificateverification.service.CertificateService;
import com.example.certificateverification.service.StudentService;
import com.example.certificateverification.service.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CertificateVerificationApplicationTests {

    @Autowired
    private StudentService studentService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private com.example.certificateverification.service.PdfService pdfService;

    @Autowired
    private com.example.certificateverification.service.TemplateService templateService;

    @Autowired
    private com.example.certificateverification.repository.ActivityLogRepository activityLogRepository;

    @Test
    @DisplayName("1. Student Creation & Auto Roll Number Test")
    void testCreateStudentSuccess() {
        StudentCreateRequest request = new StudentCreateRequest();
        String uniqueReg = "REG_" + System.currentTimeMillis();
        request.setRegisterNumber(uniqueReg);
        request.setName("Priyan S");
        request.setEmail("priyan." + System.currentTimeMillis() + "@example.com");
        request.setCourse("MCA");
        request.setDateOfBirth(LocalDate.of(2002, 6, 19));

        StudentResponse response = studentService.createStudent(request);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getRollNumber());
        assertTrue(response.getRollNumber().startsWith("STU"));
        assertEquals(uniqueReg, response.getRegisterNumber());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    @DisplayName("2. Duplicate Register Number Check Test")
    void testDuplicateRegisterNumberThrowsException() {
        StudentCreateRequest request = new StudentCreateRequest();
        request.setRegisterNumber("REG2024001"); // Exists in data.sql seed
        request.setName("Duplicate Register Student");
        request.setEmail("dup@example.com");
        request.setCourse("MCA");
        request.setDateOfBirth(LocalDate.of(2003, 1, 1));

        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(request));
    }

    @Test
    @DisplayName("2b. Initial Password Generator Test")
    void testInitialPasswordGenerator() {
        assertEquals("Priy19062002", com.example.certificateverification.util.InitialPasswordGenerator.generate("Priyan S", LocalDate.of(2002, 6, 19)));
        assertEquals("AKum19062002", com.example.certificateverification.util.InitialPasswordGenerator.generate("A. Kumar", LocalDate.of(2002, 6, 19)));
        assertEquals("Ram01012003", com.example.certificateverification.util.InitialPasswordGenerator.generate("Ram", LocalDate.of(2003, 1, 1)));
    }

    @Test
    @DisplayName("3. Certificate Creation & Number Generation Test")
    void testCertificateCreationAndNumberGeneration() {
        CertificateIssueRequest request = new CertificateIssueRequest();
        request.setStudentId(1L); // Seed student ID 1
        request.setTemplateId(1L); // Seed template ID 1
        request.setCertificateTitle("CERTIFICATE OF EXCELLENCE");
        request.setDescription("Achieved highest distinction in MCA project.");
        request.setIssueDate(LocalDate.now());

        CertificateResponse cert = certificateService.issueCertificate(request);
        assertNotNull(cert);
        assertNotNull(cert.getCertificateNumber());
        assertTrue(cert.getCertificateNumber().startsWith("CERT-2026-"));
        assertNotNull(cert.getVerificationCode());
        assertEquals("ACTIVE", cert.getStatus());
    }

    @Test
    @DisplayName("4. Verification Code UUID Format Test")
    void testVerificationCodeIsUUID() {
        CertificateIssueRequest request = new CertificateIssueRequest();
        request.setStudentId(1L);
        request.setTemplateId(1L);
        request.setCertificateTitle("TEST TITLE");
        request.setIssueDate(LocalDate.now());

        CertificateResponse cert = certificateService.issueCertificate(request);
        assertDoesNotThrow(() -> UUID.fromString(cert.getVerificationCode()));
    }

    @Test
    @DisplayName("5. Valid Certificate Verification Test")
    void testValidCertificateVerification() {
        // Code from seed data.sql: 31f8b76d-1234-4567-89ab-cdef01234567
        VerificationResponse response = verificationService.verifyCertificate("31f8b76d-1234-4567-89ab-cdef01234567");
        assertTrue(response.isValid());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("Certificate Verified", response.getMessage());
        assertEquals("Rahul Sharma", response.getStudentName());
    }

    @Test
    @DisplayName("6. Invalid Certificate Verification Test")
    void testInvalidCertificateVerification() {
        VerificationResponse response = verificationService.verifyCertificate("NON-EXISTENT-UUID-TOKEN-12345");
        assertFalse(response.isValid());
        assertEquals("INVALID", response.getStatus());
        assertEquals("Invalid Certificate", response.getMessage());
    }

    @Test
    @DisplayName("7. Revoked Certificate Verification Test")
    void testRevokedCertificateVerification() {
        // Code from seed data.sql: 7c9e6679-7425-40de-944b-e07fc1f90ae7 (Status: REVOKED)
        VerificationResponse response = verificationService.verifyCertificate("7c9e6679-7425-40de-944b-e07fc1f90ae7");
        assertFalse(response.isValid());
        assertEquals("REVOKED", response.getStatus());
        assertEquals("Certificate Revoked", response.getMessage());
        assertNotNull(response.getCertificateNumber());
    }

    @Test
    @DisplayName("8. Student Certificate Scope Test")
    void testStudentCertificateFetch() {
        var certs = certificateService.getCertificatesByStudentId(1L);
        assertNotNull(certs);
        assertFalse(certs.isEmpty());
    }

    @Test
    @DisplayName("9. Template Creation with Dual Signatories Test")
    void testCreateTemplateWithDualSignatories() {
        TemplateRequest request = new TemplateRequest();
        request.setTemplateName("Internship Completion");
        request.setCertificateTitle("INTERNSHIP COMPLETION CERTIFICATE");
        request.setDescription("Successfully completed 6-month software development internship.");
        request.setLogoPath("/images/default-logo.png");
        request.setSignatoryOneName("Thomasine Mosley");
        request.setSignatoryOneDesignation("Chief Ecologist");
        request.setSignatoryTwoName("Willa Payne");
        request.setSignatoryTwoDesignation("Company Director");
        request.setActive(true);

        TemplateResponse response = templateService.createTemplate(request);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("INTERNSHIP COMPLETION CERTIFICATE", response.getCertificateTitle());
        assertEquals("Thomasine Mosley", response.getSignatoryOneName());
        assertEquals("Willa Payne", response.getSignatoryTwoName());
    }

    @Test
    @DisplayName("10. Dynamic Certificate PDF Generation Test")
    void testDynamicCertificatePdfGeneration() {
        // Issue certificate with custom dynamic title
        CertificateIssueRequest request = new CertificateIssueRequest();
        request.setStudentId(1L);
        request.setTemplateId(1L);
        request.setCertificateTitle("ADVANCED AI SPECIALIZATION CERTIFICATE");
        request.setDescription("Completed advanced curriculum in Machine Learning.");
        request.setIssueDate(LocalDate.of(2026, 9, 13));

        CertificateResponse issued = certificateService.issueCertificate(request);
        assertNotNull(issued);

        com.example.certificateverification.entity.Certificate certEntity =
                com.example.certificateverification.entity.Certificate.builder()
                        .certificateNumber(issued.getCertificateNumber())
                        .verificationCode(issued.getVerificationCode())
                        .student(com.example.certificateverification.entity.Student.builder()
                                .name("PRIYAN S")
                                .rollNumber("STU1001")
                                .registerNumber("REG2024001")
                                .course("MCA")
                                .build())
                        .template(com.example.certificateverification.entity.CertificateTemplate.builder()
                                .templateName("Specialization Template")
                                .certificateTitle(issued.getCertificateTitle())
                                .description(issued.getDescription())
                                .signatoryOneName("Thomasine Mosley")
                                .signatoryOneDesignation("Chief Ecologist")
                                .signatoryTwoName("Willa Payne")
                                .signatoryTwoDesignation("Company Director")
                                .logoPath("/images/default-logo.png")
                                .active(true)
                                .build())
                        .certificateTitle(issued.getCertificateTitle())
                        .description(issued.getDescription())
                        .issueDate(issued.getIssueDate())
                        .status(CertificateStatus.ACTIVE)
                        .build();

        byte[] pdfBytes = pdfService.generateCertificatePdf(certEntity, issued.getVerificationUrl());
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);
        // Validate PDF signature "%PDF-"
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
        assertEquals('-', (char) pdfBytes[4]);
    }

    @Test
    @DisplayName("11. Reactivate Revoked Certificate & Verification Test")
    void testReactivateRevokedCertificateSuccess() {
        // Seed certificate 3 is REVOKED
        CertificateResponse certBefore = certificateService.getCertificateById(3L);
        assertEquals("REVOKED", certBefore.getStatus());

        // Verify public verification shows revoked before reactivation
        VerificationResponse verifyBefore = verificationService.verifyCertificate(certBefore.getVerificationCode());
        assertFalse(verifyBefore.isValid());
        assertEquals("REVOKED", verifyBefore.getStatus());
        assertEquals("Certificate Revoked", verifyBefore.getMessage());

        // Reactivate certificate
        CertificateResponse reactivated = certificateService.reactivateCertificate(3L);
        assertNotNull(reactivated);
        assertEquals("ACTIVE", reactivated.getStatus());
        assertNull(reactivated.getRevokedAt());

        // Verify public verification immediately returns valid and active
        VerificationResponse verifyAfter = verificationService.verifyCertificate(certBefore.getVerificationCode());
        assertTrue(verifyAfter.isValid());
        assertEquals("ACTIVE", verifyAfter.getStatus());
        assertEquals("Certificate Verified", verifyAfter.getMessage());

        // Verify audit log has CERTIFICATE_REACTIVATED entry
        var logs = activityLogRepository.findAll();
        boolean hasReactivatedLog = logs.stream()
                .anyMatch(l -> "CERTIFICATE_REACTIVATED".equals(l.getAction()) && "CERT-2026-000003".equals(l.getCertificateNumber()));
        assertTrue(hasReactivatedLog, "Audit log should contain CERTIFICATE_REACTIVATED action for CERT-2026-000003");
    }

    @Test
    @DisplayName("12. Reactivate Already Active Certificate Throws BadRequestException")
    void testReactivateAlreadyActiveCertificateThrowsBadRequest() {
        // Seed certificate 1 is ACTIVE
        CertificateResponse cert = certificateService.getCertificateById(1L);
        assertEquals("ACTIVE", cert.getStatus());

        com.example.certificateverification.exception.BadRequestException ex =
                assertThrows(com.example.certificateverification.exception.BadRequestException.class,
                        () -> certificateService.reactivateCertificate(1L));

        assertEquals("Certificate is already active.", ex.getMessage());
    }

    @Test
    @DisplayName("13. Patch Status Endpoint Test")
    void testUpdateCertificateStatusPatch() {
        // Certificate 3 is REVOKED -> patch to ACTIVE
        CertificateStatusUpdateRequest request = CertificateStatusUpdateRequest.builder()
                .status("ACTIVE")
                .build();

        CertificateResponse response = certificateService.updateCertificateStatus(3L, request);
        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
    }
}
