package com.example.certificateverification.controller;

import com.example.certificateverification.dto.VerificationResponse;
import com.example.certificateverification.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/api/public/verify/{verificationCode}")
    public ResponseEntity<VerificationResponse> verifyPublicCode(@PathVariable String verificationCode) {
        return ResponseEntity.ok(verificationService.verifyCertificate(verificationCode));
    }

    @GetMapping("/verify/{verificationCode}")
    public RedirectView redirectToVerifyPage(@PathVariable String verificationCode) {
        return new RedirectView("/verify.html?code=" + verificationCode);
    }
}
