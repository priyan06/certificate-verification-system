package com.example.certificateverification.repository;

import com.example.certificateverification.entity.Student;
import com.example.certificateverification.entity.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByRegisterNumber(String registerNumber);
    Optional<Student> findByUserId(Long userId);
    boolean existsByRollNumber(String rollNumber);
    boolean existsByRegisterNumber(String registerNumber);
    boolean existsByEmail(String email);

    @Query(value = "SELECT roll_number FROM students WHERE roll_number LIKE 'STU%' ORDER BY LENGTH(roll_number) DESC, roll_number DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestRollNumber();

    @Query("SELECT s FROM Student s WHERE " +
           "(:query IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.registerNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<Student> searchStudents(@Param("query") String query, @Param("status") StudentStatus status, Pageable pageable);

    long countByStatus(StudentStatus status);
}
