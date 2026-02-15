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

package com.iboot.iprint.service;

import com.iboot.iprint.model.request.RenderRequest;
import jakarta.validation.Valid;

import java.io.File;

public interface HiprintRenderService {
    /**
     * 生成打印预览 HTML。
     *
     * @param renderRequest 请求参数
     * @return 渲染后的 HTML 字符串
     */
    String generateHtml(RenderRequest renderRequest);

    /**
     * 获取 Hiprint 版本号。
     *
     * @return Hiprint 版本号字符串
     */
    String getHiprintVersion();

    /**
     * 生成 PDF
     *
     * @param renderRequest 请求参数
     * @return PDF 文件
     */
    File generatePdfByWkhtml2Pdf(@Valid RenderRequest renderRequest);
}
