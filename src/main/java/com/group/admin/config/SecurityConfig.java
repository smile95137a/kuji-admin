package com.group.admin.config;

import com.group.admin.security.AdminJwtAuthenticationFilter;
import com.group.admin.security.ApiJwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * 分離前台（/api/**）和後台（/admin/**）的認證機制
 * 
 * 前台：支援 Email + Google OAuth2
 * 後台：僅支援 Email + 密碼
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AdminJwtAuthenticationFilter adminJwtFilter;
    private final ApiJwtAuthenticationFilter apiJwtFilter;

    /**
     * 密碼加密器（使用 BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 後台安全配置（/admin/**）
     * Order(1) 表示優先處理
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        // 登入相關 API 不需要認證
                        .requestMatchers("/admin/auth/**").permitAll()
                        // 其他 /admin/** 需要後台角色（注意：Spring Security 會自動移除 ROLE_ 前綴）
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STORE_OWNER", "STORE_EDITOR")
                )
                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * 前台安全配置（/api/**）
     * Order(2) 表示次要處理
     * 支援 OAuth2 登入
     * 同時支援前台 USER 和後台 Admin/StoreOwner/StoreEditor 角色
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        // 登入、註冊、OAuth 不需要認證
                        .requestMatchers("/api/auth/**", "/login/oauth2/**").permitAll()
                        // 其他 /api/** 需要 USER 或後台管理角色
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN", "STORE_OWNER", "STORE_EDITOR")
                )
                // ⚠️ 暫時停用 OAuth2 登入（需要先配置 OAuth2 Provider）
                // .oauth2Login(oauth2 -> oauth2
                //         .loginPage("/api/auth/login")
                //         .defaultSuccessUrl("/api/auth/oauth2/success", true)
                //         .failureUrl("/api/auth/oauth2/failure")
                // )
                .addFilterBefore(apiJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * 預設安全配置（處理其他路徑）
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Swagger 相關路徑
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
