package com.example.certificateverification.service;

import com.example.certificateverification.dto.VerificationResponse;

public interface VerificationService {
    VerificationResponse verifyCertificate(String verificationCode);
}
