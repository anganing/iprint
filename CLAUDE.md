# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

iboot-iprint 是一个极简管理系统，提供单用户认证和 API Key 管理功能。前后端分离架构。
- **后端**: Spring Boot 4.0.2 + Java 17（Amazon Corretto 17）
- **前端**: Vue 3 + Vite + TypeScript + Pinia + daisyUI（Tailwind CSS）
- 属于 iboot-pro 项目群的子项目

## Build & Run Commands

### 后端（Maven Wrapper）

```bash
# 构建项目
.\mvnw.cmd clean install

# 运行应用
.\mvnw.cmd spring-boot:run

# 运行全部测试
.\mvnw.cmd test

# 运行单个测试类
.\mvnw.cmd test -Dtest=IbootIprintApplicationTests

# 运行单个测试方法
.\mvnw.cmd test -Dtest=IbootIprintApplicationTests#contextLoads

# 跳过测试构建
.\mvnw.cmd clean install -DskipTests
```

### 前端（pnpm）

```bash
cd iboot-iprint-ui

# 安装依赖
pnpm install

# 开发模式（自动代理 /api 到后端 8080）
pnpm dev

# 生产构建（输出到 ../src/main/resources/static）
pnpm build
```

### 数据库

PostgreSQL 17，连接信息：`localhost:5432/iboot_iprint`，`postgres/123456`。
初始化脚本：`docs/schema.sql`，默认管理员账户 `admin/admin123`。

## Tech Stack

### 后端
- **Spring Boot 4.0.2**（Spring Security 7，使用 Lambda DSL 和 `requestMatchers` 配置）
- **Spring MVC**（Servlet 模式）+ **Spring Security**（Session + API Key 双重认证）
- **Spring Data JPA** + **PostgreSQL**
- **Caffeine** 本地缓存（API Key 实时验证，无 Redis）
- **Bean Validation** + **Lombok**

### 前端
- **Vue 3** + **TypeScript** + **Vite 7**
- **Pinia**（状态管理）+ **Vue Router**（SPA 路由）
- **Axios**（HTTP 客户端，withCredentials 传递 Session Cookie）
- **daisyUI 5**（基于 Tailwind CSS 4 的组件库）

## Architecture

### 后端 `com.iboot.iprint`
- `config/` — SecurityConfig（双重认证）、CacheConfig（Caffeine）、WebConfig（CORS + SPA）
- `security/` — ApiKeyAuthFilter（API Key 认证过滤器）
- `controller/` — AuthController（登录/注销/改密）、ApiKeyController（CRUD）、PrintTemplateController（模版 CRUD）、HiprintRenderEngineController（渲染引擎）
- `service/` — UserService、ApiKeyService（含缓存同步）、PrintTemplateService、HiprintRenderService、CustomUserDetailsService
- `service/impl/` — HiprintRenderServiceImpl（Hiprint 服务端渲染引擎实现）
- `converter/` — ApiKeyConverter、PrintTemplateConverter（实体与响应对象转换）
- `model/request/` — LoginRequest、ChangePasswordRequest、ApiKeyRequest、PrintTemplateRequest、RenderRequest
- `model/response/` — UserInfoResponse、ApiKeyResponse、PrintTemplateResponse
- `repository/` — JPA Repository 接口
- `entity/` — BaseEntity（公共基类）、User、ApiKey、PrintTemplate（映射 sys_user、sys_api_key、sys_print_template）
- `result/` — ApiResult（统一响应封装）
- `exception/` — BusinessException、GlobalExceptionHandler
- `enums/` — ApiKeyStatus
- `util/` — WkhtmltopdfUtil（PDF 生成工具）

### 安全模型
- **管理员 Web 认证**：Session-based，通过 `/api/auth/login` 登录
- **API Key 认证**：请求头 `X-API-Key`，经 ApiKeyAuthFilter 通过 Caffeine 缓存实时验证
- API Key 的 CRUD 操作会同步刷新缓存，确保变更即时生效

### 前端 `iboot-iprint-ui/src/`
- `api/` — Axios 实例（401 自动跳转登录）+ 各模块 API 封装
- `stores/` — Pinia auth store
- `router/` — 路由 + 登录守卫
- `views/` — Login、ApiKeys、ChangePassword 页面
- `components/` — Layout（导航栏）

### API 端点
- `POST /api/auth/login` — 登录
- `POST /api/auth/logout` — 注销
- `GET /api/auth/info` — 当前用户信息
- `PUT /api/auth/password` — 修改密码
- `GET /api/keys` — API Key 列表
- `POST /api/keys` — 创建 API Key
- `PUT /api/keys/{id}` — 更新 API Key
- `DELETE /api/keys/{id}` — 删除 API Key
- `GET /api/templates` — 打印模版列表
- `GET /api/templates/{id}` — 打印模版详情
- `POST /api/templates` — 创建打印模版
- `PUT /api/templates/{id}` — 更新打印模版
- `DELETE /api/templates/{id}` — 删除打印模版
- `POST /api/engine/generateHtml` — 生成打印预览 HTML
- `POST /api/engine/generatePdf` — 生成打印 PDF
- `GET /api/engine/version` — 获取 Hiprint 版本号

## Code Conventions

- 使用 Lombok 注解（`@Data`, `@Builder`, `@RequiredArgsConstructor` 等）
- 统一响应格式 `ApiResult<T>`：`{ code, message, data }`
- 业务异常使用 `BusinessException`（`exception` 包），由 `GlobalExceptionHandler` 统一捕获
- 实体类继承 `BaseEntity`（公共 id、createdAt、updatedAt）
- 实体与响应对象转换通过 `converter/` 包中的 Converter 类完成
- 前端 Axios 拦截器统一处理 code !== 200 和 401 跳转
- 源文件编码 UTF-8
