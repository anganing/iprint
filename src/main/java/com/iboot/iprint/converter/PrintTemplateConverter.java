package com.iboot.iprint.converter;

import com.iboot.iprint.entity.PrintTemplate;
import com.iboot.iprint.model.response.PrintTemplateResponse;
import org.springframework.stereotype.Component;

@Component
public class PrintTemplateConverter {

    public PrintTemplateResponse toResponse(PrintTemplate entity) {
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
