package com.iboot.iprint.service.impl;

import com.iboot.iprint.exception.BusinessException;
import com.iboot.iprint.model.response.PrinterResponse;
import com.iboot.iprint.service.PrinterService;
import lombok.extern.slf4j.Slf4j;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintJobAttributes;
import org.cups4j.WhichJobsEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "iprint.cups", name = "enabled", havingValue = "true")
public class CupsPrinterServiceImpl implements PrinterService {

    private final CupsClient cupsClient;

    public CupsPrinterServiceImpl(CupsClient cupsClient) {
        this.cupsClient = cupsClient;
    }

    @Override
    public List<PrinterResponse> listPrinters() {
        try {
            return cupsClient.getPrinters().stream()
                    .map(this::toPrinterResponse)
                    .toList();
        } catch (Exception e) {
            log.error("获取 CUPS 打印机列表失败", e);
            throw new BusinessException("获取打印机列表失败: " + e.getMessage());
        }
    }

    @Override
    public PrinterResponse getPrinterStatus(String name) {
        try {
            CupsPrinter printer = cupsClient.getPrinters().stream()
                    .filter(p -> name.equals(p.getName()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("打印机不存在: " + name));
            return toPrinterResponse(printer);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取打印机 [{}] 状态失败", name, e);
            throw new BusinessException("获取打印机状态失败: " + e.getMessage());
        }
    }

    private PrinterResponse toPrinterResponse(CupsPrinter cupsPrinter) {
        int jobCount = 0;
        String state = "UNKNOWN";
        try {
            List<PrintJobAttributes> jobs = cupsClient.getJobs(cupsPrinter, WhichJobsEnum.NOT_COMPLETED, null, false);
            jobCount = jobs != null ? jobs.size() : 0;
            if (cupsPrinter.getState() != null) {
                state = cupsPrinter.getState().getStateName();
            }
        } catch (Exception e) {
            log.debug("获取打印机任务数失败: {}", e.getMessage());
        }

        return PrinterResponse.builder()
                .name(cupsPrinter.getName())
                .description(cupsPrinter.getDescription())
                .location(cupsPrinter.getLocation())
                .state(state)
                .isDefault(cupsPrinter.isDefault())
                .acceptingJobs(true)
                .jobCount(jobCount)
                .build();
    }
}
