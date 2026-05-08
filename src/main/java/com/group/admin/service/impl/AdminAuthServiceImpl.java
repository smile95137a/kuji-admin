package com.group.admin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group.admin.constants.ErrorCodes;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.entity.StoreUser;
import com.group.admin.enums.AdminUserStatus;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.auth.AdminLoginReq;
import com.group.admin.req.auth.ChangePasswordReq;
import com.group.admin.req.auth.RefreshTokenReq;
import com.group.admin.res.auth.LoginRes;
import com.group.admin.security.UserPrincipal;
import com.group.admin.service.AdminAuthService;
import com.group.admin.service.EmailService;
import com.group.admin.util.AuditContext;
import com.group.admin.util.JwtUtil;
import com.group.admin.util.PasswordUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 後台認證服務實作
 * 
 * <p>實作後台帳號的登入、登出、Token 管理</p>
 * <p>使用 MyBatis Example 模式進行資料存取</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;
    private final StoreUserMapper storeUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final com.group.admin.service.TokenBlacklistService tokenBlacklistService;
    private final PasswordUtil passwordUtil;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public LoginRes login(AdminLoginReq req) {
        log.debug("後台登入請求：username={}", req.getUsername());
        
        // 查詢使用者 (使用 Example)
        AdminUserExample example = new AdminUserExample();
        example.createCriteria().andUsernameEqualTo(req.getUsername());
        List<AdminUser> users = adminUserMapper.selectByExample(example);
        if (users.isEmpty()) {
            log.warn("登入失敗：帳號不存在 username={}", req.getUsername());
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "帳號或密碼錯誤");
        }
        AdminUser user = users.get(0);
        AuditContext.setAuthResolvedUser(user.getId(), user.getEmail(), user.getUsername(), "ADMIN");

        // 帳號鎖定檢查
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            throw new BusinessException(ErrorCodes.AUTH_ACCOUNT_LOCKED, "帳號已鎖定，請稍後再試");
        }

        // 驗證密碼
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("登入失敗：密碼錯誤 username={}", req.getUsername());
            int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            AdminUser lockUpdate = new AdminUser();
            lockUpdate.setId(user.getId());
            lockUpdate.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                lockUpdate.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                log.warn("🔒 後台帳號已鎖定 15 分鐘: username={}", req.getUsername());
            }
            adminUserMapper.updateByPrimaryKeySelective(lockUpdate);
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "帳號或密碼錯誤");
        }
        
        // 檢查帳號狀態
        AdminUserStatus status = AdminUserStatus.fromCode(user.getStatus());
        if (status == AdminUserStatus.INACTIVE) {
            log.warn("登入失敗：帳號已停用 username={}", req.getUsername());
            throw new BusinessException(ErrorCodes.AUTH_ACCOUNT_DISABLED, "帳號已停用");
        }
        
        // 重設失敗次數
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        // 取得角色列表
        List<String> roles = getUserRoles(user.getId());
        
        // 取得店家 ID 列表
        List<String> storeIds = getUserStoreIds(user.getId());
        
        // 更新最後登入時間
        user.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKey(user);
        
        // 生成 Token（包含 storeIds 與 gen）
        long gen = tokenBlacklistService.getCurrentGen(user.getId());
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), "admin", roles, storeIds, gen);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        
        log.info("後台登入成功：username={}, userId={}, storeIds={}", user.getUsername(), user.getId(), storeIds);
        
        return buildLoginRes(user, accessToken, refreshToken, roles);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        log.info("📧 後台忘記密碼請求: email={}", email);

        AdminUserExample example = new AdminUserExample();
        example.createCriteria().andEmailEqualTo(email);
        List<AdminUser> users = adminUserMapper.selectByExample(example);

        // 不暴露帳號是否存在。
        if (users.isEmpty()) {
            log.warn("⚠️ 後台忘記密碼：帳號不存在但不回錯: email={}", email);
            return;
        }

        AdminUser user = users.get(0);
        String temporaryPassword = passwordUtil.generateRandomPassword();

        AdminUser update = new AdminUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(temporaryPassword));
        update.setForceChangePassword(true);
        update.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKeySelective(update);

        String displayName = user.getDisplayName() != null && !user.getDisplayName().isBlank()
                ? user.getDisplayName()
                : user.getUsername();
        emailService.sendTemporaryPasswordEmail(
            user.getEmail(),
            displayName,
            temporaryPassword,
            frontendUrl + "/admin/login",
            "後台忘記密碼");

        log.info("✅ 後台忘記密碼完成，已寄送臨時密碼: userId={}", user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public LoginRes firstLoginChangePassword(ChangePasswordReq req) {
        String userId = getCurrentUserId();
        log.debug("首次登入修改密碼：userId={}", userId);
        
        AdminUser user = adminUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }
        
        // 檢查是否需要首次修改密碼
        if (!Boolean.TRUE.equals(user.getForceChangePassword())) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "非首次登入，請使用一般修改密碼功能");
        }
        
        // 驗證舊密碼
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "原密碼錯誤");
        }
        
        // 更新密碼
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setForceChangePassword(false);
        user.setStatus(AdminUserStatus.ACTIVE.getCode());
        user.setUpdatedBy(userId);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKey(user);
        
        // 撤銷舊 Token
        tokenBlacklistService.invalidateUser(userId);
        
        // 取得角色列表
        List<String> roles = getUserRoles(user.getId());
        
        // 取得店家 ID 列表
        List<String> storeIds = getUserStoreIds(user.getId());
        
        // 生成新 Token（包含 storeIds 與 gen）
        long gen = tokenBlacklistService.getCurrentGen(user.getId());
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), "admin", roles, storeIds, gen);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        
        log.info("首次登入修改密碼成功：userId={}, storeIds={}", userId, storeIds);
        
        return buildLoginRes(user, accessToken, refreshToken, roles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordReq req) {
        String userId = getCurrentUserId();
        log.debug("修改密碼：userId={}", userId);
        
        AdminUser user = adminUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }
        
        // 驗證舊密碼
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "原密碼錯誤");
        }
        
        // 更新密碼
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedBy(userId);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKey(user);
        
        log.info("修改密碼成功：userId={}", userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginRes refreshToken(RefreshTokenReq req) {
        log.debug("刷新 Token");
        
        // 驗證 Refresh Token
        if (!jwtUtil.validateToken(req.getRefreshToken())) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "Refresh Token 無效或已過期");
        }
        
        // 取得使用者帳號 (使用 Example)
        String username = jwtUtil.getUsername(req.getRefreshToken());
        AdminUserExample example = new AdminUserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<AdminUser> users = adminUserMapper.selectByExample(example);
        if (users.isEmpty()) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }
        AdminUser user = users.get(0);
        
        // 檢查帳號狀態
        AdminUserStatus status = AdminUserStatus.fromCode(user.getStatus());
        if (status == AdminUserStatus.INACTIVE) {
            throw new BusinessException(ErrorCodes.AUTH_ACCOUNT_DISABLED, "帳號已停用");
        }
        
        // 取得角色列表
        List<String> roles = getUserRoles(user.getId());
        
        // 取得店家 ID 列表
        List<String> storeIds = getUserStoreIds(user.getId());
        
        // 生成新 Token（包含 storeIds 與 gen）
        long gen = tokenBlacklistService.getCurrentGen(user.getId());
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), "admin", roles, storeIds, gen);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        
        log.info("刷新 Token 成功：username={}, storeIds={}", username, storeIds);
        
        return buildLoginRes(user, accessToken, refreshToken, roles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout() {
        // 目前採用無狀態 JWT，登出由前端刪除 Token 實現
        // 若需要伺服器端撤銷，可實作 Token 黑名單機制
        log.info("使用者登出：userId={}", getCurrentUserId());
        SecurityContextHolder.clearContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "未登入或 Token 無效");
        }
        
        // 從 principal 取得 userId
        Object principal = auth.getPrincipal();
        
        // 若 principal 為 UserPrincipal
        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            return userPrincipal.getUserId();
        }
        
        // 若 principal 為 String（username），則需查詢 (使用 Example)
        String username = principal.toString();
        AdminUserExample example = new AdminUserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<AdminUser> users = adminUserMapper.selectByExample(example);
        if (users.isEmpty()) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }
        return users.get(0).getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "未登入或 Token 無效");
        }
        return auth.getName();
    }

    /**
     * 取得使用者角色列表 (使用 Example)
     * 
     * @param userId 使用者 ID (UUID)
     * @return 角色代碼列表
     */
    private List<String> getUserRoles(String userId) {
        AdminUserRoleExample example = new AdminUserRoleExample();
        example.createCriteria().andAdminUserIdEqualTo(userId);
        List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(example);
        return userRoles.stream()
                .map(ur -> {
                    Role role = roleMapper.selectByPrimaryKey(ur.getRoleId());
                    return role != null ? role.getCode() : null;
                })
                .filter(code -> code != null)
                .collect(Collectors.toList());
    }

    /**
     * 取得使用者店家 ID 列表
     * 
     * @param adminUserId Admin 使用者 ID (UUID)
     * @return 店家 ID 列表（可能為空）
     */
    private List<String> getUserStoreIds(String adminUserId) {
        StoreUserExample example = new StoreUserExample();
        example.createCriteria().andAdminUserIdEqualTo(adminUserId);
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
        return storeUsers.stream()
                .map(StoreUser::getStoreId)
                .collect(Collectors.toList());
    }

    /**
     * 建構登入回應
     * 
     * @param user 使用者實體
     * @param accessToken Access Token
     * @param refreshToken Refresh Token
     * @param roles 角色列表
     * @return 登入回應
     */
    private LoginRes buildLoginRes(AdminUser user, String accessToken, String refreshToken, List<String> roles) {
        LoginRes.UserInfo userInfo = LoginRes.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .roles(roles)
                .build();

        return LoginRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationSeconds())
                .forceChangePassword(Boolean.TRUE.equals(user.getForceChangePassword()))
                .user(userInfo)
                .build();
    }
}
