# iBoot iPrint — 让你的业务系统轻松拥有打印能力

---

## 还在为打印功能头疼吗？

你是否遇到过这些场景：

- 业务系统需要打印发货单、标签、票据，但开发打印功能太耗时
- 每次模板调整都要改代码、重新部署
- 想在服务端直接生成 PDF，而不是依赖用户浏览器
- 第三方系统需要对接打印，缺少标准化的 API

如果你正为这些问题烦恼，**iBoot iPrint** 就是为你准备的。

---

## iBoot iPrint 是什么？

**iBoot iPrint** 是一个轻量级的打印服务平台，开箱即用。

它将打印能力封装为标准 API 服务——你只需要设计好模板，传入数据，就能立即获得格式化的 HTML 或 PDF 文件。

**一句话总结：模板设计可视化，打印生成 API 化。**

![](https://md2card.com/img/screenshots/1771323827034_vicfp_clipboard-image.png)

---

## 核心能力

### 1. 可视化模板设计器

拖拽即设计，所见即所得。

支持的元素类型：
- 文本（支持数据绑定）
- 表格（自动分页、合计）
- 图片
- 条形码 / 二维码
- 线条、矩形、椭圆等图形元素

无需编写代码，运营人员也能自主调整模板——从字号大小到边距布局，一切可视化完成。

![](https://md2card.com/img/screenshots/1771323935636_ozptq8_clipboard-image.png)

### 2. 服务端渲染引擎

**不依赖用户浏览器，纯服务端生成。**

iBoot iPrint 在服务端内置了完整的 Hiprint 渲染引擎，通过 HtmlUnit 无头浏览器执行 JS 渲染逻辑，结合 wkhtmltopdf 生成高质量 PDF。

这意味着：
- 批量生成数千张标签？没问题
- 定时任务自动出报表？可以
- 无人值守的后台打印服务？支持

![](https://md2card.com/img/screenshots/1771324063092_a2kdcl_clipboard-image.png)

### 3. 标准 RESTful API

所有功能通过 API 暴露，对接简单直接。

```
POST /api/engine/render/pdf
Header: X-API-Key: your-key
Body: { "code": "SHIPPING_LABEL", "printData": {...} }
→ 返回 PDF 文件流
```

两种渲染模式：
- **直传模式**：直接传入模板 JSON + 数据，适合灵活场景
- **编码模式**：传入模板编码 + 数据，模板在平台管理，业务系统只关心数据

### 4. API Key 安全认证

为每个对接系统分配独立的 API Key，支持启用/禁用，变更即时生效（Caffeine 本地缓存，无需 Redis）。

既安全，又简单。

![](https://md2card.com/img/screenshots/1771324112359_9ju9kp_clipboard-image.png)

---

## 技术架构

![](https://md2card.com/img/screenshots/1771324167422_bbbowa_clipboard-image.png)

**后端**：Spring Boot 4 + Spring Security 7 + JPA + PostgreSQL

**前端**：Vue 3 + TypeScript + Vite + daisyUI

**部署**：Docker 一键部署，单 JAR 包含前后端，开箱即用

---

## 适用场景

| 场景 | 说明 |
|------|------|
| 电商发货 | 批量生成快递面单、发货标签 |
| 仓储物流 | 货架标签、拣货单、出库单 |
| 零售门店 | 价签、小票、会员卡 |
| 财务报表 | 对账单、结算单、发票模板 |
| 生产制造 | 工序流转卡、质检报告 |
| 医疗教育 | 检验报告单、成绩单、证书 |

---

## 部署有多简单？

**三步启动：**

**第一步**：准备 PostgreSQL 数据库

**第二步**：编辑配置文件 `application-prod.yaml`

**第三步**：Docker 启动

```bash
docker-compose up -d
```

完成。访问 `http://your-server:58080`，使用默认账户 `admin/123456` 登录即可。

---

## 为什么选择 iBoot iPrint？

**轻量**
没有 Redis、没有消息队列、没有微服务拆分。一个 JAR + 一个 PostgreSQL，就是全部。

**实用**
不堆砌概念，只解决问题——把模板设计和打印生成这件事做到位。

**标准**
RESTful API + API Key 认证，任何语言、任何框架都能5分钟完成对接。

**自主可控**
私有化部署，数据不出内网，模板不上云端。

---

## 关于 Hiprint 引擎改造

iBoot iPrint 内置了 vue-plugin-hiprint 打印引擎的改造版本。

主要改造点：将条形码和二维码从独立元素类型统一为文本元素的 `textType` 变体，使其在服务端渲染时能够走统一的文本处理链路，解决了原版条形码/二维码在无头浏览器环境下的渲染兼容性问题。

这一改造对终端用户完全透明——在可视化设计器中，条形码和二维码的拖拽使用方式与原版完全一致。

---

## 在线试用

眼见为实，直接体验：

> 试用地址：**http://iprint.iboot.top**
>
> 账户密码：关注公众号后台私信：iprint

登录后即可体验模板设计、数据渲染、PDF 生成等全部功能。



---

## 获取与咨询

如果你对 iBoot iPrint 感兴趣，欢迎联系我们：

- 定制化需求沟通
- 私有化部署指导
- 技术支持与培训

**后台私信，或直接留言咨询。**


---

*iBoot iPrint — 让打印回归简单。*
