package com.iboot.iprint.controller;

import com.iboot.iprint.result.ApiResult;
import com.iboot.iprint.model.response.PrinterResponse;
import com.iboot.iprint.service.PrinterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CUPS 打印机管理接口
 */
@RestController
@RequestMapping("/api/printers")
@RequiredArgsConstructor
public class PrinterController {

    private final PrinterService printerService;

    /**
     * 获取 CUPS 打印机列表
     */
    @GetMapping
    public ApiResult<List<PrinterResponse>> listPrinters() {
        return ApiResult.ok(printerService.listPrinters());
    }

    /**
     * 获取指定打印机状态
     */
    @GetMapping("/{name}")
    public ApiResult<PrinterResponse> getPrinterStatus(@PathVariable String name) {
        return ApiResult.ok(printerService.getPrinterStatus(name));
    }
}
