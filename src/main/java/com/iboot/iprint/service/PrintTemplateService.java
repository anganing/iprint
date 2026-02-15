package com.iboot.iprint.service;

import com.iboot.iprint.common.BusinessException;
import com.iboot.iprint.dto.PrintTemplateRequest;
import com.iboot.iprint.dto.PrintTemplateResponse;
import com.iboot.iprint.entity.PrintTemplate;
import com.iboot.iprint.repository.PrintTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintTemplateService {

    private final PrintTemplateRepository printTemplateRepository;

    public List<PrintTemplateResponse> listAll() {
        return printTemplateRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PrintTemplateResponse getById(Long id) {
        return toResponse(printTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("打印模版不存在")));
    }

    @Transactional
    public PrintTemplateResponse create(PrintTemplateRequest request) {
        if (printTemplateRepository.existsByCode(request.getCode())) {
            throw new BusinessException("编码已存在: " + request.getCode());
        }
        PrintTemplate entity = PrintTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .templateData(request.getTemplateData())
                .printData(request.getPrintData())
                .build();
        printTemplateRepository.save(entity);
        log.info("打印模版已创建: code={}", entity.getCode());
        return toResponse(entity);
    }

    @Transactional
    public PrintTemplateResponse update(Long id, PrintTemplateRequest request) {
        PrintTemplate entity = printTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("打印模版不存在"));

        // 编码变更时检查唯一性
        if (!entity.getCode().equals(request.getCode())
                && printTemplateRepository.existsByCode(request.getCode())) {
            throw new BusinessException("编码已存在: " + request.getCode());
        }

        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setTemplateData(request.getTemplateData());
        entity.setPrintData(request.getPrintData());
        printTemplateRepository.save(entity);
        log.info("打印模版已更新: id={}", id);
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        PrintTemplate entity = printTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("打印模版不存在"));
        printTemplateRepository.delete(entity);
        log.info("打印模版已删除: id={}", id);
    }

    private PrintTemplateResponse toResponse(PrintTemplate entity) {
        return PrintTemplateResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .templateData(entity.getTemplateData())
                .printData(entity.getPrintData())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
