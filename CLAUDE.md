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
- `controller/` — AuthController（登录/注销/改密）、ApiKeyController（CRUD）
- `service/` — UserService、ApiKeyService（含缓存同步）、CustomUserDetailsService
- `repository/` — JPA Repository 接口
- `entity/` — User、ApiKey（映射 sys_user、sys_api_key）
- `dto/` — 请求/响应 DTO
- `common/` — R（统一响应）、GlobalExceptionHandler、BusinessException、ApiKeyAuthFilter
- `enums/` — ApiKeyStatus

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
- `GET /api/keys` — 列表
- `POST /api/keys` — 创建
- `PUT /api/keys/{id}` — 更新
- `DELETE /api/keys/{id}` — 删除

## Code Conventions

- 使用 Lombok 注解（`@Data`, `@Builder`, `@RequiredArgsConstructor` 等）
- 统一响应格式 `R<T>`：`{ code, message, data }`
- 业务异常使用 `BusinessException`，由 `GlobalExceptionHandler` 统一捕获
- 前端 Axios 拦截器统一处理 code !== 200 和 401 跳转
- 源文件编码 UTF-8
