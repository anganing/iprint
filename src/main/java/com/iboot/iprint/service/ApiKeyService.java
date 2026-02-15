package com.iboot.iprint.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.iboot.iprint.common.BusinessException;
import com.iboot.iprint.dto.ApiKeyRequest;
import com.iboot.iprint.dto.ApiKeyResponse;
import com.iboot.iprint.entity.ApiKey;
import com.iboot.iprint.enums.ApiKeyStatus;
import com.iboot.iprint.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final Cache<String, Boolean> apiKeyCache;

    public List<ApiKeyResponse> listAll() {
        return apiKeyRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApiKeyResponse create(ApiKeyRequest request) {
        String key = generateKey();
        ApiKey entity = ApiKey.builder()
                .name(request.getName())
                .apiKey(key)
                .status(request.getStatus())
                .description(request.getDescription())
                .build();
        apiKeyRepository.save(entity);

        // 立即更新缓存，确保实时生效
        if (entity.getStatus().equals(ApiKeyStatus.ACTIVE.getCode())) {
            apiKeyCache.put(key, true);
        }

        log.info("API Key 已创建: name={}, key={}...", entity.getName(),
                key.substring(0, 8));
        return toResponse(entity);
    }

    @Transactional
    public ApiKeyResponse update(Long id, ApiKeyRequest request) {
        ApiKey entity = apiKeyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("API Key 不存在"));

        String keyStr = entity.getApiKey();
        entity.setName(request.getName());
        entity.setStatus(request.getStatus());
        entity.setDescription(request.getDescription());
        apiKeyRepository.save(entity);

        // 先失效旧缓存，再根据新状态写入
        apiKeyCache.invalidate(keyStr);
        if (entity.getStatus().equals(ApiKeyStatus.ACTIVE.getCode())) {
            apiKeyCache.put(keyStr, true);
        }

        log.info("API Key 已更新: id={}", id);
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        ApiKey entity = apiKeyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("API Key 不存在"));

        // 先从缓存移除，再删除数据库记录
        apiKeyCache.invalidate(entity.getApiKey());
        apiKeyRepository.delete(entity);

        log.info("API Key 已删除: id={}", id);
    }

    private String generateKey() {
        String key;
        do {
            key = UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        } while (apiKeyRepository.existsByApiKey(key));
        return key;
    }

    private ApiKeyResponse toResponse(ApiKey entity) {
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
