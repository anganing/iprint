package com.iboot.iprint.controller;

import com.iboot.iprint.common.R;
import com.iboot.iprint.dto.PrintTemplateRequest;
import com.iboot.iprint.dto.PrintTemplateResponse;
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
    public R<List<PrintTemplateResponse>> list() {
        return R.ok(printTemplateService.listAll());
    }

    @GetMapping("/{id}")
    public R<PrintTemplateResponse> get(@PathVariable Long id) {
        return R.ok(printTemplateService.getById(id));
    }

    @PostMapping
    public R<PrintTemplateResponse> create(@RequestBody @Valid PrintTemplateRequest request) {
        return R.ok(printTemplateService.create(request));
    }

    @PutMapping("/{id}")
    public R<PrintTemplateResponse> update(@PathVariable Long id,
                                           @RequestBody @Valid PrintTemplateRequest request) {
        return R.ok(printTemplateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        printTemplateService.delete(id);
        return R.ok();
    }
}
