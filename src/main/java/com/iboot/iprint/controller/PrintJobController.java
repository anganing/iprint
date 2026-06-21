package com.iboot.iprint.controller;

import com.iboot.iprint.result.ApiResult;
import com.iboot.iprint.model.response.PageResult;
import com.iboot.iprint.model.response.PrintJobResponse;
import com.iboot.iprint.model.request.SubmitPrintJobRequest;
import com.iboot.iprint.service.PrintJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 打印任务管理接口（数据来自 CUPS）
 */
@RestController
@RequestMapping("/api/print/jobs")
@RequiredArgsConstructor
public class PrintJobController {

    private final PrintJobService printJobService;

    /**
     * 提交打印任务
     */
    @PostMapping
    public ApiResult<PrintJobResponse> submitJob(@RequestBody @Valid SubmitPrintJobRequest request) {
        return ApiResult.ok(printJobService.submitJob(request));
    }

    /**
     * 分页查询打印任务（从 CUPS 获取）
     */
    @GetMapping
    public ApiResult<PageResult<PrintJobResponse>> listJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String printerName,
            @RequestParam(required = false) String status) {
        return ApiResult.ok(printJobService.listJobs(printerName, status, page, size));
    }

    /**
     * 查询任务详情
     */
    @GetMapping("/{cupsJobId}")
    public ApiResult<PrintJobResponse> getJob(
            @RequestParam(required = true) String printerName,
            @PathVariable int cupsJobId) {
        return ApiResult.ok(printJobService.getJob(printerName, cupsJobId));
    }

    /**
     * 取消任务
     */
    @PostMapping("/{cupsJobId}/cancel")
    public ApiResult<Void> cancelJob(
            @RequestParam(required = true) String printerName,
            @PathVariable int cupsJobId) {
        printJobService.cancelJob(printerName, cupsJobId);
        return ApiResult.ok();
    }
}
