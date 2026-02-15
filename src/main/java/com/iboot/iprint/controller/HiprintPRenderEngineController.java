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

import com.iboot.iprint.common.R;
import com.iboot.iprint.core.HiprintRenderService;
import com.iboot.iprint.dto.PrintTemplateRenderDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/engine")
public class HiprintPRenderEngineController {
  @Resource private HiprintRenderService hiprintRenderService;

  /**
   * 生成打印预览 HTML。
   *
   * <p>接收打印模板和数据，生成可用于预览的 HTML 内容。该方法会对输入参数进行验证， 确保必要的数据都已提供。
   *
   * @param printTemplateRenderDto 包含模板内容和打印数据的 DTO 对象
   * @return 生成的 HTML 字符串
   * @throws IllegalArgumentException 当请求参数为空或必要字段缺失时
   */
  @PostMapping("/generateHtml")
  public R<String> generateHtml(@RequestBody @Valid PrintTemplateRenderDTO printTemplateRenderDto) throws IOException {
    String html = hiprintRenderService.generateHtml(printTemplateRenderDto);
    // 将html写入一个文件
      FileUtils.writeStringToFile(new File("hiprint.html"), html, "UTF-8");
    return R.ok(html);
  }

  /**
   * 生成打印预览 PDF。
   *
   * <p>接收打印模板和数据，生成PDF并返回文件流。
   *
   * @param printTemplateRenderDto 包含模板内容和打印数据的 DTO 对象
   * @param response HTTP 响应对象
   */
  @PostMapping("/generatePdf")
  public void generatePdf(@RequestBody @Valid PrintTemplateRenderDTO printTemplateRenderDto,
                          HttpServletResponse response) throws IOException {
      File file = hiprintRenderService.generatePdfByWkhtml2Pdf(printTemplateRenderDto);

      response.setContentType("application/pdf");
      response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
      response.setContentLengthLong(file.length());

      try (FileInputStream fis = new FileInputStream(file)) {
          IOUtils.copy(fis, response.getOutputStream());
      }
  }

  /**
   * 获取 Hiprint 版本信息。
   *
   * @return Hiprint 版本号字符串
   */
  @GetMapping("/version")
  public R<String> getHiprintVersion() {
    return R.ok(hiprintRenderService.getHiprintVersion());
  }
}
