# 暴走电商 — 后端 API

Spring Boot 3.2 管理后台服务。项目总览与启动方式见 [根目录 README](../README.md)。

- 默认端口：`8080`
- 默认库：H2 文件库 `./data/dianshang`（MySQL 兼容模式）
- 统一响应：`{ "code": 200, "message": "success", "data": ... }`

## 启动

```bash
cd backend
mvn spring-boot:run
```

MySQL 配置：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

H2 库损坏时删除 `data/dianshang.mv.db*` 后重启。国标区划由 `RegionFullSqlImporter` 首次导入。

---

## 认证与账号

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录，返回 JWT + menus + perms |
| GET | `/api/auth/session` | 刷新当前用户菜单与按钮权限 |
| POST | `/api/auth/register` | 公开注册（手机/邮箱 + 邀请码） |
| POST | `/api/auth/forgot-password` | 找回密码 |
| GET | `/api/system/user/profile` | 当前用户资料 |
| PUT | `/api/system/user/profile` | 更新资料 |
| PUT | `/api/system/user/profile/updatePwd` | 修改密码 |

演示：`admin/admin123`、`finance/finance123`、邀请码 `DEMO2026REG01`（operator 角色）。

---

## 文件上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/upload/image?biz=product\|ops\|…` | 图片 multipart，返回 `{ url }` |
| POST | `/api/upload/video?biz=…` | 视频 multipart |

文件目录：`app.upload.dir`（默认 `./uploads`）。

---

## 首页看板

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/dashboard/overview` | KPI、待办、销售趋势 |

---

## 商品 `/api/product`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 商品列表（含 Tab 计数） |
| GET | `/{productNo}` | 商品详情（编辑回显，含 SKU） |
| POST | `/` | 新建商品 |
| PUT | `/{productNo}` | 更新商品 |
| GET | `/audit/list` | 审核列表 |
| POST | `/audit/{id}`、`/audit/batch` | 单条/批量审核 |
| PUT | `/{id}/status` | 上/下架 |
| DELETE | `/{id}` | 移入回收站 |
| POST | `/batch/on`、`/batch/off` | 批量上/下架 |
| DELETE | `/batch` | 批量删除 |
| GET | `/recycle/list` | 回收站 |
| POST | `/recycle/restore` | 恢复 `{ ids }` |
| DELETE | `/recycle/{productNo}`、`/recycle/batch` | 彻底删除 |
| GET | `/category/tree`、`/category/options`、`/category/list` | 分类树/下拉/分页 |
| POST | `/category` | 新增分类 |
| PUT | `/category/{id}` | 编辑分类 |
| DELETE | `/category/{id}` | 删除分类 |
| PUT | `/category/{id}/visible` | 显隐 |
| POST | `/category/transfer` | 分类商品转移 `{ fromCategoryCode, toCategoryCode }` → `{ moved }` |
| GET | `/brand/list` | 品牌列表 |
| POST | `/brand` | 新增品牌 |
| PUT | `/brand/{id}` | 编辑品牌 |
| DELETE | `/brand/{id}` | 删除品牌 |
| PUT | `/brand/{id}/visible` | 品牌显隐 |
| GET | `/comment/overview`、`/comment/list` | 评价看板与列表 |
| POST | `/comment/{productNo}/feature` | 加精 |
| DELETE | `/comment/{productNo}` | 删除评价 |

---

## 订单 `/api/order`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 订单列表（`pageType=list\|confirm`） |
| GET | `/detail/{orderNo}` | 订单详情（含可操作 actions） |
| POST | `/{orderNo}/ship` | 发货（需 `order:ship`） |
| POST | `/{orderNo}/reissue` | 补发 |
| POST | `/{orderNo}/refund` | 退款（需 `order:refund`） |
| POST | `/{orderNo}/confirm` | 确认收货 |
| POST | `/batch/confirm` | 批量确认收货 |
| GET | `/after-sale/list` | 售后列表 |
| POST | `/after-sale/{afterSaleNo}/approve` | 同意售后 |
| POST | `/after-sale/{afterSaleNo}/reject` | 拒绝售后 |
| POST | `/after-sale/{afterSaleNo}/confirm-return` | 确认退货入库 |
| GET/POST/PUT/DELETE | `/setting/return-reason/...` | 退货原因 CRUD + 显隐 |
| GET/POST/PUT/DELETE | `/setting/express-template/...` | 运费/快递单模板 CRUD |
| GET/POST/PUT/DELETE | `/setting/address/...?type=ship\|return` | 发货/退货地址 CRUD |

---

## 库存 `/api/inventory`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 库存列表（Tab: all/warning/out） |
| POST | `/update` | 调整商品/SKU 库存 |
| GET | `/flow` | 出库流水 |
| GET | `/flow/export` | 流水 CSV 导出 |

---

## 用户 `/api/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 用户列表 |
| GET | `/detail/{userNo}` | 用户详情 |
| POST | `/update` | 编辑资料 |
| GET | `/export` | 导出 CSV |
| GET/POST/DELETE | `/level/...` | 会员等级 |
| GET/POST/DELETE | `/tag/...` | 用户标签 |
| GET/POST | `/growth/...` | 成长值/积分、任务、规则 |

---

## 促销 `/api/promotion`

