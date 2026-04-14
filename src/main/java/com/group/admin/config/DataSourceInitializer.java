package com.group.admin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 應用啟動時執行 SQL 遷移的初始化類別
 * 負責 Referral Signup 相關的數據庫遷移
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSourceInitializer implements CommandLineRunner {
    
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("🔍 檢查數據庫遷移狀態...");
        
        try {
            // 檢查是否需要執行遷移（檢查 referral_code 欄位是否存在）
            String checkSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = 'user' 
                AND COLUMN_NAME = 'referral_code' 
                AND TABLE_SCHEMA = DATABASE()
                """;
            
            Integer existingColumns = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (existingColumns != null && existingColumns > 0) {
                log.info("✅ Referral Signup 欄位已存在，跳過遷移");
                return;
            }
            
            log.info("🚀 開始執行 Referral Signup 數據庫遷移...");
            
            // 讀取 SQL 遷移腳本
            String sqlFilePath = "db/migration/V_2026_04_14__add_referral_signup_integration.sql";
            String sqlContent;
            
            try {
                // 首先嘗試從 classpath 讀取
                Resource resource = resourceLoader.getResource("classpath:" + sqlFilePath);
                sqlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                log.debug("✓ 從 classpath 讀取 SQL 檔案");
            } catch (Exception e) {
                // 如果 classpath 失敗，嘗試從檔案系統讀取
                String sqlFileSystemPath = "sql/V_2026_04_14__add_referral_signup_integration.sql";
                try {
                    sqlContent = new String(
                        Files.readAllBytes(Paths.get(sqlFileSystemPath)),
                        StandardCharsets.UTF_8
                    );
                    log.debug("✓ 從檔案系統讀取 SQL 檔案");
                } catch (Exception e2) {
                    log.warn("⚠️ 無法讀取 SQL 遷移檔案，跳過遷移");
                    return;
                }
            }
            
            // 分割 SQL 語句並執行
            String[] statements = sqlContent.split(";");
            int successCount = 0;
            int skipCount = 0;
            int errorCount = 0;
            
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                
                // 跳過空或註解的語句
                if (trimmedStatement.isEmpty() 
                    || trimmedStatement.startsWith("--")
                    || trimmedStatement.startsWith("/*")) {
                    skipCount++;
                    continue;
                }
                
                try {
                    log.debug("執行: {}", trimmedStatement.substring(0, Math.min(50, trimmedStatement.length())));
                    jdbcTemplate.execute(trimmedStatement);
                    successCount++;
                    log.debug("✓ 成功");
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    
                    // 某些錯誤（如已存在）可以忽略
                    if (errorMsg.contains("already exists") || 
                        errorMsg.contains("Duplicate key")) {
                        log.debug("⚠️ 已存在或重複（忽略）: {}", errorMsg.substring(0, Math.min(50, errorMsg.length())));
                        skipCount++;
                    } else {
                        log.warn("❌ 執行失敗: {}", errorMsg);
                        errorCount++;
                    }
                }
            }
            
            log.info("✅ Referral Signup 遷移完成");
            log.info("📊 統計 - 成功: {}, 跳過: {}, 失敗: {}", successCount, skipCount, errorCount);
            
            if (errorCount == 0) {
                log.info("🎉 所有遷移完成，無錯誤");
            }
            
        } catch (Exception e) {
            log.error("❌ 數據庫初始化失敗", e);
            // 不中斷應用啟動
            // throw new RuntimeException("Database initialization failed", e);
        }
    }
}
