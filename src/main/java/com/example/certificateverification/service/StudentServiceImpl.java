package com.example.certificateverification.service;

import com.example.certificateverification.dto.StudentCreateRequest;
import com.example.certificateverification.dto.StudentResponse;
import com.example.certificateverification.dto.StudentUpdateRequest;
import com.example.certificateverification.entity.Role;
import com.example.certificateverification.entity.Student;
import com.example.certificateverification.entity.StudentStatus;
import com.example.certificateverification.entity.User;
import com.example.certificateverification.exception.DuplicateResourceException;
import com.example.certificateverification.exception.ResourceNotFoundException;
import com.example.certificateverification.repository.StudentRepository;
import com.example.certificateverification.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        if (studentRepository.existsByRegisterNumber(request.getRegisterNumber())) {
            throw new DuplicateResourceException("A student with this Register Number already exists.");
        }

        String rollNumber = generateNextRollNumber();
        String initialPassword = com.example.certificateverification.util.InitialPasswordGenerator.generate(
                request.getName(), request.getDateOfBirth()
        );

        User user = User.builder()
                .username(rollNumber)
                .password(passwordEncoder.encode(initialPassword))
                .role(Role.STUDENT)
                .enabled(true)
                .passwordChangeRequired(true)
                .build();

        user = userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .rollNumber(rollNumber)
                .registerNumber(request.getRegisterNumber())
                .name(request.getName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .course(request.getCourse())
                .department(request.getDepartment())
                .batch(request.getBatch())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .status(StudentStatus.ACTIVE)
                .build();

        student = studentRepository.save(student);
        return mapToResponse(student);
    }

    private synchronized String generateNextRollNumber() {
        long nextSeq = 1;
        java.util.Optional<String> latest = studentRepository.findLatestRollNumber();
        if (latest.isPresent()) {
            String digits = latest.get().replaceAll("\\D+", "");
            if (!digits.isEmpty()) {
                try {
                    nextSeq = Long.parseLong(digits) + 1;
                } catch (NumberFormatException ignored) {}
            }
        }
        String rollNumber = String.format("STU%04d", nextSeq);
        while (studentRepository.existsByRollNumber(rollNumber)) {
            nextSeq++;
            rollNumber = String.format("STU%04d", nextSeq);
        }
        return rollNumber;
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        if (request.getRegisterNumber() != null && !request.getRegisterNumber().equalsIgnoreCase(student.getRegisterNumber())) {
            if (studentRepository.existsByRegisterNumber(request.getRegisterNumber())) {
                throw new DuplicateResourceException("A student with this Register Number already exists.");
            }
            student.setRegisterNumber(request.getRegisterNumber());
        }

        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setMobileNumber(request.getMobileNumber());
        student.setCourse(request.getCourse());
        student.setDepartment(request.getDepartment());
        student.setBatch(request.getBatch());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAddress(request.getAddress());

        if (request.getStatus() != null) {
            StudentStatus newStatus = StudentStatus.valueOf(request.getStatus().toUpperCase());
            student.setStatus(newStatus);
            student.getUser().setEnabled(newStatus == StudentStatus.ACTIVE);
        }

        student = studentRepository.save(student);
        return mapToResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return mapToResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentByRollNumber(String rollNumber) {
        Student student = studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with roll number: " + rollNumber));
        return mapToResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for user ID: " + userId));
        return mapToResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getAllStudents(String query, String statusStr, Pageable pageable) {
        StudentStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = StudentStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return studentRepository.searchStudents(query, status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public StudentResponse toggleStudentStatus(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        if (student.getStatus() == StudentStatus.ACTIVE) {
            student.setStatus(StudentStatus.INACTIVE);
            student.getUser().setEnabled(false);
        } else {
            student.setStatus(StudentStatus.ACTIVE);
            student.getUser().setEnabled(true);
        }

        student = studentRepository.save(student);
        return mapToResponse(student);
    }

    @Override
    @Transactional
    public String resetPassword(Long id, String newPassword) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        User user = student.getUser();

        String passToSet = newPassword;
        if (passToSet == null || passToSet.isBlank()) {
            passToSet = com.example.certificateverification.util.InitialPasswordGenerator.generate(
                    student.getName(), student.getDateOfBirth()
            );
        }

        user.setPassword(passwordEncoder.encode(passToSet));
        user.setPasswordChangeRequired(true);
        userRepository.save(user);
        return passToSet;
    }

    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .userId(student.getUser() != null ? student.getUser().getId() : null)
                .rollNumber(student.getRollNumber())
                .registerNumber(student.getRegisterNumber())
                .name(student.getName())
                .email(student.getEmail())
                .mobileNumber(student.getMobileNumber())
                .course(student.getCourse())
                .department(student.getDepartment())
                .batch(student.getBatch())
                .dateOfBirth(student.getDateOfBirth())
                .address(student.getAddress())
                .status(student.getStatus().name())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
