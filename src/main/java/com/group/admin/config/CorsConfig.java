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
        
        // 使用 AllowedOriginPatterns 而非 AllowedOrigins（支援 credentials）
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            // 使用 setAllowedOriginPatterns 以支援 credentials
            config.setAllowedOriginPatterns(origins);
            log.info("🔧 [CORS] Using configured origin patterns: {}", origins);
        } else {
            // 預設允許所有來源（開發環境）
            config.addAllowedOriginPattern("*");
            log.info("🔧 [CORS] Allowing all origins with pattern *");
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
