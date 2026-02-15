package com.iboot.iprint.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PrintTemplateResponse {
    private Long id;
    private String code;
    private String name;
    private String templateData;
    private String printData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
