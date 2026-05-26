# 暴走电商 - 管理后台 API（模块1）

Spring Boot 3.2 + JWT + JPA，默认使用 H2 文件库（开发联调）。

## 启动

```bash
cd backend
mvn spring-boot:run
```

服务地址：`http://localhost:8080`

默认账号：`admin` / `admin123`

## 模块2 商品管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/product/list` | 商品列表（含 Tab 计数） |
| GET | `/api/product/audit/list` | 审核列表 |
| PUT | `/api/product/{id}/status` | 上/下架 |
| DELETE | `/api/product/{id}` | 删除 |
| POST | `/api/product/batch/on` | 批量上架 |
| POST | `/api/product/batch/off` | 批量下架 |
| DELETE | `/api/product/batch` | 批量删除 |
| POST | `/api/product/audit/{id}` | 单条审核 |
| POST | `/api/product/audit/batch` | 批量审核 |
| GET | `/api/product/category/tree` | 分类树 |
| GET | `/api/product/category/options` | 分类下拉 |
| GET | `/api/product/category/list` | 分类分页列表 |
| GET | `/api/product/brand/list` | 品牌列表 |
| GET | `/api/product/comment/overview` | 评价看板 |
| GET | `/api/product/comment/list` | 评价列表 |

## 模块1 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录，返回 JWT |
| GET | `/api/dashboard/overview` | 首页看板（需 Bearer Token） |
| GET | `/api/system/user/profile` | 当前用户资料 |
| PUT | `/api/system/user/profile` | 更新资料 |
| PUT | `/api/system/user/profile/updatePwd` | 修改密码 |

统一响应：`{ "code": 200, "message": "success", "data": ... }`

## MySQL（可选）

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

在 `application-mysql.yml` 中配置数据源后使用。

## 前端联调

根目录执行 `npm run dev`，Vite 已将 `/api` 代理到 `8080`。
