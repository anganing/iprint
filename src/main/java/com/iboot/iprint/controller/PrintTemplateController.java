package com.iboot.iprint.controller;

import com.iboot.iprint.result.ApiResult;
import com.iboot.iprint.model.request.PrintTemplateRequest;
import com.iboot.iprint.model.response.PrintTemplateResponse;
import com.iboot.iprint.service.PrintTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class PrintTemplateController {

    private final PrintTemplateService printTemplateService;

    @GetMapping
    public ApiResult<List<PrintTemplateResponse>> list() {
        return ApiResult.ok(printTemplateService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResult<PrintTemplateResponse> get(@PathVariable Long id) {
        return ApiResult.ok(printTemplateService.getById(id));
    }

    @PostMapping
    public ApiResult<PrintTemplateResponse> create(@RequestBody @Valid PrintTemplateRequest request) {
        return ApiResult.ok(printTemplateService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<PrintTemplateResponse> update(@PathVariable Long id,
                                           @RequestBody @Valid PrintTemplateRequest request) {
        return ApiResult.ok(printTemplateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        printTemplateService.delete(id);
        return ApiResult.ok();
    }
}
