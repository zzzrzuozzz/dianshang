# 暴走电商 - 管理后台 API（模块1）

Spring Boot 3.2 + JWT + JPA，默认使用 H2 文件库（开发联调）。

## 启动

```bash
cd backend
mvn spring-boot:run
```

服务地址：`http://localhost:8080`

默认账号：`admin` / `admin123`

若此前启动失败导致 H2 库不完整，请先删除 `backend/data/dianshang.mv.db*` 再启动。

国标区划在首次启动时由 `RegionFullSqlImporter` 导入（不再每次执行 `region-full.sql`）。

## 模块3 订单管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/order/list` | 订单列表（`pageType=list\|confirm`） |
| GET | `/api/order/detail/{orderNo}` | 订单详情 |
| POST | `/api/order/{orderNo}/ship` | 发货 |
| POST | `/api/order/{orderNo}/reissue` | 补发（扣库存 + reissue 流水） |
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

## 运营模块接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ops/notification/list` | 推送列表（`msgType=SYSTEM\|SMS\|STATION`，`tab` 分类/状态） |
| GET | `/api/ops/notification/{notifyCode}` | 推送详情 |
| POST | `/api/ops/notification/save` | 保存推送（系统/短信/站内信） |
| POST | `/api/ops/notification/estimate` | 预估触达人数 |
| POST | `/api/ops/notification/{notifyCode}/resend` | 发送/再发 |
| DELETE | `/api/ops/notification/{notifyCode}` | 删除推送 |
| POST | `/api/ops/notification/batch/delete` | 批量删除 `{ "ids": ["NT6550"] }` |
| POST | `/api/ops/notification/batch/resend` | 批量发送/再发 |
| POST | `/api/upload/image?biz=ops` | 图片上传（multipart，返回 `{ "url": "/uploads/ops/..." }`） |
| GET | `/api/ops/advertisement/list` | 广告位列表 |
| GET | `/api/ops/advertisement/{advCode}` | 广告详情（编辑回显） |
| POST | `/api/ops/advertisement/save` | 新增/编辑广告 |
| POST | `/api/ops/advertisement/{advCode}/online` | 上下架 `{ "online": true }` |
| POST | `/api/ops/advertisement/{advCode}/pin` | 置顶 |
| DELETE | `/api/ops/advertisement/{advCode}` | 删除广告 |
| GET | `/api/ops/global/search` | 全局搜索（`keyword`，商品/订单/用户各最多 5 条） |

启动后 `OpsDataInitializer` 会写入演示推送与广告数据。

## 模块4 库存管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/inventory/list` | 库存列表（Tab: all/warning/out） |
| POST | `/api/inventory/update` | 编辑商品/SKU 库存 |
| GET | `/api/inventory/flow` | 出库流水（Tab: all/sales_out/return_in/manual_out/manual_in/reissue） |
| GET | `/api/inventory/flow/export` | 流水 CSV 导出 |

## 模块2 商品管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/product/{productNo}` | 商品详情（编辑回显） |
| POST | `/api/product` | 新建商品 |
| PUT | `/api/product/{productNo}` | 更新商品 |
| GET | `/api/product/list` | 商品列表（含 Tab 计数） |
| GET | `/api/product/audit/list` | 审核列表 |
| PUT | `/api/product/{id}/status` | 上/下架 |
| DELETE | `/api/product/{id}` | 删除（移入回收站） |
| GET | `/api/product/recycle/list` | 回收站列表 |
| POST | `/api/product/recycle/restore` | 恢复 `{ "ids": ["025345"] }` |
| DELETE | `/api/product/recycle/{productNo}` | 彻底删除 |
| DELETE | `/api/product/recycle/batch` | 批量彻底删除 |
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

## 内容管理接口（`/api/content`）

| 模块 | 主要路径 |
|------|----------|
| 专题类型 | `GET/POST /topic/type/list|save|options`，`GET/DELETE /topic/type/{id}`，`PUT /topic/type/{id}/visible` |
| 专题 | `GET /topic/list`，`GET /topic/detail/{code}`，`GET /topic/{code}`，`POST /topic/save`，`PUT /topic/{code}/status` |
| 专题评论 | `POST /topic/comment/reply|review`，`DELETE /topic/comment/{code}`，`POST /topic/comment/batch/delete` |
| 帮助分类 | `GET/POST /help/type/list|save|options`，`GET/DELETE /help/type/{id}` |
| 帮助文章 | `GET /help/list`，`GET /help/{code}`，`POST /help/save`，`PUT /help/{code}/status` |

