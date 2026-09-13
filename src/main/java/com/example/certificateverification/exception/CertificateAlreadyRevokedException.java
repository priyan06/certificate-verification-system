package com.example.certificateverification.exception;

public class CertificateAlreadyRevokedException extends RuntimeException {
    public CertificateAlreadyRevokedException(String message) {
        super(message);
    }
}
