package com.group.admin.config;

import com.group.admin.security.AdminJwtAuthenticationFilter;
import com.group.admin.security.ApiJwtAuthenticationFilter;
import com.group.admin.security.ApiOAuth2AuthenticationFailureHandler;
import com.group.admin.security.ApiOAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // OAuth2 / Swagger / health
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // Admin auth
                        .requestMatchers("/admin/auth/**").permitAll()

                        // Frontend auth
                        .requestMatchers("/auth/**").permitAll()

                        // Public frontend APIs
                        .requestMatchers("/district/**").permitAll()
                        .requestMatchers("/marquee/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/recharge-plan/**").permitAll()
                        .requestMatchers("/recharge-plans/**").permitAll()
                        .requestMatchers("/recharge/payment-methods").permitAll()
                        .requestMatchers("/shipping-methods/**").permitAll()
                        .requestMatchers("/payment/shipping/callback", "/payment/shipping/callback/**").permitAll()
                        .requestMatchers("/wallet/recharge/callback", "/wallet/recharge/callback/**").permitAll()
                        .requestMatchers("/payment/shipping/result", "/payment/shipping/result/**").permitAll()
                        .requestMatchers("/wallet/recharge/result", "/wallet/recharge/result/**").permitAll()
                        .requestMatchers("/stores/list").permitAll()
                        .requestMatchers("/stores", "/stores/**").permitAll()
                        .requestMatchers("/lottery/list").permitAll()
                        .requestMatchers("/lottery/browse/**").permitAll()
                        .requestMatchers("/news/published").permitAll()
                        .requestMatchers("/banners").permitAll()

                        // Protected admin APIs
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STORE_OWNER", "STORE_EDITOR")

                        // Protected frontend APIs
                        .requestMatchers(
                                "/user/**",
                                "/order/**",
                                "/orders/**",
                                "/draw/**",
                                "/random-draw/**",
                                "/lottery-lock/**",
                                "/prize-box/**",
                                "/recharge/**",
                                "/wallet/**",
                                "/consumption-record/**",
                                "/referral/**",
                                "/user-address/**"
                        ).hasAnyRole("USER", "ADMIN", "STORE_OWNER", "STORE_EDITOR")

                        // Everything else can stay open unless controller/method security says otherwise.
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(apiOAuth2AuthenticationSuccessHandler)
                        .failureHandler(apiOAuth2AuthenticationFailureHandler))
                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiJwtFilter, UsernamePasswordAuthenticationFilter.class);

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
