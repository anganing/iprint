package com.iboot.iprint.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiKeyResponse {
    private Long id;
    private String name;
    private String apiKey;
    private Integer status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
