package com.iboot.iprint.controller;

import com.iboot.iprint.repository.ApiKeyRepository;
import com.iboot.iprint.repository.PrintTemplateRepository;
import com.iboot.iprint.result.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ApiKeyRepository apiKeyRepository;
    private final PrintTemplateRepository printTemplateRepository;

    @GetMapping("/stats")
    public ApiResult<Map<String, Long>> stats() {
        return ApiResult.ok(Map.of(
                "templateCount", printTemplateRepository.count(),
                "apiKeyCount", apiKeyRepository.count(),
                "apiKeyActive", apiKeyRepository.countByStatus(1),
                "apiKeyDisabled", apiKeyRepository.countByStatus(0)
        ));
    }
}
