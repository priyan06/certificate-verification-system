package com.example.certificateverification.service;

import com.example.certificateverification.dto.TemplateRequest;
import com.example.certificateverification.dto.TemplateResponse;

import java.util.List;

public interface TemplateService {
    TemplateResponse createTemplate(TemplateRequest request);
    TemplateResponse updateTemplate(Long id, TemplateRequest request);
    TemplateResponse getTemplateById(Long id);
    List<TemplateResponse> getAllTemplates(boolean activeOnly);
    TemplateResponse toggleTemplateStatus(Long id);
}
