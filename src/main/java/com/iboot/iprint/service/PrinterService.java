package com.iboot.iprint.service;

import com.iboot.iprint.model.response.PrinterResponse;

import java.util.List;

/**
 * CUPS 打印机服务接口
 */
public interface PrinterService {

    /**
     * 获取 CUPS 打印机列表
     */
    List<PrinterResponse> listPrinters();

    /**
     * 获取指定打印机状态
     */
    PrinterResponse getPrinterStatus(String name);
}
