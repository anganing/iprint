package com.iboot.iprint.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TemplateRenderRequest {
    @NotBlank(message = "模版编码不能为空")
    private String code;

    @NotNull(message = "打印数据不能为空")
    private List<Map<String, Object>> printData;
}
