# 暴走电商

一套可本地联调的电商后台管理系统：Vue 3 管理端 + Spring Boot REST API + Uniapp 移动端（消息中心等模块）。

前后端通过 JWT 鉴权、RBAC 菜单/按钮权限、统一 JSON 响应格式对接；开发环境默认 H2 文件库，无需额外安装数据库即可跑通主流程。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 管理端 | Vue 3、TypeScript、Vite 6、Element Plus、ECharts、Vue Router |
| 后端 | Java 17、Spring Boot 3.2、Spring Security、JWT、Spring Data JPA |
| 数据库 | H2（默认，MySQL 兼容模式）/ MySQL（可选） |
| 移动端 | Uniapp（`uniapp/` 目录，H5 / 小程序） |

---

## 仓库结构

```
dianshang/
├── src/                 # 管理后台前端（Vue）
│   ├── api/             # 接口封装
│   ├── components/      # 通用组件（上传、富文本、侧栏等）
│   ├── composables/     # 组合式逻辑（如商品编辑器）
│   ├── constants/       # 业务常量（订单/商品/库存枚举）
│   ├── views/           # 页面模块
│   └── router/          # 路由与守卫
├── backend/             # Spring Boot API
│   ├── src/main/java/   # 业务代码（auth / product / order / …）
│   ├── src/main/resources/
│   │   ├── schema.sql   # 表结构
│   │   └── data.sql     # 种子数据
│   └── data/            # H2 数据库文件（运行时生成）
└── uniapp/              # C 端 Uniapp 项目
```

---

## 快速开始

### 环境要求

- **Node.js** 18+
- **JDK** 17+
- **Maven** 3.8+

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

- 服务地址：`http://localhost:8080`
- H2 控制台：`http://localhost:8080/h2-console`（JDBC URL 见 `application.yml`）

若此前启动失败导致 H2 库损坏，删除 `backend/data/dianshang.mv.db*` 后重新启动。

### 2. 启动管理端

在项目根目录：

```bash
npm install
npm run dev
```

- 前端地址：`http://localhost:5173`
- Vite 已将 `/api`、`/uploads` 代理到 `8080`

### 3. 构建生产包

```bash
# 前端
npm run build

# 后端
cd backend && mvn package
```

---

## 演示账号

| 账号 | 密码 | 说明 |
|------|------|------|
| `admin` | `admin123` | 超级管理员，拥有全部菜单与按钮权限 |
| `finance` | `finance123` | 财务角色，主要可见财务相关模块 |
| 自助注册 | — | 邀请码 `DEMO2026REG01`，绑定 `operator` 普通操作员角色 |

登录后可在 **个人中心 → 刷新菜单与权限** 调用 `GET /api/auth/session`，无需重新登录即可同步最新 RBAC。

---

## 功能模块

| 模块 | 管理端路径示例 | 能力概要 |
|------|----------------|----------|
| 首页看板 | `/dashboard` | KPI、待办、销售趋势 |
| 消息中心 | `/message/index` | 系统通知铃铛、已读/删除（`sys_notice`） |
| 商品 | `/product/*` | 列表/审核/回收站、多规格 SKU、分类品牌 CRUD、分类商品转移、评价 |
| 订单 | `/order/*` | 列表/详情、发货退款、售后审批、运费模板与地址设置 |
| 库存 | `/inventory/*` | 库存列表、批量调整、出库流水与导出 |
| 用户 | `/user/*` | 会员列表、等级/标签、成长值积分 |
| 促销 | `/promotion/*` | 秒杀、团购、优惠券 |
| 内容 | `/content/*` | 专题、帮助中心（简易富文本） |
| 运营 | `/ops/*` | 系统/短信/站内信推送、广告位（C 端 `ops_notification`） |
| 财务 | `/finance/*` | 资金看板、流水、提现审批 |
| 数据统计 | `/stats/*` | 交易、流量、商品分析 |
| 权限 | `/permission/*` | 菜单、角色、管理员、邀请码 |
| 系统设置 | `/setting/*` | 平台信息、扩展 KV 配置、区划、运维 |

### 消息体系说明

项目中有两套通知，用途不同：

- **`sys_notice`**：管理端内部消息（顶部铃铛、消息中心），如提现待审、商品审核结果。
- **`ops_notification`**：面向 C 端用户的运营推送（系统消息 / 短信 / 站内信），在运营模块维护。

管理端相关页面已挂载 `NoticeScopeHelp` 组件说明上述区别。

### 文件上传

- `POST /api/upload/image?biz=product|ops|…` — 图片（multipart）
- `POST /api/upload/video?biz=…` — 视频

文件保存在 `backend/uploads/`，前端通过 `/uploads/...` 访问。

---

## 开发说明

### 前端约定

- 接口统一走 `src/api/*`，响应格式 `{ code, message, data }`
- 权限：`DynamicMenu` 动态侧栏、`v-perm` 按钮显隐、`canAccessPath` 路由守卫
- 业务枚举集中在 `src/constants/`，已移除旧 `src/mock/` 假数据

### 后端约定

- 统一响应体 `ApiResponse`
- 敏感操作服务端校验权限码，如 `order:ship`、`order:refund`、`finance:withdraw:verify`
- 启动时多个 `*DataInitializer` 写入演示数据；国标区划首次导入后持久化，不重复执行大 SQL

### 切换 MySQL

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

在 `application-mysql.yml` 中配置数据源，并确保已创建库表（`schema.sql` / `data.sql`）。

### Uniapp 客户端

见 [uniapp/README.md](./uniapp/README.md)。消息中心模块可联调 C 端消息接口。

---

## API 文档

完整 REST 接口列表见 **[backend/README.md](./backend/README.md)**。

统一响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

除登录、注册、公开配置等白名单路径外，请求头需携带：

```
Authorization: Bearer <token>
```

---

## 常见问题

**Q：后端编译报 H2 / JPA 相关错误？**  
A：确认 JDK 17，执行 `mvn clean compile`；若数据库文件损坏，删除 `backend/data/dianshang.mv.db*` 后重启。

**Q：前端 401 或菜单空白？**  
A：重新登录；或在个人中心点击「刷新菜单与权限」。

**Q：上传图片 404？**  
A：确认后端已启动，且访问路径为 `/uploads/...`（Vite 开发环境已代理）。

**Q：operator 角色看不到新菜单？**  
A：超管保存角色权限后，操作员在个人中心刷新权限；旧库升级时后端会自动补全内置按钮菜单。

---

## License

Private / 课程与演示用途。生产部署请修改 JWT Secret、数据库密码及上传目录权限。
