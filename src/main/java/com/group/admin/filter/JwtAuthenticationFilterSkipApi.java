// package com.group.admin.filter;

// import java.io.IOException;

// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// public class JwtAuthenticationFilterSkipApi extends UsernamePasswordAuthenticationFilter {

//     private final JwtAuthenticationFilter originalFilter;

//     public JwtAuthenticationFilterSkipApi(JwtAuthenticationFilter originalFilter) {
//         this.originalFilter = originalFilter;
//     }

//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//             throws IOException, ServletException {

//         String path = request.getRequestURI();
//         if (path.startsWith("/api/")) {
//             chain.doFilter(request, response); // /api/** 直接放行，不驗證
//         } else {
//             originalFilter.doFilter(request, response, chain); // 其他路徑才走 JWT
//         }
//     }
// }