| 模块 | 主要路径 |
|------|----------|
| 商品选择 | `GET /products/picker` |
| 秒杀 | `GET/POST /seckill/list\|save`，时段与 SKU 子路径 |
| 团购 | `GET/POST /group-buy/list\|save`，时段与 SKU 子路径 |
| 优惠券 | `GET/POST /coupon/list\|save`，`GET /coupon/{code}/history` |

---

## 内容 `/api/content`

| 模块 | 主要路径 |
|------|----------|
| 专题类型 | `GET/POST /topic/type/list\|save\|options`，`PUT /topic/type/{id}/visible` |
| 专题 | `GET /topic/list`，`GET /topic/{code}`，`POST /topic/save`，`PUT /topic/{code}/status` |
| 专题评论 | `POST /topic/comment/reply\|review`，批量删除 |
| 帮助分类 | `GET/POST /help/type/list\|save\|options` |
| 帮助文章 | `GET /help/list`，`GET /help/{code}`，`POST /help/save`，`PUT /help/{code}/status` |

商品选择复用 `GET /api/promotion/products/picker`。

---

## 运营 `/api/ops`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/notification/list` | 推送列表（`msgType=SYSTEM\|SMS\|STATION`） |
| GET | `/notification/{notifyCode}` | 推送详情 |
| POST | `/notification/save` | 保存推送 |
| POST | `/notification/estimate` | 预估触达人数 |
| POST | `/notification/{notifyCode}/resend` | 发送/再发 |
| DELETE | `/notification/{notifyCode}` | 删除 |
| POST | `/notification/batch/delete`、`/batch/resend` | 批量操作 |
| GET | `/advertisement/list` | 广告位列表 |
| GET | `/advertisement/{advCode}` | 广告详情 |
| POST | `/advertisement/save` | 新增/编辑 |
| POST | `/advertisement/{advCode}/online` | 上下架 |
| POST | `/advertisement/{advCode}/pin` | 置顶 |
| DELETE | `/advertisement/{advCode}` | 删除 |
| GET | `/express/preview/{orderId}` | 快递单套打预览 |
| GET | `/global/search` | 全局搜索（商品/订单/用户/流水） |

---

## 财务 `/api/finance`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/overview` | 资金看板 |
| POST | `/statement/page` | 交易流水分页 |
| POST | `/statement/export` | 导出 CSV |
| POST | `/withdraw/page` | 提现申请分页 |
| PUT | `/withdraw/verify` | 审批（需 `finance:withdraw:verify`） |
| POST | `/reconcile` | 全库对账与看板同步 |

联动：订单发货/确认 → `ORDER_IN`；退款 → `REFUND_OUT`；提现通过 → `WITHDRAW`；待审提现同步 `sys_notice`。

---

## 数据统计 `/api/stats`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/transaction/overview` | 交易统计 |
| GET | `/flow/report` | 流量统计 |
| GET | `/product/category` | 类目分析 |
| GET | `/product/ranking` | 销售排行 |

---

## 权限 RBAC `/api/system`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/menu/tree` | 菜单权限树 |
| POST | `/menu/save` | 保存菜单/按钮 |
| DELETE | `/menu/{id}` | 删除菜单 |
| POST | `/role/page` | 角色分页 |
| GET | `/role/options` | 角色下拉 |
| GET | `/role/{id}/menu-ids` | 角色已分配菜单 |
| POST | `/role/save` | 保存角色 |
| PUT | `/role/{id}/status` | 启用/禁用 |
| POST | `/role/save-permissions` | 分配权限 |
| POST | `/admin/page` | 管理员分页 |
| POST | `/admin/save` | 保存管理员 |
| PUT | `/admin/{id}/status` | 启用/禁用 |
| POST | `/invite-code/generate` | 生成邀请码 |
| POST | `/invite-code/page` | 邀请码分页 |

---

## 系统设置 `/api/system`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/config/public` | 公开配置（免 Token） |
| GET | `/config/platform` | 平台基础信息 |
| PUT | `/config/platform` | 保存平台信息 |
| GET | `/config/get-all` | 全部 KV 配置 |
| PUT | `/config/update-batch` | 批量更新 KV |
| GET | `/region/list-by-parent?parentCode=0` | 省市区懒加载 |
| POST | `/maintenance/clear-cache` | 清除配置缓存 |
| GET | `/maintenance/initializer-status` | 种子脚本健康检查 |

---

## 系统消息 `/api/system/notice`

管理端内部通知（铃铛 / 消息中心），与 C 端运营推送 `ops_notification` 分离。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/unread-list?limit=5` | 顶部未读摘要 |
| GET | `/unread-summary` | 未读计数 |
| POST | `/page` | 消息中心分页 |
| PUT | `/read` | 批量已读 `{ ids }` |
| PUT | `/read-all` | 一键已读 |
| POST | `/delete` | 批量删除 |
| GET | `/{id}` | 消息详情 |

启动时 `NoticeDataInitializer` 写入演示数据；提现待审、商品审核等由同步任务动态更新。

---

## 鉴权说明

- JWT 过滤器注入 `roleKey` 与按钮 `perms` 到 Security 上下文
- `admin` 角色拥有全部菜单；菜单/角色/管理员/邀请码维护仅超管可访问
- 前端：`DynamicMenu`、`v-perm`、`canAccessPath`；布局挂载与个人中心均可调用 `/api/auth/session` 刷新
