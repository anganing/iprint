package com.iboot.iprint.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CUPS 打印机响应
 */
@Data
@Builder
public class PrinterResponse {

    private String name;

    private String description;

    private String location;

    private String state;

    private boolean isDefault;

    private boolean acceptingJobs;

    private long jobCount;
}
