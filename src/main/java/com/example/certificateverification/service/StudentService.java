package com.example.certificateverification.service;

import com.example.certificateverification.dto.StudentCreateRequest;
import com.example.certificateverification.dto.StudentResponse;
import com.example.certificateverification.dto.StudentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {
    StudentResponse createStudent(StudentCreateRequest request);
    StudentResponse updateStudent(Long id, StudentUpdateRequest request);
    StudentResponse getStudentById(Long id);
    StudentResponse getStudentByRollNumber(String rollNumber);
    StudentResponse getStudentByUserId(Long userId);
    Page<StudentResponse> getAllStudents(String query, String status, Pageable pageable);
    StudentResponse toggleStudentStatus(Long id);
    String resetPassword(Long id, String newPassword);
}
