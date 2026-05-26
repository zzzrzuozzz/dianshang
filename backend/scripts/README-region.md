# 国标省市区数据导入

## 方式一：启动自动导入（默认）

`application.yml` 已配置：

```yaml
data-locations: classpath:data.sql,classpath:region/region-full.sql
```

`region-full.sql` 含 **3351** 条省/市/区记录（来源：[modood/Administrative-divisions-of-China](https://github.com/modood/Administrative-divisions-of-China)）。

每次启动会先 `DELETE FROM sys_region` 再批量 `INSERT`（开发环境可接受；生产建议改为一次性手工导入）。

## 方式二：MySQL 手工导入

```bash
mysql -u root -p dianshang < backend/src/main/resources/region/region-full.sql
```

## 重新生成 SQL

```bash
# 可选：将 modood 的 provinces/cities/areas.json 放到 backend/scripts/data/
node backend/scripts/generate-region-sql.mjs
```

输出：`backend/src/main/resources/region/region-full.sql`

## 懒加载 API

`GET /api/system/region/list-by-parent?parentCode=0`

`parent_code` 字段已建索引，单次查询目标 &lt; 10ms。
