package com.group.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 跨域配置
 * 
 * 依據不同環境配置允許的來源：
 * - dev: 允許所有來源或指定的本地端口
 * - prod: 僅允許指定的前端域名
 */
@Configuration
public class CorsConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CorsConfig.class);

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允許的 HTTP Headers
        config.addAllowedHeader("*");
        
        // 允許的 HTTP Methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // 允許攜帶 Cookie（用於 JWT 認證）
        config.setAllowCredentials(true);
        
        // 預檢請求的有效期（秒）
        config.setMaxAge(3600L);
        
        log.info("🔧 [CORS] Active Profile: {}", activeProfile);
        log.info("🔧 [CORS] Allowed Origins Config: {}", allowedOrigins);
        
        // 依據環境設定允許的來源
        if ("prod".equals(activeProfile)) {
            // 生產環境：從配置讀取允許的域名
            if (allowedOrigins != null && !allowedOrigins.isBlank()) {
                List<String> origins = Arrays.asList(allowedOrigins.split(","));
                config.setAllowedOrigins(origins);
                log.info("🔧 [CORS] Prod - Using configured origins: {}", origins);
            } else {
                // 預設的生產環境域名（請替換為實際域名）
                List<String> defaultOrigins = Arrays.asList(
                    "https://kuji.com",
                    "https://www.kuji.com",
                    "https://admin.kuji.com",
                    "http://18.179.187.129"
                );
                config.setAllowedOrigins(defaultOrigins);
                log.info("🔧 [CORS] Prod - Using default origins: {}", defaultOrigins);
            }
        } else {
            // 開發環境
            if (allowedOrigins != null && !allowedOrigins.isBlank()) {
                // 如果有配置，使用配置的來源
                List<String> origins = Arrays.asList(allowedOrigins.split(","));
                config.setAllowedOrigins(origins);
                log.info("🔧 [CORS] Dev - Using configured origins: {}", origins);
            } else {
                // 沒有配置則允許所有來源
                config.addAllowedOriginPattern("*");
                log.info("🔧 [CORS] Dev - Allowing all origins with pattern *");
            }
        }
        
        // 暴露的 Response Headers（前端可讀取）
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Request-Id",
            "X-Total-Count"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
