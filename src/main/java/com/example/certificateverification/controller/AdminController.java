package com.example.certificateverification.controller;

import com.example.certificateverification.dto.*;
import com.example.certificateverification.service.CertificateService;
import com.example.certificateverification.service.StudentService;
import com.example.certificateverification.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StudentService studentService;
    private final TemplateService templateService;
    private final CertificateService certificateService;

    public AdminController(StudentService studentService,
                           TemplateService templateService,
                           CertificateService certificateService) {
        this.studentService = studentService;
        this.templateService = templateService;
        this.certificateService = certificateService;
    }

    // --- Dashboard Stats ---
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(certificateService.getDashboardStats());
    }

    // --- Student Management ---
    @GetMapping("/students")
    public ResponseEntity<Page<StudentResponse>> getStudents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(studentService.getAllStudents(query, status, PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping("/students")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        return ResponseEntity.ok(studentService.createStudent(request));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @PutMapping("/students/{id}/status")
    public ResponseEntity<StudentResponse> toggleStudentStatus(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.toggleStudentStatus(id));
    }

    @PutMapping("/students/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetStudentPassword(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String newPassword = (body != null) ? body.get("newPassword") : null;
        String temporaryPassword = studentService.resetPassword(id, newPassword);
        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully",
                "temporaryPassword", temporaryPassword
        ));
    }

    // --- Certificate Template Management ---
    @GetMapping("/templates")
    public ResponseEntity<List<TemplateResponse>> getTemplates(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(templateService.getAllTemplates(activeOnly));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<TemplateResponse> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @PostMapping("/templates")
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.ok(templateService.createTemplate(request));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(@PathVariable Long id, @Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    @PutMapping("/templates/{id}/status")
    public ResponseEntity<TemplateResponse> toggleTemplateStatus(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.toggleTemplateStatus(id));
    }

    // --- Certificate Management & Issuance ---
    @GetMapping("/certificates")
    public ResponseEntity<Page<CertificateResponse>> getCertificates(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(certificateService.getAllCertificates(query, templateId, status, PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/certificates/{id}")
    public ResponseEntity<CertificateResponse> getCertificateById(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.getCertificateById(id));
    }

    @PostMapping("/certificates")
    public ResponseEntity<CertificateResponse> issueCertificate(@Valid @RequestBody CertificateIssueRequest request) {
        return ResponseEntity.ok(certificateService.issueCertificate(request));
    }

    @PutMapping("/certificates/{id}/revoke")
    public ResponseEntity<CertificateResponse> revokeCertificate(@PathVariable Long id, @Valid @RequestBody RevokeCertificateRequest request) {
        return ResponseEntity.ok(certificateService.revokeCertificate(id, request));
    }

    @PutMapping("/certificates/{id}/activate")
    public ResponseEntity<CertificateResponse> reactivateCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.reactivateCertificate(id));
    }

    @PatchMapping("/certificates/{id}/status")
    public ResponseEntity<CertificateResponse> updateCertificateStatus(
            @PathVariable Long id,
            @Valid @RequestBody com.example.certificateverification.dto.CertificateStatusUpdateRequest request) {
        return ResponseEntity.ok(certificateService.updateCertificateStatus(id, request));
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<com.example.certificateverification.entity.ActivityLog>> getRecentActivity() {
        return ResponseEntity.ok(certificateService.getRecentActivities());
    }
}
