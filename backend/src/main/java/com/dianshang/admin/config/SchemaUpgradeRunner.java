package com.dianshang.admin.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 旧库增量补列（H2 / MySQL 通用）。CREATE TABLE IF NOT EXISTS 不会修改已存在表结构。
 */
@Component
@Order(0)
public class SchemaUpgradeRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaUpgradeRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        addColumnIfMissing("sys_admin_user", "status", "TINYINT NOT NULL DEFAULT 1");
        addColumnIfMissing("dashboard_daily_metric", "yesterday_sales_amount", "DECIMAL(14, 2) NOT NULL DEFAULT 0");
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        if (columnExists(table, column)) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE UPPER(TABLE_NAME) = UPPER(?) AND UPPER(COLUMN_NAME) = UPPER(?)
                """,
                Integer.class,
                table,
                column);
        return count != null && count > 0;
    }
}
