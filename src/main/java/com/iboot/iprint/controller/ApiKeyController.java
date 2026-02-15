package com.iboot.iprint.controller;

import com.iboot.iprint.common.R;
import com.iboot.iprint.dto.ApiKeyRequest;
import com.iboot.iprint.dto.ApiKeyResponse;
import com.iboot.iprint.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    public R<List<ApiKeyResponse>> list() {
        return R.ok(apiKeyService.listAll());
    }

    @PostMapping
    public R<ApiKeyResponse> create(@RequestBody @Valid ApiKeyRequest request) {
        return R.ok(apiKeyService.create(request));
    }

    @PutMapping("/{id}")
    public R<ApiKeyResponse> update(@PathVariable Long id,
                                    @RequestBody @Valid ApiKeyRequest request) {
        return R.ok(apiKeyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        apiKeyService.delete(id);
        return R.ok();
    }
}
