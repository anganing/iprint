/*
 * Proprietary Software License
 *
 * Copyright (c) 2025 iboot
 *
 * This software and its associated documentation ("Software") are proprietary property of iboot.
 * Without explicit written permission from iboot, no individual or entity may:
 *
 * 1. Copy, modify, merge, publish, distribute, sublicense, or sell copies of the Software;
 * 2. Reverse engineer, decompile, or disassemble the Software;
 * 3. Remove or alter any copyright notices or other proprietary markings in the Software;
 * 4. Use the Software for any commercial purposes.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * IBOOT BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * The Software may not be used without explicit written permission from iboot.
 * Author: tangsc.
 */

package com.iboot.iprint.util;

import com.iboot.iprint.common.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * wkhtmltopdf 命令行工具封装类。
 *
 * <p>直接调用系统安装的 wkhtmltopdf 命令生成 PDF。
 */
@Slf4j
public class WkhtmltopdfUtil {

    private static final String WKHTMLTOPDF_CMD = "wkhtmltopdf";
    private static final int DEFAULT_TIMEOUT_SECONDS = 6000;

    /**
     * 将 HTML 字符串转换为 PDF 文件。
     *
     * @param html       HTML 内容
     * @param outputPath PDF 输出路径
     * @param options    可选的 wkhtmltopdf 参数
     */
    public static void htmlToPdf(String html, Path outputPath, PdfOptions options) {
        Path tempHtmlFile = null;
        try {
            // 创建临时 HTML 文件
            tempHtmlFile = Files.createTempFile("wkhtml_", ".html");
            Files.writeString(tempHtmlFile, html, StandardCharsets.UTF_8);

            // 构建命令
            List<String> command = buildCommand(tempHtmlFile, outputPath, options);
            log.debug("执行 wkhtmltopdf 命令: {}", String.join(" ", command));

            // 执行命令
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待进程完成
            boolean finished = process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("wkhtmltopdf 执行超时");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("wkhtmltopdf 执行失败，退出码: {}，输出: {}", exitCode, output);
                throw new BusinessException("wkhtmltopdf 执行失败，退出码: " + exitCode);
            }

            log.debug("wkhtmltopdf 执行成功，输出文件: {}", outputPath);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BusinessException("wkhtmltopdf 执行异常");
        } finally {
            // 清理临时文件
            if (tempHtmlFile != null) {
                try {
                    Files.deleteIfExists(tempHtmlFile);
                } catch (IOException e) {
                    log.warn("删除临时 HTML 文件失败: {}", tempHtmlFile, e);
                }
            }
        }
    }

    /**
     * 使用默认选项将 HTML 转换为 PDF。
     */
    public static void htmlToPdf(String html, Path outputPath) {
        htmlToPdf(html, outputPath, PdfOptions.defaults());
    }

    private static List<String> buildCommand(Path inputHtml, Path outputPdf, PdfOptions options) {
        List<String> cmd = new ArrayList<>();
        cmd.add(WKHTMLTOPDF_CMD);

        // 基础参数
        cmd.add("--encoding");
        cmd.add("UTF-8");

        // 页面大小
        if (options.pageSize != null) {
            cmd.add("--page-size");
            cmd.add(options.pageSize);
        } else if (options.pageWidth != null && options.pageHeight != null) {
            cmd.add("--page-width");
            cmd.add(options.pageWidth);
            cmd.add("--page-height");
            cmd.add(options.pageHeight);
        }

        // 边距
        if (options.marginTop != null) {
            cmd.add("--margin-top");
            cmd.add(options.marginTop);
        }
        if (options.marginBottom != null) {
            cmd.add("--margin-bottom");
            cmd.add(options.marginBottom);
        }
        if (options.marginLeft != null) {
            cmd.add("--margin-left");
            cmd.add(options.marginLeft);
        }
        if (options.marginRight != null) {
            cmd.add("--margin-right");
            cmd.add(options.marginRight);
        }

        // JavaScript 相关
        if (options.enableJavascript) {
            cmd.add("--enable-javascript");
            cmd.add("--javascript-delay");
            cmd.add(String.valueOf(options.javascriptDelay));
        } else {
            cmd.add("--disable-javascript");
        }

        // 禁用智能缩放
        if (!options.enableIntelligentShrinking) {
            cmd.add("--disable-smart-shrinking");
        }

        // 其他常用选项
        cmd.add("--no-stop-slow-scripts");
        cmd.add("--enable-local-file-access");

        // 输入输出
        cmd.add(inputHtml.toAbsolutePath().toString());
        cmd.add(outputPdf.toAbsolutePath().toString());

        return cmd;
    }

    /**
     * PDF 生成选项。
     */
    public static class PdfOptions {
        private String pageSize;
        private String pageWidth;
        private String pageHeight;
        private String marginTop = "0";
        private String marginBottom = "0";
        private String marginLeft = "0";
        private String marginRight = "0";
        private boolean enableJavascript = true;
        private int javascriptDelay = 1000;
        private boolean enableIntelligentShrinking = false;

        public static PdfOptions defaults() {
            return new PdfOptions();
        }

        public PdfOptions pageSize(String size) {
            this.pageSize = size;
            return this;
        }

        public PdfOptions customSize(String width, String height) {
            this.pageWidth = width;
            this.pageHeight = height;
            return this;
        }

        public PdfOptions margins(String top, String right, String bottom, String left) {
            this.marginTop = top;
            this.marginRight = right;
            this.marginBottom = bottom;
            this.marginLeft = left;
            return this;
        }

        public PdfOptions noMargins() {
            return margins("0", "0", "0", "0");
        }

        public PdfOptions enableJavascript(boolean enable) {
            this.enableJavascript = enable;
            return this;
        }

        public PdfOptions javascriptDelay(int delayMs) {
            this.javascriptDelay = delayMs;
            return this;
        }

        public PdfOptions enableIntelligentShrinking(boolean enable) {
            this.enableIntelligentShrinking = enable;
            return this;
        }
    }
}
