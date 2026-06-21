package com.iboot.iprint.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 提交打印任务请求
 */
@Data
public class SubmitPrintJobRequest {

    @NotBlank(message = "模版编码不能为空")
    private String templateCode;

    @NotBlank(message = "打印机名称不能为空")
    private String printerName;

    @NotNull(message = "份数不能为空")
    @Min(value = 1, message = "份数必须大于等于1")
    private Integer copies = 1;

    @NotNull(message = "打印数据不能为空")
    private List<Map<String, Object>> printData;

    private String jobName;
}
