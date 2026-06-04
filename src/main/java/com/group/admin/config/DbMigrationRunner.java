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
        apply032();
        apply033BusinessEventLog();
        fixShippingProviders();
        applyLogisticsV2();

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
                (UUID(), '宅配到府（順豐）', 'HOME_DELIVERY', '順豐速運', 100, 'ACTIVE', 1),
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
                SET tracking_url_template = 'https://www.sf-express.com/tw/tc/dynamic_function/waybill/#search/bill-number/{trackingNo}'
                WHERE code = 'HOME_DELIVERY' AND tracking_url_template IS NULL
            """);
            jdbcTemplate.execute("""
                UPDATE shipping_method
                SET tracking_url_template = 'https://eservice.7-11.com.tw/e-tracking/search.aspx?TBSTKECNO={trackingNo}'
                WHERE code = 'SEVEN_ELEVEN' AND tracking_url_template IS NULL
            """);
        }
    }

    // ==================== fix-shipping-providers ====================

    private void fixShippingProviders() {
        if (!tableExists("shipping_method")) return;
        log.info("🔧 [fix] 修正配送方式物流商名稱（移除綠界標示）...");
        jdbcTemplate.execute("""
            UPDATE shipping_method
            SET provider = '統一超商', updated_at = NOW()
            WHERE code = 'SEVEN_ELEVEN' AND (provider = '綠界' OR provider = '超商')
        """);
        jdbcTemplate.execute("""
            UPDATE shipping_method
            SET provider = '全家便利商店', updated_at = NOW()
            WHERE code = 'FAMILY_MART' AND (provider = '綠界' OR provider = '超商')
        """);
        jdbcTemplate.execute("""
            UPDATE shipping_method
            SET provider = '順豐速運', name = '宅配到府（順豐）', updated_at = NOW()
            WHERE code = 'HOME_DELIVERY' AND (provider = '宅配物流' OR provider IS NULL)
        """);
        log.info("✅ [fix] 配送方式物流商名稱修正完成");
    }

    // ==================== logistics-v2-gomypay-sf ====================

    private void applyLogisticsV2() {
        addColumnIfNotExists("`order`", "logistics_provider",
            "VARCHAR(50) NULL COMMENT '物流商代碼'", "tracking_url");
        addColumnIfNotExists("`order`", "logistics_status_code",
            "VARCHAR(50) NULL COMMENT '物流狀態代碼'", "logistics_provider");
        addColumnIfNotExists("`order`", "logistics_status_name",
            "VARCHAR(100) NULL COMMENT '物流狀態名稱'", "logistics_status_code");
        addColumnIfNotExists("`order`", "logistics_label_url",
            "VARCHAR(500) NULL COMMENT '物流標籤或託運單 URL'", "logistics_status_name");
        addColumnIfNotExists("`order`", "logistics_synced_at",
            "DATETIME NULL COMMENT '物流狀態最後同步時間'", "logistics_label_url");

        if (!tableExists("shipping_method")) {
            return;
        }

        jdbcTemplate.execute("""
            UPDATE shipping_method
            SET name = '宅配到府（順豐）',
                provider = '順豐速運',
                tracking_url_template = 'https://www.sf-express.com/tw/tc/dynamic_function/waybill/#search/bill-number/{trackingNo}',
                updated_at = NOW()
            WHERE code = 'HOME_DELIVERY'
        """);
    }

    // ==================== 032-audit-log-system ====================

    private void apply032() {
        // 五張 log 表，全部使用 CREATE TABLE IF NOT EXISTS 確保幂等
        if (!tableExists("log_auth")) {
            log.info("🔧 [032] 建立 log_auth 表...");
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `log_auth` (
                    `id`           VARCHAR(36)  NOT NULL PRIMARY KEY,
                    `user_id`      VARCHAR(36)  NULL,
                    `user_type`    VARCHAR(20)  NOT NULL,
                    `email`        VARCHAR(255) NULL,
                    `login_method` VARCHAR(30)  NOT NULL,
                    `result`       VARCHAR(10)  NOT NULL,
                    `error_message` VARCHAR(500) NULL,
                    `ip`           VARCHAR(50)  NULL,
                    `user_agent`   VARCHAR(500) NULL,
                    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX `idx_log_auth_user_id`    (`user_id`),
                    INDEX `idx_log_auth_email`      (`email`),
                    INDEX `idx_log_auth_result`     (`result`),
                    INDEX `idx_log_auth_created_at` (`created_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='認證日誌（登入/登出/OAuth）'
            """);
        }

        if (!tableExists("log_draw")) {
            log.info("🔧 [032] 建立 log_draw 表...");
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `log_draw` (
                    `id`             VARCHAR(36)  NOT NULL PRIMARY KEY,
                    `user_id`        VARCHAR(36)  NOT NULL,
                    `lottery_id`     VARCHAR(36)  NOT NULL,
                    `lottery_title`  VARCHAR(200) NULL,
                    `category`       VARCHAR(50)  NULL,
                    `play_mode`      VARCHAR(30)  NULL,
                    `game_mode`      VARCHAR(30)  NULL,
                    `ticket_id`      VARCHAR(36)  NULL,
                    `ticket_number`  INT          NULL,
                    `prize_level`    VARCHAR(20)  NULL,
                    `prize_name`     VARCHAR(200) NULL,
                    `is_grand_prize` TINYINT(1)   DEFAULT 0,
                    `deducted_gold`  BIGINT       DEFAULT 0,
                    `deducted_bonus` BIGINT       DEFAULT 0,
                    `result`         VARCHAR(10)  NOT NULL,
                    `error_message`  VARCHAR(500) NULL,
                    `duration_ms`    INT          NULL,
                    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX `idx_log_draw_user_id`    (`user_id`),
                    INDEX `idx_log_draw_lottery_id` (`lottery_id`),
                    INDEX `idx_log_draw_result`     (`result`),
                    INDEX `idx_log_draw_created_at` (`created_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎日誌'
            """);
        }

        if (!tableExists("log_recharge")) {
            log.info("🔧 [032] 建立 log_recharge 表...");
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `log_recharge` (
                    `id`                  VARCHAR(36)  NOT NULL PRIMARY KEY,
                    `user_id`             VARCHAR(36)  NOT NULL,
                    `recharge_id`         VARCHAR(36)  NULL,
                    `plan_id`             VARCHAR(36)  NULL,
                    `plan_name`           VARCHAR(100) NULL,
                    `amount`              BIGINT       NULL,
                    `gold_added`          BIGINT       DEFAULT 0,
                    `bonus_added`         BIGINT       DEFAULT 0,
                    `payment_method`      VARCHAR(50)  NULL,
                    `payment_gateway_ref` VARCHAR(200) NULL,
                    `result`              VARCHAR(10)  NOT NULL,
                    `error_message`       VARCHAR(500) NULL,
                    `ip`                  VARCHAR(50)  NULL,
                    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX `idx_log_recharge_user_id`     (`user_id`),
                    INDEX `idx_log_recharge_recharge_id` (`recharge_id`),
                    INDEX `idx_log_recharge_result`      (`result`),
                    INDEX `idx_log_recharge_created_at`  (`created_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值日誌'
            """);
        }

        if (!tableExists("log_order")) {
            log.info("🔧 [032] 建立 log_order 表...");
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `log_order` (
                    `id`              VARCHAR(36)  NOT NULL PRIMARY KEY,
                    `operator_id`     VARCHAR(36)  NOT NULL,
                    `operator_type`   VARCHAR(20)  NOT NULL,
                    `order_id`        VARCHAR(36)  NOT NULL,
                    `action`          VARCHAR(50)  NOT NULL,
                    `prize_box_count` INT          NULL,
                    `total_amount`    BIGINT       NULL,
                    `tracking_number` VARCHAR(100) NULL,
                    `result`          VARCHAR(10)  NOT NULL,
                    `error_message`   VARCHAR(500) NULL,
                    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX `idx_log_order_operator_id` (`operator_id`),
                    INDEX `idx_log_order_order_id`    (`order_id`),
                    INDEX `idx_log_order_action`      (`action`),
                    INDEX `idx_log_order_created_at`  (`created_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單操作日誌'
            """);
        }

        if (!tableExists("log_admin_action")) {
            log.info("🔧 [032] 建立 log_admin_action 表...");
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `log_admin_action` (
                    `id`              VARCHAR(36)   NOT NULL PRIMARY KEY,
                    `admin_id`        VARCHAR(36)   NOT NULL,
                    `admin_email`     VARCHAR(255)  NULL,
                    `admin_role`      VARCHAR(50)   NULL,
                    `target_type`     VARCHAR(50)   NOT NULL,
                    `target_id`       VARCHAR(36)   NULL,
                    `target_name`     VARCHAR(200)  NULL,
                    `action`          VARCHAR(50)   NOT NULL,
                    `before_snapshot` MEDIUMTEXT    NULL,
                    `after_snapshot`  MEDIUMTEXT    NULL,
                    `result`          VARCHAR(10)   NOT NULL,
                    `error_message`   VARCHAR(500)  NULL,
                    `ip`              VARCHAR(50)   NULL,
                    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX `idx_log_admin_admin_id`    (`admin_id`),
                    INDEX `idx_log_admin_target_type` (`target_type`),
                    INDEX `idx_log_admin_target_id`   (`target_id`),
                    INDEX `idx_log_admin_action`      (`action`),
                    INDEX `idx_log_admin_created_at`  (`created_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台管理操作日誌'
            """);
        }

        log.info("✅ [032] 稽核日誌表確認完成");
    }

    // ==================== 033-business-event-log ====================

    private void apply033BusinessEventLog() {
        if (tableExists("log_business_event")) {
            return;
        }

        log.info("🔧 [033] 建立 log_business_event 表...");
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS `log_business_event` (
                `id`                    VARCHAR(36)   NOT NULL PRIMARY KEY,
                `event_type`            VARCHAR(50)   NOT NULL COMMENT 'PAYMENT / LOGISTICS / ORDER_STATUS / WALLET',
                `action`                VARCHAR(80)   NOT NULL COMMENT '事件動作',
                `result`                VARCHAR(20)   NOT NULL COMMENT 'SUCCESS / FAILED / PENDING / DUPLICATE / SKIPPED',
                `actor_type`            VARCHAR(30)   NULL COMMENT 'ADMIN / USER / SYSTEM / CALLBACK',
                `actor_id`              VARCHAR(36)   NULL COMMENT '操作者 ID',
                `actor_name`            VARCHAR(200)  NULL COMMENT '操作者名稱快照',
                `target_type`           VARCHAR(50)   NULL COMMENT 'ORDER / RECHARGE / WALLET / LOGISTICS',
                `target_id`             VARCHAR(100)  NULL COMMENT '目標 ID',
                `target_no`             VARCHAR(100)  NULL COMMENT '目標單號',
                `user_id`               VARCHAR(36)   NULL COMMENT '會員 ID',
                `order_id`              VARCHAR(36)   NULL COMMENT '訂單 ID',
                `recharge_id`           VARCHAR(100)  NULL COMMENT '儲值單 ID',
                `wallet_transaction_id` VARCHAR(36)   NULL COMMENT '錢包交易 ID',
                `external_provider`     VARCHAR(50)   NULL COMMENT '外部服務商',
                `external_ref`          VARCHAR(200)  NULL COMMENT '外部交易/物流參考號',
                `amount`                BIGINT        NULL COMMENT '金額或點數異動量',
                `payment_method`        VARCHAR(50)   NULL COMMENT '付款方式',
                `before_status`         VARCHAR(50)   NULL COMMENT '變更前狀態',
                `after_status`          VARCHAR(50)   NULL COMMENT '變更後狀態',
                `before_snapshot`       MEDIUMTEXT    NULL COMMENT '操作前快照 JSON',
                `after_snapshot`        MEDIUMTEXT    NULL COMMENT '操作後快照 JSON',
                `callback_summary`      MEDIUMTEXT    NULL COMMENT '遮罩後 callback 摘要 JSON',
                `raw_payload_hash`      VARCHAR(64)   NULL COMMENT '原始 payload SHA-256',
                `error_message`         VARCHAR(500)  NULL COMMENT '錯誤訊息',
                `ip`                    VARCHAR(50)   NULL COMMENT '來源 IP',
                `user_agent`            VARCHAR(500)  NULL COMMENT 'User-Agent',
                `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                INDEX `idx_lbe_event_type` (`event_type`),
                INDEX `idx_lbe_action` (`action`),
                INDEX `idx_lbe_result` (`result`),
                INDEX `idx_lbe_actor_id` (`actor_id`),
                INDEX `idx_lbe_user_id` (`user_id`),
                INDEX `idx_lbe_order_id` (`order_id`),
                INDEX `idx_lbe_recharge_id` (`recharge_id`),
                INDEX `idx_lbe_external_ref` (`external_ref`),
                INDEX `idx_lbe_created_at` (`created_at`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='業務事件日誌'
        """);
        log.info("✅ [033] log_business_event 表建立完成");
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
