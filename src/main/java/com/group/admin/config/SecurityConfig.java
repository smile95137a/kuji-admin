package com.group.admin.config;

import com.group.admin.security.AdminJwtAuthenticationFilter;
import com.group.admin.security.ApiJwtAuthenticationFilter;
import com.group.admin.security.ApiOAuth2AuthenticationFailureHandler;
import com.group.admin.security.ApiOAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AdminJwtAuthenticationFilter adminJwtFilter;
    private final ApiJwtAuthenticationFilter apiJwtFilter;
    private final ApiOAuth2AuthenticationSuccessHandler apiOAuth2AuthenticationSuccessHandler;
    private final ApiOAuth2AuthenticationFailureHandler apiOAuth2AuthenticationFailureHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STORE_OWNER", "STORE_EDITOR"))
                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/district/**").permitAll()
                        .requestMatchers("/api/marquee/**").permitAll()
                        .requestMatchers("/api/ws/**").permitAll()
                        .requestMatchers("/api/recharge-plan/**").permitAll()
                        .requestMatchers("/api/recharge-plans/**").permitAll()
                        .requestMatchers("/api/recharge/payment-methods").permitAll()
                        .requestMatchers("/api/shipping-methods/**").permitAll()
                        .requestMatchers("/api/payment/shipping/callback", "/api/payment/shipping/callback/**")
                        .permitAll()
                        .requestMatchers("/api/wallet/recharge/callback", "/api/wallet/recharge/callback/**")
                        .permitAll()
                        .requestMatchers("/api/stores/list").permitAll()
                        .requestMatchers("/api/stores", "/api/stores/**").permitAll()
                        .requestMatchers("/api/lottery/list").permitAll()
                        .requestMatchers("/api/lottery/browse/**").permitAll()
                        .requestMatchers("/api/news/published").permitAll()
                        .requestMatchers("/api/banners").permitAll()
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN", "STORE_OWNER", "STORE_EDITOR"))
                .addFilterBefore(apiJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(apiOAuth2AuthenticationSuccessHandler)
                        .failureHandler(apiOAuth2AuthenticationFailureHandler));

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<ApiJwtAuthenticationFilter> apiJwtAuthenticationFilterRegistration(
            ApiJwtAuthenticationFilter filter) {
        FilterRegistrationBean<ApiJwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AdminJwtAuthenticationFilter> adminJwtAuthenticationFilterRegistration(
            AdminJwtAuthenticationFilter filter) {
        FilterRegistrationBean<AdminJwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