`ContentDataInitializer` 在表为空时写入演示数据。商品选择复用 `GET /api/promotion/products/picker`。

## 财务管理接口（`/api/finance`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/finance/overview` | 资金看板（KPI、走势、待审核提现），`granularity=day\|month` |
| POST | `/api/finance/statement/page` | 交易流水分页（按时间范围走 `idx_create_time`） |
| POST | `/api/finance/statement/export` | 导出 CSV 对账单（流式写入，限制 5000 条） |
| POST | `/api/finance/withdraw/page` | 提现申请分页，`tab=all\|pending\|transferring\|done` |
| PUT | `/api/finance/withdraw/verify` | 审批 `{ applyNo, passed, remark }`，仅待审核可处理（幂等） |
| POST | `/api/finance/reconcile` | 全库对账：补全订单/提现流水，并同步首页看板待办与近 7 日销售额 |

表：`fin_transaction_record`（流水号唯一防重）、`fin_withdraw_apply`。

**全局联动**：订单发货/确认收货 → `ORDER_IN`；订单退款 → `REFUND_OUT`；提现审核通过 → `WITHDRAW`；首页待办与销售额与财务流水对齐；全局搜索支持财务流水号。

## 数据统计接口（`/api/stats`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stats/transaction/overview` | 交易统计（KPI、趋势、漏斗、价格区间、来源/设备等），参数 `range=week\|month` 或 `startDate`+`endDate` |
| GET | `/api/stats/flow/report` | 流量统计（用户趋势、版本/渠道玫瑰图、页面访问） |
| GET | `/api/stats/product/category` | 商品类目分析表 + 饼图数据，分页 `page`/`pageSize` |
| GET | `/api/stats/product/ranking` | 商品销售排行，分页 `page`/`pageSize` |

数据由 `oms_order`、`ums_member`、`pms_product`、`pms_category` 与 `dashboard_daily_metric` 聚合；订单/指标变更后图表随接口刷新。

## 权限管理接口（RBAC）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/system/menu/tree` | 菜单权限树 |
| POST | `/api/system/menu/save` | 保存菜单/按钮 |
| DELETE | `/api/system/menu/{id}` | 删除菜单 |
| POST | `/api/system/role/page` | 角色分页 |
| GET | `/api/system/role/options` | 角色下拉 |
| GET | `/api/system/role/{id}/menu-ids` | 角色已分配菜单 ID |
| POST | `/api/system/role/save` | 保存角色 |
| PUT | `/api/system/role/{id}/status` | 启用/禁用角色 |
| POST | `/api/system/role/save-permissions` | 分配权限（事务内先删后插） |
| POST | `/api/system/admin/page` | 管理员分页 |
| POST | `/api/system/admin/save` | 保存管理员及角色 |
| PUT | `/api/system/admin/{id}/status` | 启用/禁用管理员 |
| GET | `/api/auth/session` | 刷新当前用户菜单与 perms |
| POST | `/api/auth/register` | 公开注册（手机/邮箱 + 用户名≥6 + 密码≥6 + 邀请码） |
| POST | `/api/auth/forgot-password` | 找回密码（注册手机/邮箱 + 邀请码 + 新密码） |
| POST | `/api/system/invite-code/generate` | 超管随机生成邀请码（绑定角色） |
| POST | `/api/system/invite-code/page` | 邀请码分页（含是否已使用） |

表：`sys_invite_code`。演示邀请码：`DEMO2026REG01`（绑定 `operator` 普通操作员角色）。

登录响应含 `menus`、`roleKeys`、`perms`；`admin` 角色拥有全部菜单。演示账号：`finance` / `finance123`（仅财务模块）。

**鉴权闭环**：
- 菜单/角色/管理员/邀请码维护接口仅超级管理员可访问。
- JWT 过滤器注入 `roleKey` 与按钮 `perms` 到 Spring Security 上下文。
- 敏感操作服务端校验：`order:ship`、`order:refund`、`finance:withdraw:verify`。
- 前端：`DynamicMenu` 动态侧栏、`canAccessPath` 路由守卫、`v-perm` 按钮级显隐；布局挂载时 `GET /api/auth/session` 刷新权限。

