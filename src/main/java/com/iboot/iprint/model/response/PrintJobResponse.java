package com.iboot.iprint.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * CUPS 打印任务响应（来自 CUPS，非持久化）
 */
@Data
@Builder
public class PrintJobResponse {

    private int cupsJobId;

    private String printerName;

    private String jobName;

    private String userName;

    private String state;

    private Integer copies;

    private Integer jobMediaSheetsCompleted;

    private String timeAtCreation;

    private String timeAtProcessing;

    private String timeAtCompleted;
}
