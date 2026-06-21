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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.f4b6a3.ulid.UlidCreator;
import com.iboot.iprint.exception.BusinessException;
import com.iboot.iprint.model.request.RenderRequest;
import com.iboot.iprint.service.HiprintRenderService;
import io.woo.htmltopdf.HtmlToPdf;
import io.woo.htmltopdf.HtmlToPdfObject;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hiprint 服务端渲染引擎。
 *
 * <p>该类负责将 Hiprint 模板和数据在服务端进行渲染，生成 HTML 或获取版本信息。 使用 HtmlUnit 模拟浏览器环境执行 JavaScript 代码。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HiprintRenderServiceImpl implements HiprintRenderService {
    private static final String PRINT_LOCK_CSS_CACHE_KEY = "print-lock.css";

    @Value("${server.port:8080}")
    private Integer port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${iprint.render.htmlunit.initial-wait-ms:200}")
    private long htmlUnitInitialWaitMs;

    @Value("${iprint.render.htmlunit.ready-timeout-ms:3000}")
    private long htmlUnitReadyTimeoutMs;

    @Value("${iprint.render.htmlunit.ready-poll-ms:50}")
    private long htmlUnitReadyPollMs;

    @Value("${iprint.render.pdf.javascript-delay-ms:100}")
    private int pdfJavascriptDelayMs;

    @Value("${iprint.render.page-pool.enabled:true}")
    private boolean pagePoolEnabled;

    @Value("${iprint.render.page-pool.size:2}")
    private int pagePoolSizeConfig;

    @Value("${iprint.render.page-pool.borrow-timeout-ms:1000}")
    private long pagePoolBorrowTimeoutMs;

    @Value("${iprint.render.page-pool.max-uses-per-page:200}")
    private int pagePoolMaxUsesPerPage;

    private final ObjectMapper objectMapper;
    private final BlockingQueue<PooledHiprintPage> pagePool = new LinkedBlockingQueue<>();
    private final AtomicInteger pagePoolSize = new AtomicInteger();

    @Resource(name = "hiprintStaticResourceCache")
    private Cache<String, String> hiprintStaticResourceCache;

    /**
     * 生成打印预览 HTML。
     *
     * @param renderRequest 请求参数
     * @return 渲染后的 HTML 字符串
     */
    @Override
    public String generateHtml(RenderRequest renderRequest) {
        return generateHtml(renderRequest, newRenderId());
    }

    private String generateHtml(RenderRequest renderRequest, String renderId) {
        long totalStart = System.nanoTime();
        long serializeStart = System.nanoTime();

        // 这里要调用两次 toJsonStr 否则 JS 脚本执行失败
        String tplData = objectMapper.writeValueAsString(renderRequest.getTplData());
        tplData = objectMapper.writeValueAsString(tplData);
        String printData = objectMapper.writeValueAsString(renderRequest.getPrintData());
        printData = objectMapper.writeValueAsString(printData);
        long serializeCost = elapsedMillis(serializeStart);

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

        long scriptStart = System.nanoTime();
        String tempHtml = executeHiprintScript(script, renderId);
        long scriptCost = elapsedMillis(scriptStart);

        // ========== 构建最终 HTML ==========//
        long cssStart = System.nanoTime();
        try {
            String cssContent = hiprintStaticResourceCache.get(PRINT_LOCK_CSS_CACHE_KEY,
                    key -> ResourceUtil.readUtf8Str("static/hiprint/css/print-lock.css"));
            long cssCost = elapsedMillis(cssStart);
            long totalCost = elapsedMillis(totalStart);
            log.info("[{}] hiprint generateHtml finished, serialize={}ms, script={}ms, css={}ms, total={}ms",
                    renderId, serializeCost, scriptCost, cssCost, totalCost);

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
            log.error("[{}] 读取 CSS 文件失败", renderId, e);
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
        return executeHiprintScript(script, newRenderId());
    }

    /**
     * 使用 io.woo:htmltopdf（内置 wkhtmltox 原生库，无需安装 wkhtmltopdf 命令行）生成 PDF。
     *
     * <p>底层为 wkhtmltopdf（QtWebKit）引擎，以纯 Java 依赖内置原生库，免去系统安装。
     *
     * @param renderRequest 请求参数
     * @return PDF 文件
     */
    @SneakyThrows
    @Override
    public File generatePdfByHtmlToPdf(RenderRequest renderRequest) {
        String renderId = newRenderId();
        long totalStart = System.nanoTime();

        long tempFileStart = System.nanoTime();
        Path pdfPath = Files.createTempFile("pdf", UlidCreator.getUlid().toLowerCase() + ".pdf");
        long tempFileCost = elapsedMillis(tempFileStart);

        long htmlStart = System.nanoTime();
        String html = this.generateHtml(renderRequest, renderId);
        long htmlCost = elapsedMillis(htmlStart);

        long replaceStart = System.nanoTime();
        // wkhtmltopdf 使用旧版 QtWebKit，不支持 SVG 2.0 的 href 属性
        // 需要将 href="#xxx" 转换为 xlink:href="#xxx" 以兼容二维码渲染
        html = html.replaceAll("href=\"(#[^\"]+)\"", "xlink:href=\"$1\"");
        long replaceCost = elapsedMillis(replaceStart);

        // 提取 hiprint 面板宽高（单位 mm）
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> panels = (List<Map<String, Object>>) renderRequest.getTplData().get("panels");
        int width = 210; // 默认 A4 宽度
        int height = 297; // 默认 A4 高度
        if (panels != null && !panels.isEmpty()) {
            Map<String, Object> firstPanel = panels.get(0);
            width = ((Number) firstPanel.getOrDefault("width", 210)).intValue();
            height = ((Number) firstPanel.getOrDefault("height", 297)).intValue();
        }
        log.debug("[{}] hiprint width: {}, height: {}", renderId, width, height);

        // 自定义页面尺寸：HtmlToPdf 未直接暴露宽高设置，通过 create(Map) 传 wkhtmltox 原生全局设置
        Map<String, String> globalSettings = new HashMap<>();
        globalSettings.put("size.width", width + "mm");
        globalSettings.put("size.height", height + "mm");

        long convertStart = System.nanoTime();
        boolean success = HtmlToPdf.create(globalSettings)
                .disableSmartShrinking(true)
                .marginTop("0")
                .marginBottom("0")
                .marginLeft("0")
                .marginRight("0")
                .object(HtmlToPdfObject.forHtml(html)
                        .enableJavascript(true)
                        .javascriptDelay(Math.max(0, pdfJavascriptDelayMs))
                        .usePrintMediaType(true)
                        .enableIntelligentShrinking(false))
                .convert(pdfPath.toString());
        long convertCost = elapsedMillis(convertStart);

        if (!success) {
            throw new BusinessException("PDF 生成失败");
        }
        log.info("[{}] hiprint generatePdf finished, tempFile={}ms, html={}ms, replace={}ms, convert={}ms, total={}ms, fileSize={}bytes, pdfJavascriptDelay={}ms",
                renderId, tempFileCost, htmlCost, replaceCost, convertCost, elapsedMillis(totalStart),
                pdfPath.toFile().length(), Math.max(0, pdfJavascriptDelayMs));
        return pdfPath.toFile();
    }

    /**
     * 执行 Hiprint JavaScript 脚本。
     *
     * @param script 要执行的 JavaScript 脚本
     * @param renderId 渲染日志追踪 ID
     * @return 脚本执行结果
     */
    private String executeHiprintScript(String script, String renderId) {
        if (pagePoolEnabled) {
            try {
                return executeHiprintScriptWithPool(script, renderId);
            } catch (PoolUnavailableException e) {
                log.warn("[{}] Hiprint 页面池不可用，回退到一次性 WebClient: {}", renderId, e.getMessage());
            }
        }
        return executeHiprintScriptOneShot(script, renderId);
    }

    private String executeHiprintScriptWithPool(String script, String renderId) {
        long totalStart = System.nanoTime();
        BorrowedPage borrowedPage = borrowPooledPage(renderId);
        PooledHiprintPage pooledPage = borrowedPage.page();
        boolean discard = false;
        try {
            log.debug("[{}] 执行的 script 长度 = {}", renderId, script.length());
            long scriptStart = System.nanoTime();
            ScriptResult result = pooledPage.page().executeJavaScript(script);
            long scriptCost = elapsedMillis(scriptStart);
            String scriptResult = (String) result.getJavaScriptResult();

            if (scriptResult.startsWith("ERROR:")) {
                throw new BusinessException("JavaScript 执行失败: " + scriptResult.substring(6));
            }
            log.info("[{}] hiprint executeScript finished, pool=true, borrow={}ms, created={}, script={}ms, total={}ms, uses={}",
                    renderId, borrowedPage.borrowCostMs(), borrowedPage.created(), scriptCost, elapsedMillis(totalStart),
                    pooledPage.uses() + 1);
            return scriptResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            discard = true;
            log.error("[{}] Hiprint 页面池脚本执行失败，将丢弃当前页面", renderId, e);
            throw new BusinessException("Hiprint 脚本执行失败: " + e.getMessage());
        } finally {
            releasePooledPage(pooledPage, discard, renderId);
        }
    }

    private String executeHiprintScriptOneShot(String script, String renderId) {
        long totalStart = System.nanoTime();
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            configureWebClient(webClient);

            // ========== 构建请求 URL ==========//
            String hiprintEntry = getHiprintEntry();

            // ========== 页面加载 ==========//
            long loadStart = System.nanoTime();
            HtmlPage page = webClient.getPage(hiprintEntry);
            long loadCost = elapsedMillis(loadStart);

            // ========== 等待 JS 初始化完成 ==========//
            long readyStart = System.nanoTime();
            waitForHiprintReady(webClient, page, renderId);
            long readyCost = elapsedMillis(readyStart);

            // ========== 执行核心脚本 ==========//
            log.debug("[{}] 执行的 script 长度 = {}", renderId, script.length());
            long scriptStart = System.nanoTime();
            ScriptResult result = page.executeJavaScript(script);
            long scriptCost = elapsedMillis(scriptStart);
            String scriptResult = (String) result.getJavaScriptResult();

            // ========== 异常处理 ==========//
            if (scriptResult.startsWith("ERROR:")) {
                throw new BusinessException("JavaScript 执行失败: " + scriptResult.substring(6));
            }
            log.info("[{}] hiprint executeScript finished, pool=false, load={}ms, ready={}ms, script={}ms, total={}ms",
                    renderId, loadCost, readyCost, scriptCost, elapsedMillis(totalStart));
            return scriptResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Hiprint 脚本执行被中断");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[{}] Hiprint 脚本执行失败", renderId, e);
            throw new BusinessException("Hiprint 脚本执行失败: " + e.getMessage());
        }
    }

    private BorrowedPage borrowPooledPage(String renderId) {
        long borrowStart = System.nanoTime();
        PooledHiprintPage pooledPage = pagePool.poll();
        if (pooledPage != null) {
            return new BorrowedPage(pooledPage, elapsedMillis(borrowStart), false);
        }

        int maxSize = Math.max(1, pagePoolSizeConfig);
        while (true) {
            int currentSize = pagePoolSize.get();
            if (currentSize >= maxSize) {
                break;
            }
            if (pagePoolSize.compareAndSet(currentSize, currentSize + 1)) {
                try {
                    pooledPage = createPooledPage(renderId);
                    return new BorrowedPage(pooledPage, elapsedMillis(borrowStart), true);
                } catch (Exception e) {
                    pagePoolSize.decrementAndGet();
                    throw new PoolUnavailableException("创建页面失败", e);
                }
            }
        }

        try {
            pooledPage = pagePool.poll(Math.max(0, pagePoolBorrowTimeoutMs), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PoolUnavailableException("等待页面池被中断", e);
        }
        if (pooledPage == null) {
            throw new PoolUnavailableException("等待页面超过 " + pagePoolBorrowTimeoutMs + "ms");
        }
        return new BorrowedPage(pooledPage, elapsedMillis(borrowStart), false);
    }

    private PooledHiprintPage createPooledPage(String renderId) throws IOException, InterruptedException {
        long totalStart = System.nanoTime();
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        boolean success = false;
        try {
            configureWebClient(webClient);
            long loadStart = System.nanoTime();
            HtmlPage page = webClient.getPage(getHiprintEntry());
            long loadCost = elapsedMillis(loadStart);

            long readyStart = System.nanoTime();
            waitForHiprintReady(webClient, page, renderId);
            long readyCost = elapsedMillis(readyStart);

            success = true;
            log.info("[{}] Hiprint 页面池创建页面完成, load={}ms, ready={}ms, total={}ms, poolSize={}",
                    renderId, loadCost, readyCost, elapsedMillis(totalStart), pagePoolSize.get());
            return new PooledHiprintPage(webClient, page);
        } finally {
            if (!success) {
                closeQuietly(webClient);
            }
        }
    }

    private void releasePooledPage(PooledHiprintPage pooledPage, boolean discard, String renderId) {
        if (pooledPage == null) {
            return;
        }
        pooledPage.incrementUses();
        boolean expired = pooledPage.uses() >= Math.max(1, pagePoolMaxUsesPerPage);
        if (discard || expired || !pagePoolEnabled) {
            closeQuietly(pooledPage.webClient());
            pagePoolSize.decrementAndGet();
            log.debug("[{}] Hiprint 页面已回收, discard={}, expired={}, uses={}, poolSize={}",
                    renderId, discard, expired, pooledPage.uses(), pagePoolSize.get());
            return;
        }
        pagePool.offer(pooledPage);
    }

    private void configureWebClient(WebClient webClient) {
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
    }

    private void waitForHiprintReady(WebClient webClient, HtmlPage page, String renderId) throws InterruptedException {
        long initialWait = Math.max(0, htmlUnitInitialWaitMs);
        if (initialWait > 0) {
            webClient.waitForBackgroundJavaScript(initialWait);
        }

        long timeout = Math.max(0, htmlUnitReadyTimeoutMs);
        long poll = Math.max(10, htmlUnitReadyPollMs);
        long deadline = System.nanoTime() + timeout * 1_000_000L;
        int checks = 0;
        while (true) {
            checks++;
            ScriptResult readyResult = page.executeJavaScript(
                    """
                            (function() {
                                return !!(window.__HIPRINT_READY__ || (
                                    typeof generateHtml === 'function'
                                    && typeof getHiprintVersion === 'function'
                                    && !!window.hiprint
                                    && !!window['vue-plugin-hiprint']
                                ));
                            })()
                            """);
            Object jsResult = readyResult.getJavaScriptResult();
            if (Boolean.TRUE.equals(jsResult) || "true".equalsIgnoreCase(String.valueOf(jsResult))) {
                log.debug("[{}] Hiprint ready, checks={}", renderId, checks);
                return;
            }
            if (System.nanoTime() >= deadline) {
                throw new BusinessException("Hiprint 页面初始化超时");
            }
            Thread.sleep(poll);
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

    @PreDestroy
    public void closePagePool() {
        PooledHiprintPage pooledPage;
        while ((pooledPage = pagePool.poll()) != null) {
            closeQuietly(pooledPage.webClient());
        }
        pagePoolSize.set(0);
    }

    private void closeQuietly(WebClient webClient) {
        try {
            webClient.close();
        } catch (Exception ignored) {
        }
    }

    private String newRenderId() {
        return UlidCreator.getUlid().toLowerCase().substring(0, 8);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private record BorrowedPage(PooledHiprintPage page, long borrowCostMs, boolean created) {
    }

    private static final class PooledHiprintPage {
        private final WebClient webClient;
        private final HtmlPage page;
        private int uses;

        private PooledHiprintPage(WebClient webClient, HtmlPage page) {
            this.webClient = webClient;
            this.page = page;
        }

        private WebClient webClient() {
            return webClient;
        }

        private HtmlPage page() {
            return page;
        }

        private int uses() {
            return uses;
        }

        private void incrementUses() {
            uses++;
        }
    }

    private static class PoolUnavailableException extends RuntimeException {
        PoolUnavailableException(String message) {
            super(message);
        }

        PoolUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
