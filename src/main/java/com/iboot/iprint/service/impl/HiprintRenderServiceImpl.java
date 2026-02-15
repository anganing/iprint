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

package com.iboot.iprint.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.iboot.iprint.exception.BusinessException;
import com.iboot.iprint.service.HiprintRenderService;
import com.iboot.iprint.model.request.RenderRequest;
import com.iboot.iprint.util.WkhtmltopdfUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.htmlunit.*;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.NameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hiprint 服务端渲染引擎。
 *
 * <p>该类负责将 Hiprint 模板和数据在服务端进行渲染，生成 HTML 或获取版本信息。 使用 HtmlUnit 模拟浏览器环境执行 JavaScript 代码。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HiprintRenderServiceImpl implements HiprintRenderService {
    @Value("${server.port:8080}")
    private Integer port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final ObjectMapper objectMapper;

    /**
     * 生成打印预览 HTML。
     *
     * @param renderRequest 请求参数
     * @return 渲染后的 HTML 字符串
     */
    @Override
    public String generateHtml(RenderRequest renderRequest) {
        // 这里要调用两次 toJsonStr 否则 JS 脚本执行失败
        String tplData = objectMapper.writeValueAsString(renderRequest.getTplData());
        tplData = objectMapper.writeValueAsString(tplData);
        String printData = objectMapper.writeValueAsString(renderRequest.getPrintData());
        printData = objectMapper.writeValueAsString(printData);

        String script =
                """
                        (function() {
                            try {
                                var result = generateHtml(%s, %s);
                                return result;
                            } catch(e) {
                                return 'ERROR: ' + e.message;
                            }
                        })()
                        """
                        .formatted(tplData, printData);

        String tempHtml = executeHiprintScript(script);

        // ========== 构建最终 HTML ==========//
        try {
            String cssContent = ResourceUtil.readUtf8Str("static/hiprint/css/print-lock.css");

            return """
                    <!DOCTYPE html>
                    <html>
                    <head>
                      <meta charset='UTF-8'>
                      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                      <style>
                    %s
                      </style>
                    </head>
                    <body>
                    %s
                    </body>
                    </html>
                    """
                    .formatted(cssContent, tempHtml);
        } catch (Exception e) {
            throw new BusinessException("读取 CSS 文件失败");
        }
    }

    /**
     * 获取 Hiprint 版本号。
     *
     * @return Hiprint 版本号字符串
     */
    @Override
    public String getHiprintVersion() {
        String script =
                """
                        (function() {
                            try {
                                var result = getHiprintVersion();
                                return result;
                            } catch(e) {
                                return 'ERROR: ' + e.message;
                            }
                        })()
                        """;
        return executeHiprintScript(script);
    }

    @SneakyThrows
    @Override
    public File generatePdfByWkhtml2Pdf(RenderRequest renderRequest) {
        Path pdfPath = Files.createTempFile("pdf", UlidCreator.getUlid().toLowerCase() + ".pdf");
        String html = this.generateHtml(renderRequest);
        // wkhtmltopdf 使用旧版 QtWebKit，不支持 SVG 2.0 的 href 属性
        // 需要将 href="#xxx" 转换为 xlink:href="#xxx" 以兼容二维码渲染
        html = html.replaceAll("href=\"(#[^\"]+)\"", "xlink:href=\"$1\"");

        // 提取 hiprint 的 宽高
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> panels = (List<Map<String, Object>>) renderRequest.getTplData().get("panels");
        int width = 210; // 默认 A4 宽度
        int height = 297; // 默认 A4 高度
        if (panels != null && !panels.isEmpty()) {
            Map<String, Object> firstPanel = panels.get(0);
            width = ((Number) firstPanel.getOrDefault("width", 210)).intValue();
            height = ((Number) firstPanel.getOrDefault("height", 297)).intValue();
        }
        log.info("hiprint width: {}, height: {}", width, height);
        // 使用系统 wkhtmltopdf 命令生成 PDF
        WkhtmltopdfUtil.PdfOptions options = WkhtmltopdfUtil.PdfOptions.defaults()
                .customSize(width + "mm", height + "mm")
                .enableJavascript(true)
                .javascriptDelay(1000)
                .enableIntelligentShrinking(false)
                .noMargins();

        WkhtmltopdfUtil.htmlToPdf(html, pdfPath, options);

        return pdfPath.toFile();
    }

    /**
     * 执行 Hiprint JavaScript 脚本。
     *
     * @param script 要执行的 JavaScript 脚本
     * @return 脚本执行结果
     */
    private String executeHiprintScript(String script) {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            // ========== 浏览器配置 ==========//
            webClient.getOptions().setJavaScriptEnabled(true); // 必须启用 JS
            webClient.getOptions().setCssEnabled(false); // 禁用 CSS 解析（按需开启）
            webClient.getOptions().setThrowExceptionOnScriptError(false); // 忽略脚本错误
            webClient.getOptions().setGeolocationEnabled(false); // 禁用地理定位
            webClient.getOptions().setPrintContentOnFailingStatusCode(true);
            webClient.setJavaScriptTimeout(15000); // JS 执行超时时间（毫秒）
            webClient.getCookieManager().setCookiesEnabled(true); // 启用 Cookie

            // ========== 强制 UTF-8 编码 ==========//
            // HtmlUnit 加载外部 JS 文件时，若 HTTP 响应未声明 charset，
            // 可能默认使用 ISO-8859-1 解码，导致 JS 库内置的中文字符串乱码。
            // 通过拦截 HTTP 响应，为缺少 charset 的 Content-Type 补充 UTF-8 声明。
            WebConnection originalConnection = webClient.getWebConnection();
            webClient.setWebConnection(new WebConnection() {
                @Override
                public WebResponse getResponse(WebRequest request) throws IOException {
                    WebResponse response = originalConnection.getResponse(request);
                    String ctHeader = response.getResponseHeaderValue("Content-Type");
                    if (ctHeader != null && !ctHeader.isEmpty() && !ctHeader.toLowerCase().contains("charset")) {
                        List<NameValuePair> headers = new ArrayList<>(response.getResponseHeaders());
                        for (int i = 0; i < headers.size(); i++) {
                            if ("Content-Type".equalsIgnoreCase(headers.get(i).getName())) {
                                headers.set(i, new NameValuePair("Content-Type",
                                        headers.get(i).getValue() + "; charset=UTF-8"));
                                break;
                            }
                        }
                        WebResponseData data = new WebResponseData(
                                response.getContentAsStream().readAllBytes(),
                                response.getStatusCode(),
                                response.getStatusMessage(),
                                headers
                        );
                        return new WebResponse(data, request, response.getLoadTime());
                    }
                    return response;
                }

                @Override
                public void close() {
                    // Delegate to original connection - ignore close errors
                    try {
                        originalConnection.close();
                    } catch (Exception ignored) {
                    }
                }
            });

            // ========== 构建请求 URL ==========//
            String hiprintEntry = getHiprintEntry();

            // ========== 页面加载 ==========//
            HtmlPage page = webClient.getPage(hiprintEntry);

            // 等待 JS 初始化完成（关键！）
            webClient.waitForBackgroundJavaScript(3000);
            int waitCount = 0;
            while (waitCount++ < 5) {
                Thread.sleep(500); // 防止 CPU 过载
            }

            // ========== 执行核心脚本 ==========//
            log.debug("执行的 script = {}", script);
            ScriptResult result = page.executeJavaScript(script);
            String scriptResult = (String) result.getJavaScriptResult();

            // ========== 异常处理 ==========//
            if (scriptResult.startsWith("ERROR:")) {
                throw new BusinessException("JavaScript 执行失败: " + scriptResult.substring(6));
            }
            return scriptResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Hiprint 脚本执行被中断");
        } catch (Exception e) {
            throw new BusinessException("Hiprint 脚本执行失败");
        }
    }

    /**
     * 获取 Hiprint 入口页面 URL。
     *
     * @return 完整的入口页面 URL
     */
    private String getHiprintEntry() {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(contextPath)
                .path("/hiprint/index.html")
                .build()
                .toUriString();
    }
}
