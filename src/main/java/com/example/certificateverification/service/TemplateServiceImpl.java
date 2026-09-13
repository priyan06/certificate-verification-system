package com.example.certificateverification.service;

import com.example.certificateverification.dto.TemplateRequest;
import com.example.certificateverification.dto.TemplateResponse;
import com.example.certificateverification.entity.CertificateTemplate;
import com.example.certificateverification.exception.ResourceNotFoundException;
import com.example.certificateverification.repository.CertificateTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemplateServiceImpl implements TemplateService {

    private final CertificateTemplateRepository templateRepository;

    public TemplateServiceImpl(CertificateTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request) {
        CertificateTemplate template = CertificateTemplate.builder()
                .templateName(request.getTemplateName())
                .certificateTitle(request.getCertificateTitle())
                .description(request.getDescription())
                .signatoryOneName(request.getSignatoryOneName())
                .signatoryOneDesignation(request.getSignatoryOneDesignation())
                .signatoryOneImage(request.getSignatoryOneImage())
                .signatoryTwoName(request.getSignatoryTwoName())
                .signatoryTwoDesignation(request.getSignatoryTwoDesignation())
                .signatoryTwoImage(request.getSignatoryTwoImage())
                .logoPath(request.getLogoPath())
                .backgroundPath(request.getBackgroundPath())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    @Override
    @Transactional
    public TemplateResponse updateTemplate(Long id, TemplateRequest request) {
        CertificateTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate template not found with ID: " + id));

        template.setTemplateName(request.getTemplateName());
        template.setCertificateTitle(request.getCertificateTitle());
        template.setDescription(request.getDescription());
        template.setSignatoryOneName(request.getSignatoryOneName());
        template.setSignatoryOneDesignation(request.getSignatoryOneDesignation());
        if (request.getSignatoryOneImage() != null) template.setSignatoryOneImage(request.getSignatoryOneImage());
        template.setSignatoryTwoName(request.getSignatoryTwoName());
        template.setSignatoryTwoDesignation(request.getSignatoryTwoDesignation());
        if (request.getSignatoryTwoImage() != null) template.setSignatoryTwoImage(request.getSignatoryTwoImage());
        if (request.getLogoPath() != null) template.setLogoPath(request.getLogoPath());
        if (request.getBackgroundPath() != null) template.setBackgroundPath(request.getBackgroundPath());
        if (request.getActive() != null) template.setActive(request.getActive());

        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(Long id) {
        CertificateTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate template not found with ID: " + id));
        return mapToResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates(boolean activeOnly) {
        List<CertificateTemplate> list = activeOnly
                ? templateRepository.findByActiveTrue()
                : templateRepository.findAll();
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TemplateResponse toggleTemplateStatus(Long id) {
        CertificateTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate template not found with ID: " + id));
        template.setActive(!template.getActive());
        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    private TemplateResponse mapToResponse(CertificateTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .certificateTitle(template.getCertificateTitle())
                .description(template.getDescription())
                .signatoryOneName(template.getSignatoryOneName())
                .signatoryOneDesignation(template.getSignatoryOneDesignation())
                .signatoryOneImage(template.getSignatoryOneImage())
                .signatoryTwoName(template.getSignatoryTwoName())
                .signatoryTwoDesignation(template.getSignatoryTwoDesignation())
                .signatoryTwoImage(template.getSignatoryTwoImage())
                .logoPath(template.getLogoPath())
                .backgroundPath(template.getBackgroundPath())
                .active(template.getActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
