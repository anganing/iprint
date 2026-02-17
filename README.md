# iBoot iPrint

**iBoot iPrint** 是一个轻量级、开箱即用的打印服务平台，提供可视化模板设计、服务端渲染和 API 驱动的 HTML/PDF 生成能力。适用于需要批量生成票据、标签、报表等打印场景的业务系统集成。

## 功能特性

- **可视化模板设计器** — 基于 Hiprint 的拖拽式模板编辑器，支持文本、图片、表格、条形码、二维码等多种元素
- **服务端渲染引擎** — 通过 HtmlUnit 在服务端执行 Hiprint JS 引擎，无需浏览器即可生成 HTML/PDF
- **RESTful API** — 提供模板管理、渲染引擎等完整 API，支持通过模板编码 + 数据直接生成打印文件
- **API Key 认证** — 支持 `X-API-Key` 请求头认证，方便第三方系统对接
- **多用户管理** — 管理员可管理用户、API Key、打印模板等资源
- **Docker 部署** — 提供 Dockerfile 和 docker-compose 配置，一键容器化部署

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 (Corretto) | 运行时 |
| Spring Boot | 4.0.2 | 应用框架 |
| Spring Security | 7 | Session + API Key 双重认证 |
| Spring Data JPA | - | ORM 持久层 |
| PostgreSQL | 17 | 关系数据库 |
| Caffeine | - | 本地缓存（API Key 实时验证） |
| HtmlUnit | 4.12.0 | 无头浏览器（服务端 JS 执行） |
| wkhtmltopdf | - | HTML 转 PDF（系统级工具） |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 渐进式 UI 框架 |
| TypeScript | 5.9 | 类型安全 |
| Vite | 7.3+ | 构建工具 |
| Pinia | 3.0 | 状态管理 |
| daisyUI | 5.0 | 基于 Tailwind CSS 4 的组件库 |
| vue-plugin-hiprint | 0.0.61-beta5 | 打印模板设计器（改造版） |

## 关于 vue-plugin-hiprint 的改造

