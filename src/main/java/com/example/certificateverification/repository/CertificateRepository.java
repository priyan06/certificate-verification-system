package com.example.certificateverification.repository;

import com.example.certificateverification.entity.Certificate;
import com.example.certificateverification.entity.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByVerificationCode(String verificationCode);
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    boolean existsByCertificateNumber(String certificateNumber);
    boolean existsByVerificationCode(String verificationCode);

    List<Certificate> findByStudentId(Long studentId);
    long countByStudentId(Long studentId);
    long countByStatus(CertificateStatus status);

    @Query("SELECT c FROM Certificate c WHERE " +
           "(:query IS NULL OR LOWER(c.student.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.student.rollNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.certificateNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.certificateTitle) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:templateId IS NULL OR c.template.id = :templateId) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<Certificate> searchCertificates(@Param("query") String query,
                                         @Param("templateId") Long templateId,
                                         @Param("status") CertificateStatus status,
                                         Pageable pageable);
}
