// package com.group.admin.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// import com.group.admin.filter.JwtAuthenticationFilter;
// import com.group.admin.filter.JwtAuthenticationFilterSkipApi;

// @Configuration
// public class SecurityConfig {

//     private final JwtAuthenticationFilter jwtFilter;

//     public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
//         this.jwtFilter = jwtFilter;
//     }

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//                 .csrf(csrf -> csrf.disable())
//                 .cors(cors -> {
//                 }) // CorsFilter 已經註冊了，這裡空著即可
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers("/**").permitAll() // 放行所有 /api
//                         .anyRequest().authenticated() // 其他請求需要驗證
//                 )
//                 // 不把 jwtFilter 加在 /api/** 上，避免攔截全放行路徑
//                 .addFilterBefore(new JwtAuthenticationFilterSkipApi(jwtFilter),
//                         UsernamePasswordAuthenticationFilter.class)
//                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//         return http.build();
//     }
// }
