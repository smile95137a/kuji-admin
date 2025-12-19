package com.group.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * JWT 配置屬性類
 * 用於從 application.yml 讀取 jwt.* 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT 簽名密鑰
     */
    private String secret;
    
    /**
     * Token 有效期（毫秒），預設 24 小時
     */
    private long expiration = 86400000L;
    
    /**
     * Token 前綴（例如：Bearer）
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * Header 名稱
     */
    private String headerName = "Authorization";
}