本项目内置了 [vue-plugin-hiprint](https://github.com/CcSimple/vue-plugin-hiprint)（`@anganing/vue-plugin-hiprint` v0.0.61-beta5）的改造版本，位于 `vue-plugin-hiprint/` 目录。

### 改造内容：条形码与二维码元素类型

**原版实现**（`default-etyps-provider.js`）：

```javascript
{
  tid: 'defaultModule.barcode',
  title: '条形码',
  type: 'barcode',
},
{
  tid: 'defaultModule.qrcode',
  title: '二维码',
  type: 'qrcode',
}
```

**改造后实现**：

```javascript
{
  tid: 'defaultModule.barcode',
  title: '123456789',
  type: 'text',
  options: {
    height: 35,
    width: 140,
    textType: 'barcode'
  }
},
{
  tid: 'defaultModule.qrcode',
  title: '二维码',
  type: 'text',
  options: {
    height: 50,
    width: 50,
    textType: 'qrcode'
  }
}
```

**改造原因**：将条形码和二维码从独立的 `barcode`/`qrcode` 类型改为 `text` 类型 + `textType` 选项的方式。这样条形码和二维码本质上是文本元素的变体，可以更好地支持服务端渲染引擎中的统一处理逻辑，同时保留了对数据绑定的完整支持。

## 快速开始

### 环境要求

- Java 17+
- PostgreSQL 17
- Node.js 18+（前端开发）
- pnpm（前端包管理器）
- wkhtmltopdf（PDF 生成，生产环境必需）

### 1. 数据库初始化

```bash
# 创建数据库
createdb -U postgres iboot_iprint

# 执行初始化脚本（如有）
psql -U postgres -d iboot_iprint -f docs/schema.sql
```

> 应用首次启动时 JPA 会自动建表（`ddl-auto: update`），并初始化默认管理员账户 `admin/123456`。

### 2. 后端启动

```bash
# 构建（跳过测试）
.\mvnw.cmd clean install -DskipTests

# 启动
.\mvnw.cmd spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 3. 前端启动（开发模式）

```bash
cd iboot-iprint-ui

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev
```

前端开发服务器运行在 `http://localhost:5173`，自动代理 `/api` 请求到后端。

### 4. 生产构建

```bash
# 前端构建（输出到 Spring Boot 静态资源目录）
cd iboot-iprint-ui
pnpm build

# 后端打包
cd ..
.\mvnw.cmd clean install -DskipTests
```

构建产物为单个可执行 JAR，包含前端静态资源。

### 5. Docker 部署

```bash
# 构建镜像
docker build -t ibootio/iprint:latest .

# 使用 docker-compose 启动
docker-compose up -d
```

服务运行在 `http://localhost:58080`。

配置文件挂载路径：`./config/application-prod.yaml`

## API 使用

### 认证方式

**管理员 Web 认证**（Session）：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

**API Key 认证**（适用于第三方集成）：

```bash
curl http://localhost:8080/api/templates \
  -H "X-API-Key: your-api-key-here"
```

### 核心 API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/auth/login` | 管理员登录 |
| `POST` | `/api/auth/logout` | 注销 |
| `GET` | `/api/auth/info` | 当前用户信息 |
| `PUT` | `/api/auth/password` | 修改密码 |
| `GET` | `/api/keys` | API Key 列表 |
| `POST` | `/api/keys` | 创建 API Key |
| `PUT` | `/api/keys/{id}` | 更新 API Key |
| `DELETE` | `/api/keys/{id}` | 删除 API Key |
| `GET` | `/api/templates` | 模板列表 |
| `GET` | `/api/templates/{id}` | 模板详情 |
| `POST` | `/api/templates` | 创建模板 |
| `PUT` | `/api/templates/{id}` | 更新模板 |
| `DELETE` | `/api/templates/{id}` | 删除模板 |
| `POST` | `/api/engine/generateHtml` | 模板数据 → HTML |
| `POST` | `/api/engine/generatePdf` | 模板数据 → PDF |
| `POST` | `/api/engine/render/html` | 模板编码 + 数据 → HTML |
| `POST` | `/api/engine/render/pdf` | 模板编码 + 数据 → PDF |
| `GET` | `/api/engine/version` | Hiprint 引擎版本 |
| `GET` | `/api/users` | 用户列表 |
| `POST` | `/api/users` | 创建用户 |
| `PUT` | `/api/users/{id}` | 更新用户 |
| `DELETE` | `/api/users/{id}` | 删除用户 |
| `GET` | `/api/dashboard/stats` | 仪表盘统计 |

### 渲染示例

**通过模板编码生成 PDF**：

```bash
curl -X POST http://localhost:8080/api/engine/render/pdf \
  -H "X-API-Key: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SHIPPING_LABEL",
    "printData": {
      "orderId": "ORD-20250217-001",
      "customerName": "张三",
      "address": "北京市朝阳区xxx路xxx号"
    }
  }' \
  --output shipping_label.pdf
```

## 项目结构

```
iboot-iprint/
├── src/main/java/com/iboot/iprint/
│   ├── config/          # 安全、缓存、Web 配置
│   ├── security/        # API Key 认证过滤器
│   ├── controller/      # REST 控制器
│   ├── service/         # 业务逻辑层
│   │   └── impl/        # 服务实现（含 Hiprint 渲染引擎）
│   ├── converter/       # 实体与响应对象转换
│   ├── model/           # 请求/响应模型
│   ├── repository/      # JPA 数据访问
│   ├── entity/          # 数据库实体
│   ├── result/          # 统一响应封装
│   ├── exception/       # 异常处理
│   ├── enums/           # 枚举类型
│   └── util/            # 工具类
├── iboot-iprint-ui/     # Vue 3 前端
│   └── src/
│       ├── api/         # API 封装
│       ├── stores/      # Pinia 状态管理
│       ├── router/      # 路由配置
│       ├── views/       # 页面组件
│       └── components/  # 公共组件
├── vue-plugin-hiprint/  # Hiprint 打印插件（改造版）
├── config/              # 生产环境配置
├── Dockerfile           # Docker 镜像构建
└── docker-compose.yml   # Docker Compose 编排
```

## License

Proprietary Software - Copyright (c) 2025 iboot. All rights reserved.
