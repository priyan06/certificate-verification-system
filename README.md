# Digital Certificate Verification System

A web-based MCA project developed using **Java 17, Spring Boot, MySQL, HTML, CSS, and JavaScript**.

The system allows administrators to manage students, create certificate templates, issue digital certificates, generate QR codes, revoke/reactivate certificates, and verify certificates publicly using a unique verification URL.

---

## Features

### Admin
- Secure Admin Login
- Dashboard with:
  - Total Students
  - Certificates Issued
  - Active Certificates
  - Revoked Certificates
  - Certificate Templates
- Student Management
- Certificate Template Management
- Issue Certificates
- Download Certificates as PDF
- Revoke Certificates
- Reactivate Revoked Certificates
- Recent Activity
- Certificate Status Overview

### Student
- Student Login
- View Profile
- View Issued Certificates
- Download Certificates
- Change Password

### Public Verification
- Scan QR code from certificate
- Open unique verification URL
- Verify certificate status
- Display:
  - Certificate Verified
  - Certificate Revoked
  - Invalid Certificate

---

## Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security
- MySQL
- Maven

### Frontend
- HTML5
- CSS3
- Vanilla JavaScript
- Fetch API

### Additional Libraries
- ZXing for QR Code Generation
- BCrypt for Password Encryption
- Chart.js for Dashboard Charts
- PDF generation library compatible with Java 17

---

## Project Architecture

```text
src/main/java/com/example/certificateverification/
├── config/
├── controller/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── exception/
├── repository/
├── security/
├── service/
│   └── impl/
└── util/
