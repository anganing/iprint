package com.iboot.iprint.service.impl;

import com.iboot.iprint.exception.BusinessException;
import com.iboot.iprint.model.request.RenderRequest;
import com.iboot.iprint.model.request.SubmitPrintJobRequest;
import com.iboot.iprint.model.response.PageResult;
import com.iboot.iprint.model.response.PrintJobResponse;
import com.iboot.iprint.service.HiprintRenderService;
import com.iboot.iprint.service.PrintJobService;
import com.iboot.iprint.service.PrintTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintJob;
import org.cups4j.PrintJobAttributes;
import org.cups4j.PrintRequestResult;
import org.cups4j.WhichJobsEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "iprint.cups", name = "enabled", havingValue = "true")
public class CupsPrintJobServiceImpl implements PrintJobService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final PrintTemplateService printTemplateService;
    private final HiprintRenderService hiprintRenderService;
    private final CupsClient cupsClient;

    public CupsPrintJobServiceImpl(PrintTemplateService printTemplateService,
                                   HiprintRenderService hiprintRenderService,
                                   CupsClient cupsClient) {
        this.printTemplateService = printTemplateService;
        this.hiprintRenderService = hiprintRenderService;
        this.cupsClient = cupsClient;
    }

    @Override
    public PrintJobResponse submitJob(SubmitPrintJobRequest request) {
        try {
            // 1. 渲染 PDF
            RenderRequest renderRequest = printTemplateService.buildRenderRequest(
                    request.getTemplateCode(), request.getPrintData());
            File pdfFile = hiprintRenderService.generatePdfByHtmlToPdf(renderRequest);

            try {
                // 2. 获取打印机
                CupsPrinter cupsPrinter = cupsClient.getPrinters().stream()
                        .filter(p -> request.getPrinterName().equals(p.getName()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("打印机不存在: " + request.getPrinterName()));

                // 3. 提交打印
                PrintJob cupsPrintJob = new PrintJob.Builder(new java.io.FileInputStream(pdfFile))
                        .copies(Optional.ofNullable(request.getCopies()).orElse(1))
                        .jobName(StringUtils.hasText(request.getJobName())
                                ? request.getJobName()
                                : ("print-" + System.currentTimeMillis()))
                        .build();

                PrintRequestResult result = cupsPrinter.print(cupsPrintJob);

                // 4. 返回任务信息
                return PrintJobResponse.builder()
                        .cupsJobId(result.getJobId())
                        .printerName(request.getPrinterName())
                        .copies(Optional.ofNullable(request.getCopies()).orElse(1))
                        .jobName(StringUtils.hasText(request.getJobName())
                                ? request.getJobName()
                                : ("print-" + System.currentTimeMillis()))
                        .state(result.isSuccessfulResult() ? "SUBMITTED" : "FAILED")
                        .build();

            } finally {
                // 5. 删除临时 PDF 文件
                if (pdfFile.exists() && !pdfFile.delete()) {
                    log.warn("PDF 临时文件删除失败: {}", pdfFile.getAbsolutePath());
                }
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("打印任务提交失败: templateCode={}", request.getTemplateCode(), e);
            throw new BusinessException("打印任务提交失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<PrintJobResponse> listJobs(String printerName, String status, int page, int size) {
        try {
            List<PrintJobResponse> allJobs = new ArrayList<>();

            for (CupsPrinter cupsPrinter : cupsClient.getPrinters()) {
                // 按打印机过滤
                if (StringUtils.hasText(printerName)
                        && !cupsPrinter.getName().equals(printerName)) {
                    continue;
                }

                List<PrintJobAttributes> cupsJobs = cupsClient.getJobs(cupsPrinter, WhichJobsEnum.ALL, null, false);
                if (cupsJobs == null || cupsJobs.isEmpty()) {
                    continue;
                }

                for (PrintJobAttributes cupsJob : cupsJobs) {
                    // 按状态过滤
                    if (StringUtils.hasText(status) && cupsJob.getJobState() != null
                            && !status.equalsIgnoreCase(cupsJob.getJobState().toString())) {
                        continue;
                    }
                    allJobs.add(toResponse(cupsJob, cupsPrinter.getName()));
                }
            }

            // 分页
            int total = allJobs.size();
            int totalPages = (int) Math.ceil((double) total / size);
            int from = (page - 1) * size;
            int to = Math.min(from + size, total);

            List<PrintJobResponse> pageContent;
            if (from < total) {
                pageContent = allJobs.subList(from, to);
            } else {
                pageContent = List.of();
            }

            return PageResult.of(pageContent, total, totalPages, page, size);

        } catch (Exception e) {
            log.error("获取 CUPS 打印任务列表失败", e);
            return PageResult.of(List.of(), 0, 0, page, size);
        }
    }

    @Override
    public PrintJobResponse getJob(String printerName, int cupsJobId) {
        try {
            CupsPrinter cupsPrinter = cupsClient.getPrinters().stream()
                    .filter(p -> !StringUtils.hasText(printerName) || printerName.equals(p.getName()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("未找到打印机: " + printerName));

            List<PrintJobAttributes> cupsJobs = cupsClient.getJobs(cupsPrinter, WhichJobsEnum.ALL, null, false);
            return cupsJobs.stream()
                    .filter(j -> j.getJobID() == cupsJobId)
                    .findFirst()
                    .map(j -> toResponse(j, cupsPrinter.getName()))
                    .orElseThrow(() -> new BusinessException("任务不存在: cupsJobId=" + cupsJobId));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取任务详情失败: cupsJobId={}", cupsJobId, e);
            throw new BusinessException("获取任务详情失败: " + e.getMessage());
        }
    }

    @Override
    public void cancelJob(String printerName, int cupsJobId) {
        try {
            CupsPrinter cupsPrinter = cupsClient.getPrinters().stream()
                    .filter(p -> p.getName().equals(printerName))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("未找到打印机: " + printerName));

            cupsClient.cancelJob(cupsPrinter, cupsJobId);
            log.info("取消打印任务: printer={}, jobId={}", printerName, cupsJobId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消任务失败: cupsJobId={}", cupsJobId, e);
            throw new BusinessException("取消任务失败: " + e.getMessage());
        }
    }

    private PrintJobResponse toResponse(PrintJobAttributes cupsJob, String printerName) {
        return PrintJobResponse.builder()
                .cupsJobId(cupsJob.getJobID())
                .printerName(printerName)
                .jobName(cupsJob.getJobName())
                .userName(cupsJob.getUserName())
                .state(cupsJob.getJobState() != null ? cupsJob.getJobState().toString() : null)
                .copies(1)
                .jobMediaSheetsCompleted(cupsJob.getPagesPrinted())
                .timeAtCreation(cupsJob.getJobCreateTime() != null ? DATE_FORMAT.format(cupsJob.getJobCreateTime()) : null)
                .timeAtCompleted(cupsJob.getJobCompleteTime() != null ? DATE_FORMAT.format(cupsJob.getJobCompleteTime()) : null)
                .build();
    }
}
