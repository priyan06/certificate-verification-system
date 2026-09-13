package com.example.certificateverification.repository;

import com.example.certificateverification.entity.CertificateTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {
    List<CertificateTemplate> findByActiveTrue();
    long countByActiveTrue();
}
