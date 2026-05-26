# 暴走电商 Uniapp — 消息中心模块

## 页面

| 路径 | 说明 |
|------|------|
| `pages/message/index.vue` | 消息中心分类看板（三入口 + 未读红点 + 最新摘要） |
| `pages/message/list.vue` | 分类消息列表（Tabs / 分页 / 已读 / 跳转） |

## 接口联调

在 `api/message.js` 将 `USE_MOCK` 设为 `false`，并在 `utils/request.js` 配置 `BASE_URL`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/u/message/unread-count` | 各分类未读数 |
| GET | `/api/u/message/page?category=&page=&size=` | 分页列表 |
| PUT | `/api/u/message/read` | Body: `number[]` 消息 ID |

## 运行

使用 HBuilderX 打开本目录，或：

```bash
npm install
npm run dev:h5
```

## 闭环测试

1. 后台 `systemMessage.vue` 发送秒杀公告 → 移动端【促销优惠】红点增加（需后端 PROMOTION 类消息同步）
2. 下单/发货 → `ums_user_message` 插入 `ORDER` 类 → 【交易物流】列表展示
3. 点击消息 → 单条已读 + `jump_url` 跳转；一键已读 → 红点扣减
