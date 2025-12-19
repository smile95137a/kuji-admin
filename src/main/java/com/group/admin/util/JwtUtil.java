package com.group.admin.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT 工具類
 * 
 * <p>提供 Token 生成、驗證、解析功能</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Access Token 過期時間（1 天）
     */
    private final long expiration = 1000 * 60 * 60 * 24;
    
    /**
     * Refresh Token 過期時間（30 天）
     */
    private final long refreshExpiration = 1000L * 60 * 60 * 24 * 30;

    /**
     * 生成 Access Token
     * 
     * @param username 使用者帳號
     * @param userId 使用者 ID (UUID)
     * @param userType 使用者類型（admin/user）
     * @param roles 角色列表
     * @return JWT Token
     */
    public String generateToken(String username, String userId, String userType, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("roles", roles);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 生成 Access Token（簡易版，向下相容）
     * 
     * @param username 使用者帳號
     * @return JWT Token
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 生成 Refresh Token
     * 
     * @param username 使用者帳號
     * @return Refresh Token
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 從 Token 取得使用者帳號
     * 
     * @param token JWT Token
     * @return 使用者帳號
     */
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 從 Token 取得使用者 ID
     * 
     * @param token JWT Token
     * @return 使用者 ID (UUID 字串)
     */
    public String getUserId(String token) {
        Object userId = getClaims(token).get("userId");
        return userId != null ? userId.toString() : null;
    }

    /**
     * 從 Token 取得使用者類型
     * 
     * @param token JWT Token
     * @return 使用者類型（admin/user）
     */
    public String getUserType(String token) {
        return (String) getClaims(token).get("userType");
    }

    /**
     * 從 Token 取得角色列表
     * 
     * @param token JWT Token
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) getClaims(token).get("roles");
    }

    /**
     * 驗證 Token 是否有效
     * 
     * @param token JWT Token
     * @return true 如果有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 取得 Access Token 過期時間（秒）
     * 
     * @return 過期時間（秒）
     */
    public long getExpirationSeconds() {
        return expiration / 1000;
    }

    /**
     * 取得 Claims
     * 
     * @param token JWT Token
     * @return Claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
