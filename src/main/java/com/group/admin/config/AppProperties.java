package com.group.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 應用程式配置屬性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    /**
     * 資料初始化配置
     */
    private DataConfig data = new DataConfig();
    
    @Data
    public static class DataConfig {
        /**
         * 是否啟用自動初始化
         * - true: 首次啟動時自動建立基本資料
         * - false: 不自動初始化（需手動執行 SQL）
         */
        private boolean autoInit = false;
    }
}
