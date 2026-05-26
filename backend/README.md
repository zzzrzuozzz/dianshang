# 暴走电商 - 管理后台 API（模块1）

Spring Boot 3.2 + JWT + JPA，默认使用 H2 文件库（开发联调）。

## 启动

```bash
cd backend
mvn spring-boot:run
```

服务地址：`http://localhost:8080`

默认账号：`admin` / `admin123`

## 模块3 订单管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/order/list` | 订单列表（`pageType=list\|confirm`） |
| GET | `/api/order/detail/{orderNo}` | 订单详情 |
| POST | `/api/order/{orderNo}/ship` | 发货 |
| POST | `/api/order/{orderNo}/refund` | 退款 |
| POST | `/api/order/{orderNo}/confirm` | 确认收货 |
| POST | `/api/order/batch/confirm` | 批量确认收货 |
| GET | `/api/order/after-sale/list` | 售后列表 |
| GET | `/api/order/setting/return-reason/list` | 退货原因列表 |
| POST/PUT/DELETE | `/api/order/setting/return-reason/...` | 退货原因 CRUD |
| GET/POST/PUT/DELETE | `/api/order/setting/express-template/...` | 快递单模板 CRUD |
| GET/POST/PUT/DELETE | `/api/order/setting/address/...?type=ship\|return` | 发货/退货地址 CRUD |
| GET | `/api/system/region/list-by-parent?parentCode=0` | 省市区懒加载（完整国标见 `region/region-full.sql`） |
| GET | `/api/ops/express/preview/{orderId}` | 快递单套打预览数据 |

## 模块2 商品管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/product/{productNo}` | 商品详情（编辑回显） |
| POST | `/api/product` | 新建商品 |
| PUT | `/api/product/{productNo}` | 更新商品 |
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
