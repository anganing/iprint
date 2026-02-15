package com.iboot.iprint.service;

import com.iboot.iprint.converter.PrintTemplateConverter;
import com.iboot.iprint.exception.BusinessException;
import com.iboot.iprint.model.request.PrintTemplateRequest;
import com.iboot.iprint.model.request.RenderRequest;
import com.iboot.iprint.model.response.PrintTemplateResponse;
import com.iboot.iprint.entity.PrintTemplate;
import com.iboot.iprint.repository.PrintTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import com.iboot.iprint.model.response.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintTemplateService {

    private final PrintTemplateRepository printTemplateRepository;
    private final PrintTemplateConverter printTemplateConverter;
    private final ObjectMapper objectMapper;

    public List<PrintTemplateResponse> listAll() {
        return printTemplateRepository.findAll().stream()
                .map(printTemplateConverter::toResponse)
                .collect(Collectors.toList());
    }

    public PageResult<PrintTemplateResponse> listPage(String keyword, int page, int size) {
        Page<PrintTemplate> pageData = printTemplateRepository.findByKeyword(
                keyword, PageRequest.of(page - 1, size));
        List<PrintTemplateResponse> content = pageData.getContent().stream()
                .map(printTemplateConverter::toResponse)
                .collect(Collectors.toList());
        return PageResult.of(content, pageData.getTotalElements(),
                pageData.getTotalPages(), page, size);
    }

    public PrintTemplateResponse getById(Long id) {
        return printTemplateConverter.toResponse(printTemplateRepository.findById(id)
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
        return printTemplateConverter.toResponse(entity);
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
        return printTemplateConverter.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        PrintTemplate entity = printTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("打印模版不存在"));
        printTemplateRepository.delete(entity);
        log.info("打印模版已删除: id={}", id);
    }

    @SuppressWarnings("unchecked")
    public RenderRequest buildRenderRequest(String code, List<Map<String, Object>> printData) {
        PrintTemplate entity = printTemplateRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("模版不存在: " + code));
        if (entity.getTemplateData() == null || entity.getTemplateData().isBlank()) {
            throw new BusinessException("模版数据为空: " + code);
        }
        Map<String, Object> tplData = objectMapper.readValue(entity.getTemplateData(), Map.class);
        RenderRequest renderRequest = new RenderRequest();
        renderRequest.setTplData(tplData);
        renderRequest.setPrintData(printData);
        return renderRequest;
    }
}
