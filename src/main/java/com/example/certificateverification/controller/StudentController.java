package com.example.certificateverification.controller;

import com.example.certificateverification.dto.CertificateResponse;
import com.example.certificateverification.dto.StudentResponse;
import com.example.certificateverification.service.CertificateService;
import com.example.certificateverification.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final CertificateService certificateService;

    public StudentController(StudentService studentService, CertificateService certificateService) {
        this.studentService = studentService;
        this.certificateService = certificateService;
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentResponse> getOwnProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        StudentResponse student = studentService.getStudentByRollNumber(authentication.getName());
        return ResponseEntity.ok(student);
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateResponse>> getOwnCertificates(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        StudentResponse student = studentService.getStudentByRollNumber(authentication.getName());
        return ResponseEntity.ok(certificateService.getCertificatesByStudentId(student.getId()));
    }

    @GetMapping("/certificates/{id}")
    public ResponseEntity<CertificateResponse> getOwnCertificateById(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        StudentResponse student = studentService.getStudentByRollNumber(authentication.getName());
        CertificateResponse cert = certificateService.getCertificateById(id);

        if (!cert.getStudentId().equals(student.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(cert);
    }
}
