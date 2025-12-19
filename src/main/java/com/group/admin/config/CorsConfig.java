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
 * - dev: 允許所有來源（開發方便）
 * - prod: 僅允許指定的前端域名
 */
@Configuration
public class CorsConfig {

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
        
        // 依據環境設定允許的來源
        if ("prod".equals(activeProfile)) {
            // 生產環境：從配置讀取允許的域名
            if (allowedOrigins != null && !allowedOrigins.isBlank()) {
                List<String> origins = Arrays.asList(allowedOrigins.split(","));
                config.setAllowedOrigins(origins);
            } else {
                // 預設的生產環境域名（請替換為實際域名）
                config.setAllowedOrigins(Arrays.asList(
                    "https://kuji.com",
                    "https://www.kuji.com",
                    "https://admin.kuji.com"
                ));
            }
        } else {
            // 開發環境：允許所有來源
            config.addAllowedOriginPattern("*");
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
