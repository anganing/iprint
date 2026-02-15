package com.iboot.iprint.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrintTemplateRequest {

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String templateData;

    private String printData;
}
