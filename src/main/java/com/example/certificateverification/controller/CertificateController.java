package com.example.certificateverification.controller;

import com.example.certificateverification.dto.CertificateResponse;
import com.example.certificateverification.service.CertificateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCertificatePdf(@PathVariable Long id) {
        CertificateResponse cert = certificateService.getCertificateById(id);
        byte[] pdfBytes = certificateService.generateCertificatePdf(id);

        String filename = "Certificate_" + cert.getCertificateNumber() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
