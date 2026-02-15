package com.iboot.iprint.controller;

import com.iboot.iprint.result.ApiResult;
import com.iboot.iprint.model.request.ApiKeyRequest;
import com.iboot.iprint.model.response.ApiKeyResponse;
import com.iboot.iprint.model.response.PageResult;
import com.iboot.iprint.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    public ApiResult<PageResult<ApiKeyResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResult.ok(apiKeyService.listPage(keyword, status, page, size));
    }

    @PostMapping
    public ApiResult<ApiKeyResponse> create(@RequestBody @Valid ApiKeyRequest request) {
        return ApiResult.ok(apiKeyService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<ApiKeyResponse> update(@PathVariable Long id,
                                    @RequestBody @Valid ApiKeyRequest request) {
        return ApiResult.ok(apiKeyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        apiKeyService.delete(id);
        return ApiResult.ok();
    }
}
