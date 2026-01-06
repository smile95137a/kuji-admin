package com.group.admin.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 統一的使用者主體（Principal）
 * 用於 Spring Security 認證資訊
 * 支援前台 User 和後台 AdminUser
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    /**
     * 使用者 ID
     */
    private String userId;
    
    /**
     * 使用者名稱（Email 或 Username）
     */
    private String username;
    
    /**
     * 密碼（通常不需要儲存）
     */
    private String password;
    
    /**
     * 角色列表（例如：Admin, StoreOwner, StoreEditor, USER）
     */
    private List<String> roles;
    
    /**
     * Spring Security 權限列表
     */
    private Collection<? extends GrantedAuthority> authorities;
    
    /**
     * 是否為後台使用者
     */
    private Boolean isAdmin;
    
    /**
     * 是否為前台使用者
     */
    private Boolean isUser;
    
    /**
     * 店家 ID（如果是店家相關角色）
     * @deprecated 使用 storeIds 代替，支援多店家管理
     */
    @Deprecated
    private String storeId;
    
    /**
     * 店家 ID 列表（支援使用者管理多個店家）
     * 如果是 ROLE_ADMIN，此列表可能為空（表示可存取所有店家）
     */
    private List<String> storeIds;
    
    /**
     * 後台管理員物件（如果是後台使用者）
     */
    private Object adminUser;
    
    /**
     * 前台使用者物件（如果是前台使用者）
     */
    private Object user;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    /**
     * 檢查是否擁有某個角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * 檢查是否為管理員
     */
    public boolean isAdminUser() {
        return Boolean.TRUE.equals(isAdmin);
    }
    
    /**
     * 檢查是否為前台使用者
     */
    public boolean isFrontendUser() {
        return Boolean.TRUE.equals(isUser);
    }
}
