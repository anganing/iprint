package com.iboot.iprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApiKeyRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
