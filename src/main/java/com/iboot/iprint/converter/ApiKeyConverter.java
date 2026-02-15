package com.iboot.iprint.converter;

import com.iboot.iprint.entity.ApiKey;
import com.iboot.iprint.model.response.ApiKeyResponse;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyConverter {

    public ApiKeyResponse toResponse(ApiKey entity) {
        return ApiKeyResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .apiKey(entity.getApiKey())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