## 系统设置接口（`/api/system`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/system/config/public` | 公开配置（商城名、客服、包邮门槛、库存策略，无需 Token） |
| GET | `/api/system/config/platform` | 平台基础信息（表单结构化） |
| PUT | `/api/system/config/platform` | 保存平台基础信息 |
| GET | `/api/system/config/get-all` | 全部 Key-Value 配置 |
| PUT | `/api/system/config/update-batch` | 批量更新配置项 |
| GET | `/api/system/region/list-by-parent` | 省市区懒加载 |
| POST | `/api/system/maintenance/clear-cache` | 清除配置 JVM 缓存 |
| GET | `/api/system/maintenance/initializer-status` | 启动种子脚本健康检查 |

表：`sys_config`（动态扩展，无需改表结构即可新增配置键）。

## 系统消息通知（`/api/system/notice`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/system/notice/unread-list?limit=5&urgent=false` | 顶部铃铛微型未读列表 |
| GET | `/api/system/notice/unread-summary` | 未读总数与各类型计数 |
| POST | `/api/system/notice/page` | 消息中心分页（`noticeType` 可选） |
| PUT | `/api/system/notice/read` | 批量标记已读 `{ "ids": [1,2] }` |
| PUT | `/api/system/notice/read-all` | 一键已读（可选 `noticeType`） |
| POST | `/api/system/notice/delete` | 批量删除 `{ "ids": [1,2] }` |
| GET | `/api/system/notice/{id}` | 消息详情 |

表：`sys_notice`。启动时 `NoticeDataInitializer` 写入演示通知，并根据待审批提现单动态同步「商户提现待审批」待办。前端：`NoticeBell` 60 秒轮询 + `message/index` 详情已读联动刷新红点。

**全局联动**：配置变更 → 登录页/侧栏品牌、首页横幅、订单详情平台规则；`unpaid_close_minutes` 驱动定时关单；`stock_deduct_strategy` 驱动发货出库策略；清缓存后重新加载配置。

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

## 用户模块接口（`/api/user`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/list` | 用户列表（tab、日期筛选） |
| GET | `/api/user/detail/{userNo}` | 用户详情（地址、订单） |
| POST | `/api/user/update` | 编辑资料 |
| GET | `/api/user/export` | 导出用户 CSV |
| GET | `/api/user/level/list` | 会员等级列表 |
| GET | `/api/user/level/detail/{code}` | 等级详情 |
| POST | `/api/user/level/save` | 新增/编辑等级 |
| DELETE | `/api/user/level/{code}` | 删除等级 |
| GET | `/api/user/level/options` | 等级下拉 |
| GET | `/api/user/tag/list` | 标签列表 |
| GET | `/api/user/tag/detail/{code}` | 标签规则 |
| POST | `/api/user/tag/save` | 新增/编辑标签（自动匹配人数） |
| DELETE | `/api/user/tag/{code}` | 删除标签 |
| GET | `/api/user/tag/users` | 标签下用户 |
| POST | `/api/user/tag/users/remove` | 移出标签 |
| GET | `/api/user/growth/list` | 成长值/积分用户列表 |
| POST | `/api/user/growth/adjust` | 调整成长值/积分/设置 |
| POST | `/api/user/growth/batch` | 批量调整 |
| GET | `/api/user/growth/tasks` | 任务设置 |
| POST | `/api/user/growth/tasks/save` | 保存任务 |
| GET/POST | `/api/user/growth/rules` | 规则设置 |
| GET/POST | `/api/user/growth/rewards` | 奖励设置 |
| GET | `/api/user/growth/ledger` | 成长值/积分明细 |
| GET | `/api/user/growth/export` | 导出成长积分 CSV |

启动后 `MemberDataInitializer` 会写入演示用户（BZ6542 等）、等级、标签与任务。

## 促销模块接口（`/api/promotion`）

| 模块 | 主要路径 |
|------|----------|
| 商品选择 | `GET /products/picker` |
| 秒杀活动 | `GET/POST /seckill/list|save`，`DELETE /seckill/{code}` |
| 秒杀时段 | `GET/POST /seckill/time/list|save` |
| 秒杀商品 | `GET /seckill/{act}/sku/{slot}`，`POST /seckill/sku/save` |
| 团购活动 | `GET/POST /group-buy/list|save` |
| 团购时段 | `GET /group-buy/{act}/time/list`，`POST /group-buy/time/save` |
| 团购商品 | `GET /group-buy/{act}/sku/{slot}`，`POST /group-buy/sku/save`（含属性 JSON） |
| 优惠券 | `GET/POST /coupon/list|save`，`GET /coupon/{code}`，`GET /coupon/{code}/history` |

`PromotionDataInitializer` 写入秒杀/团购/优惠券演示数据。

## 前端联调

根目录执行 `npm run dev`，Vite 已将 `/api` 代理到 `8080`。
