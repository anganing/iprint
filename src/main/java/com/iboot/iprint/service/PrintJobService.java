package com.iboot.iprint.service;

import com.iboot.iprint.model.response.PageResult;
import com.iboot.iprint.model.response.PrintJobResponse;
import com.iboot.iprint.model.request.SubmitPrintJobRequest;

/**
 * 打印任务服务接口（数据来自 CUPS）
 */
public interface PrintJobService {

    /**
     * 提交打印任务
     */
    PrintJobResponse submitJob(SubmitPrintJobRequest request);

    /**
     * 分页查询打印任务（从 CUPS 获取）
     */
    PageResult<PrintJobResponse> listJobs(String printerName, String status, int page, int size);

    /**
     * 查询任务详情
     */
    PrintJobResponse getJob(String printerName, int cupsJobId);

    /**
     * 取消任务
     */
    void cancelJob(String printerName, int cupsJobId);
}
