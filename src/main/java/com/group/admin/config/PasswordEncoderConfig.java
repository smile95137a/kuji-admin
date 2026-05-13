package com.group.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密碼加密設定
 *
 * 將 PasswordEncoder 獨立出來，避免 UserServiceImpl 依賴 SecurityConfig 造成循環依賴。
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 密碼加密器
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}