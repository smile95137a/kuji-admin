package com.group.admin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DB Schema Migration Runner
 * 啟動時幂等執行 021 + 027 的欄位新增，避免重複執行報錯。
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class DbMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("🔧 DbMigrationRunner 開始執行 DB schema 補丁...");

        apply021();
        apply027();

        log.info("✅ DbMigrationRunner 完成");
    }

    // ==================== 021-order-logistics ====================

    private void apply021() {
        // shipping_method 表
        if (!tableExists("shipping_method")) {
            log.info("🔧 [021] 建立 shipping_method 表...");
            jdbcTemplate.execute("""
                CREATE TABLE `shipping_method` (
                  `id`          VARCHAR(36)   NOT NULL PRIMARY KEY,
                  `name`        VARCHAR(100)  NOT NULL COMMENT '運送方式名稱',
                  `code`        VARCHAR(50)   NOT NULL UNIQUE COMMENT '代碼（如 SEVEN_ELEVEN）',
                  `provider`    VARCHAR(100)  NULL COMMENT '物流商名稱',
                  `fee`         BIGINT        NOT NULL DEFAULT 0 COMMENT '運費（元）',
                  `status`      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / INACTIVE',
                  `sort_order`  INT           NOT NULL DEFAULT 0,
                  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='運送方式管理表'
            """);

            jdbcTemplate.execute("""
                INSERT IGNORE INTO `shipping_method` (`id`, `name`, `code`, `provider`, `fee`, `status`, `sort_order`) VALUES
                (UUID(), '宅配到府', 'HOME_DELIVERY', '黑貓宅急便', 100, 'ACTIVE', 1),
                (UUID(), '7-11 取貨', 'SEVEN_ELEVEN', '統一速達', 60, 'ACTIVE', 2),
                (UUID(), '全家取貨', 'FAMILY_MART', '全家物流', 60, 'ACTIVE', 3)
            """);
            log.info("✅ [021] shipping_method 表建立完成");
        }

        // order 表補欄位（021）
        addColumnIfNotExists("`order`", "shipping_method_id",
            "VARCHAR(36) NULL COMMENT 'FK → shipping_method'", "shipping_method");
        addColumnIfNotExists("`order`", "shipping_fee",
            "BIGINT NOT NULL DEFAULT 0 COMMENT '運費（元）'", "shipping_method_id");
        addColumnIfNotExists("`order`", "payment_method",
            "VARCHAR(30) NOT NULL DEFAULT 'STUB' COMMENT 'STUB / MASTERCARD'", "payment_status");
    }

    // ==================== 027-order-payment-shipping ====================

    private void apply027() {
        // order 表補欄位（027）
        addColumnIfNotExists("`order`", "tracking_url",
            "VARCHAR(500) NULL COMMENT '物流追蹤外部連結'", "tracking_no");
        addColumnIfNotExists("`order`", "gomypay_trade_no",
            "VARCHAR(100) NULL COMMENT 'GoMyPay 交易編號'", "payment_method");

        // shipping_method 表補 tracking_url_template
        if (tableExists("shipping_method")) {
            addColumnIfNotExists("shipping_method", "tracking_url_template",
                "VARCHAR(500) NULL COMMENT '物流查詢 URL 模板，{trackingNo} 替換為實際單號'", "sort_order");

            // 更新追蹤 URL 模板（僅在值為 null 時更新）
            jdbcTemplate.execute("""
                UPDATE shipping_method
                SET tracking_url_template = 'https://www.t-cat.com.tw/Inquire/TraceDetail.aspx?BillID={trackingNo}'
                WHERE code = 'HOME_DELIVERY' AND tracking_url_template IS NULL
            """);
            jdbcTemplate.execute("""
                UPDATE shipping_method
                SET tracking_url_template = 'https://eservice.7-11.com.tw/e-tracking/search.aspx?TBSTKECNO={trackingNo}'
                WHERE code = 'SEVEN_ELEVEN' AND tracking_url_template IS NULL
            """);
        }
    }

    // ==================== 工具方法 ====================

    private boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            // 移除 backtick 以對應 information_schema
            String cleanTable = tableName.replace("`", "");
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class, cleanTable, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDef, String afterColumn) {
        if (!columnExists(tableName, columnName)) {
            String sql = String.format("ALTER TABLE %s ADD COLUMN `%s` %s AFTER `%s`",
                tableName, columnName, columnDef, afterColumn);
            log.info("🔧 [Migration] 新增欄位: {}.{}", tableName, columnName);
            jdbcTemplate.execute(sql);
        }
    }
}
