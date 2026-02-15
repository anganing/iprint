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

package com.iboot.iprint.controller;

import com.iboot.iprint.result.ApiResult;
import com.iboot.iprint.service.HiprintRenderService;
import com.iboot.iprint.model.request.RenderRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/engine")
public class HiprintRenderEngineController {
    @Resource private HiprintRenderService hiprintRenderService;

    /**
     * 生成打印预览 HTML。
     *
     * <p>接收打印模板和数据，生成可用于预览的 HTML 内容，以文件流形式返回。
     *
     * @param renderRequest 包含模板内容和打印数据的请求对象
     * @param response HTTP 响应对象
     */
    @PostMapping("/generateHtml")
    public void generateHtml(@RequestBody @Valid RenderRequest renderRequest,
                             HttpServletResponse response) throws IOException {
        String html = hiprintRenderService.generateHtml(renderRequest);
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition", "inline; filename=hiprint.html");
        response.setContentLengthLong(htmlBytes.length);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(htmlBytes)) {
            IOUtils.copy(bis, response.getOutputStream());
        }
    }

    /**
     * 生成打印预览 PDF。
     *
     * <p>接收打印模板和数据，生成PDF并返回文件流。
     *
     * @param renderRequest 包含模板内容和打印数据的请求对象
     * @param response HTTP 响应对象
     */
    @PostMapping("/generatePdf")
    public void generatePdf(@RequestBody @Valid RenderRequest renderRequest,
                            HttpServletResponse response) throws IOException {
        File file = hiprintRenderService.generatePdfByWkhtml2Pdf(renderRequest);
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            response.setContentLengthLong(file.length());

            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, response.getOutputStream());
            }
        } finally {
            file.delete();
        }
    }

    /**
     * 获取 Hiprint 版本信息。
     *
     * @return Hiprint 版本号字符串
     */
    @GetMapping("/version")
    public ApiResult<String> getHiprintVersion() {
        return ApiResult.ok(hiprintRenderService.getHiprintVersion());
    }
}
