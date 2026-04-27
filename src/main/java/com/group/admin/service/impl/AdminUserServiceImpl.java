package com.group.admin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group.admin.constants.ErrorCodes;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.enums.AdminUserStatus;
import com.group.admin.enums.RoleCode;
import com.group.admin.enums.StoreStatus;
import com.group.admin.enums.StoreUserRoleType;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.example.RoleExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.admin.AdminUserCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.admin.ChangePasswordReq;
import com.group.admin.req.admin.CreateStoreEditorReq;
import com.group.admin.req.admin.CreateStoreOwnerReq;
import com.group.admin.req.admin.UpdateAdminUserReq;
import com.group.admin.res.admin.AdminUserRes;
import com.group.admin.service.AdminAuthService;
import com.group.admin.service.AdminUserService;
import com.group.admin.util.PasswordUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 後台帳號管理服務實作
 * 
 * <p>
 * 實作 Admin 管理 StoreOwner、StoreEditor 帳號的功能
 * </p>
 * <p>
 * 使用 MyBatis Example 模式進行資料存取
 * </p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;
    private final StoreMapper storeMapper;
    private final StoreUserMapper storeUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil;
    private final AdminAuthService adminAuthService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdminUserRes createStoreOwner(CreateStoreOwnerReq req) {
        log.info("建立店家負責人帳號：email={}, storeName={}", req.getEmail(), req.getStoreName());
        String currentUserId = adminAuthService.getCurrentUserId();

        // 檢查 Email 是否重複 (使用 Example)
        AdminUserExample emailExample = new AdminUserExample();
        emailExample.createCriteria().andEmailEqualTo(req.getEmail());
        if (!adminUserMapper.selectByExample(emailExample).isEmpty()) {
            throw new BusinessException(ErrorCodes.USER_EMAIL_EXISTS, "Email 已被使用");
        }

        // 生成初始密碼
        String initialPassword = passwordUtil.generateRandomPassword();

        // 建立 AdminUser
        AdminUser adminUser = new AdminUser();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setUsername(req.getEmail());
        adminUser.setEmail(req.getEmail());
        adminUser.setPassword(passwordEncoder.encode(initialPassword));
        adminUser.setDisplayName(req.getDisplayName());
        adminUser.setPhone(req.getPhone());
        adminUser.setStatus(AdminUserStatus.PENDING.getCode());
        adminUser.setForceChangePassword(true);
        adminUser.setCreatedBy(currentUserId);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setRemark(req.getRemark());
        adminUser.setFailedLoginAttempts(0);
        adminUserMapper.insert(adminUser);

        // 綁定 StoreOwner 角色
        bindRole(adminUser.getId(), RoleCode.ROLE_STORE_OWNER);

        // 建立 Store（完整資料）
        Store store = new Store();
        store.setId(UUID.randomUUID().toString());
        store.setOwnerId(adminUser.getId());
        store.setStoreName(req.getStoreName());
        store.setShortDescription(req.getShortDescription());
        store.setLongDescription(req.getLongDescription());
        store.setLogoUrl(req.getLogoUrl());
        store.setCoverImageUrl(req.getCoverImageUrl());
        store.setEmail(req.getStoreEmail());
        store.setPhone(req.getStorePhone());
        store.setAddress(req.getStoreAddress());
        store.setBusinessHours(req.getBusinessHours());
        store.setFacebookUrl(req.getFacebookUrl());
        store.setInstagramUrl(req.getInstagramUrl());
        store.setLineId(req.getLineId());
        store.setStatus(StoreStatus.ACTIVE.getCode());
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.insert(store);

        // 建立 StoreUser 關聯
        StoreUser storeUser = new StoreUser();
        storeUser.setId(UUID.randomUUID().toString());
        storeUser.setStoreId(store.getId());
        storeUser.setAdminUserId(adminUser.getId());
        storeUser.setRoleType(StoreUserRoleType.OWNER.getCode());
        storeUser.setCreatedAt(LocalDateTime.now());
        storeUserMapper.insert(storeUser);

        log.info("店家負責人帳號建立成功：userId={}, storeId={}, 初始密碼={}",
                adminUser.getId(), store.getId(), initialPassword);

        // TODO: 發送 Email 通知初始密碼

        return toAdminUserRes(adminUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdminUserRes createStoreEditor(CreateStoreEditorReq req) {
        log.info("建立店家編輯人員帳號：email={}, storeId={}", req.getEmail(), req.getStoreId());
        String currentUserId = adminAuthService.getCurrentUserId();

        // 檢查店家是否存在
        Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
        if (store == null) {
            throw new BusinessException(ErrorCodes.STORE_NOT_FOUND, "店家不存在");
        }

        // 檢查 Email 是否重複 (使用 Example)
        AdminUserExample emailExample = new AdminUserExample();
        emailExample.createCriteria().andEmailEqualTo(req.getEmail());
        if (!adminUserMapper.selectByExample(emailExample).isEmpty()) {
            throw new BusinessException(ErrorCodes.USER_EMAIL_EXISTS, "Email 已被使用");
        }

        // 生成初始密碼
        String initialPassword = passwordUtil.generateRandomPassword();

        // 建立 AdminUser
        AdminUser adminUser = new AdminUser();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setUsername(req.getEmail());
        adminUser.setEmail(req.getEmail());
        adminUser.setPassword(passwordEncoder.encode(initialPassword));
        adminUser.setDisplayName(req.getDisplayName());
        adminUser.setPhone(req.getPhone());
        adminUser.setStatus(AdminUserStatus.PENDING.getCode());
        adminUser.setForceChangePassword(true);
        adminUser.setCreatedBy(currentUserId);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setRemark(req.getRemark());
        adminUser.setFailedLoginAttempts(0);
        adminUserMapper.insert(adminUser);

        // 綁定 StoreEditor 角色
        bindRole(adminUser.getId(), RoleCode.ROLE_STORE_EDITOR);

        // 建立 StoreUser 關聯
        StoreUser storeUser = new StoreUser();
        storeUser.setId(UUID.randomUUID().toString());
        storeUser.setStoreId(req.getStoreId());
        storeUser.setAdminUserId(adminUser.getId());
        storeUser.setRoleType(StoreUserRoleType.EDITOR.getCode());
        storeUser.setCreatedAt(LocalDateTime.now());
        storeUserMapper.insert(storeUser);

        log.info("店家編輯人員帳號建立成功：userId={}, storeId={}, 初始密碼={}",
                adminUser.getId(), req.getStoreId(), initialPassword);

        // TODO: 發送 Email 通知初始密碼

        return toAdminUserRes(adminUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AdminUserRes getAdminUser(String userId) {
        AdminUser adminUser = adminUserMapper.selectByPrimaryKey(userId);
        if (adminUser == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }
        return toAdminUserRes(adminUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdminUserRes> getAllAdminUsers() {
        AdminUserExample example = new AdminUserExample();
        List<AdminUser> users = adminUserMapper.selectByExample(example);
        return users.stream()
                .map(this::toAdminUserRes)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdminUserRes> getAdminUsersByStore(String storeId) {
        // 使用 Example 查詢
        StoreUserExample example = new StoreUserExample();
        example.createCriteria().andStoreIdEqualTo(storeId);
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
        List<AdminUserRes> result = new ArrayList<>();

        for (StoreUser su : storeUsers) {
            AdminUser user = adminUserMapper.selectByPrimaryKey(su.getAdminUserId());
            if (user != null) {
                result.add(toAdminUserRes(user));
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void activateAdminUser(String userId) {
        log.info("啟用帳號：userId={}", userId);
        String currentUserId = adminAuthService.getCurrentUserId();

        AdminUser user = adminUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }

        user.setStatus(AdminUserStatus.ACTIVE.getCode());
        user.setUpdatedBy(currentUserId);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKey(user);

        log.info("帳號已啟用：userId={}", userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deactivateAdminUser(String userId) {
        log.info("停用帳號：userId={}", userId);
        String currentUserId = adminAuthService.getCurrentUserId();

        AdminUser user = adminUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }

        user.setStatus(AdminUserStatus.INACTIVE.getCode());
        user.setUpdatedBy(currentUserId);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKey(user);

        log.info("帳號已停用：userId={}", userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String resetPassword(String userId) {
        log.info("重設密碼：userId={}", userId);
        String currentUserId = adminAuthService.getCurrentUserId();

        AdminUser user = adminUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }

        // 生成新密碼
        String newPassword = passwordUtil.generateRandomPassword();

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForceChangePassword(true);
        user.setUpdatedBy(currentUserId);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKey(user);

        log.info("密碼重設成功：userId={}, 新密碼={}", userId, newPassword);

        // TODO: 發送 Email 通知新密碼

        return newPassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAdminUser(String userId) {
        log.info("刪除帳號（停用）：userId={}", userId);
        deactivateAdminUser(userId);
    }

    // ========== 013-store-account-mgmt new implementations ==========

    @Override
    @Transactional
    public AdminUserRes updateAdminUser(String userId, UpdateAdminUserReq req, String operatorId) {
        log.info("📝 更新帳號資料：userId={}, operatorId={}", userId, operatorId);

        AdminUser user = adminUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }

        if (req.getDisplayName() != null) {
            user.setDisplayName(req.getDisplayName());
        }
        if (req.getEmail() != null) {
            // Check email uniqueness
            AdminUserExample emailCheck = new AdminUserExample();
            emailCheck.createCriteria().andEmailEqualTo(req.getEmail()).andIdNotEqualTo(userId);
            if (!adminUserMapper.selectByExample(emailCheck).isEmpty()) {
                throw new BusinessException(ErrorCodes.USER_EMAIL_EXISTS, "Email 已被使用");
            }
            user.setEmail(req.getEmail());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        user.setUpdatedBy(operatorId);
        user.setUpdatedAt(LocalDateTime.now());

        adminUserMapper.updateByPrimaryKeySelective(user);
        log.info("✅ 帳號資料更新成功：userId={}", userId);

        return toAdminUserRes(user);
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordReq req) {
        log.info("🔑 修改密碼：userId={}", userId);

        AdminUser user = adminUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCodes.USER_OLD_PASSWORD_WRONG, "舊密碼錯誤");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setForceChangePassword(false);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKeySelective(user);

        log.info("✅ 密碼修改成功：userId={}", userId);
    }

    @Override
    public List<AdminUserRes> queryAdminUsers(QueryReq<AdminUserCondition> req) {
        AdminUserCondition condition = req != null ? req.getCondition() : null;

        // 1. 如果指定了 storeId，先從 store_user 取出符合的 adminUserIds
        List<String> storeFilteredIds = null;
        if (condition != null && condition.getStoreId() != null && !condition.getStoreId().isEmpty()) {
            StoreUserExample storeUserExample = new StoreUserExample();
            storeUserExample.createCriteria().andStoreIdEqualTo(condition.getStoreId());
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);
            storeFilteredIds = storeUsers.stream()
                    .map(StoreUser::getAdminUserId)
                    .collect(Collectors.toList());
            if (storeFilteredIds.isEmpty()) {
                return new ArrayList<>();
            }
        }

        // 2. 如果指定了 roleCode，先從 admin_user_role 取出符合的 adminUserIds
        List<String> roleFilteredIds = null;
        if (condition != null && condition.getRoleCode() != null && !condition.getRoleCode().isEmpty()) {
            RoleExample roleExample = new RoleExample();
            roleExample.createCriteria().andCodeEqualTo(condition.getRoleCode());
            List<Role> roles = roleMapper.selectByExample(roleExample);
            if (roles.isEmpty()) {
                return new ArrayList<>();
            }
            String roleId = roles.get(0).getId();
            AdminUserRoleExample adminUserRoleExample = new AdminUserRoleExample();
            adminUserRoleExample.createCriteria().andRoleIdEqualTo(roleId);
            List<AdminUserRole> adminUserRoles = adminUserRoleMapper.selectByExample(adminUserRoleExample);
            roleFilteredIds = adminUserRoles.stream()
                    .map(AdminUserRole::getAdminUserId)
                    .collect(Collectors.toList());
            if (roleFilteredIds.isEmpty()) {
                return new ArrayList<>();
            }
        }

        // 3. 合併 ID 過濾（storeId + roleCode 取交集）
        List<String> combinedIds = null;
        if (storeFilteredIds != null && roleFilteredIds != null) {
            combinedIds = storeFilteredIds.stream()
                    .filter(roleFilteredIds::contains)
                    .collect(Collectors.toList());
            if (combinedIds.isEmpty()) {
                return new ArrayList<>();
            }
        } else if (storeFilteredIds != null) {
            combinedIds = storeFilteredIds;
        } else if (roleFilteredIds != null) {
            combinedIds = roleFilteredIds;
        }

        // 4. 建立 AdminUser 查詢條件
        AdminUserExample example = new AdminUserExample();
        AdminUserExample.Criteria criteria = example.createCriteria();

        if (combinedIds != null) {
            criteria.andIdIn(combinedIds);
        }
        if (condition != null) {
            if (condition.getStatus() != null && !condition.getStatus().isEmpty()) {
                criteria.andStatusEqualTo(condition.getStatus());
            }
            if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
                // keyword OR 搜尋：先加 email 條件於主 criteria，再 or displayName
                criteria.andEmailLike("%" + condition.getKeyword() + "%");
                AdminUserExample.Criteria orCriteria = example.or();
                if (combinedIds != null) {
                    orCriteria.andIdIn(combinedIds);
                }
                if (condition.getStatus() != null && !condition.getStatus().isEmpty()) {
                    orCriteria.andStatusEqualTo(condition.getStatus());
                }
                orCriteria.andDisplayNameLike("%" + condition.getKeyword() + "%");
            }
        }

        example.setOrderByClause("created_at DESC");
        List<AdminUser> users = adminUserMapper.selectByExample(example);
        return users.stream().map(this::toAdminUserRes).collect(Collectors.toList());
    }

    @Override
    public List<AdminUserRes> listAdminUsers(String storeId) {
        if (storeId != null && !storeId.isEmpty()) {
            return getAdminUsersByStore(storeId);
        }
        return getAllAdminUsers();
    }

    @Override
    @Transactional
    public void disableAdminUser(String userId, String operatorId) {
        log.info("🚫 停用帳號：userId={}, operatorId={}", userId, operatorId);

        AdminUser user = adminUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "使用者不存在");
        }

        user.setStatus(AdminUserStatus.INACTIVE.getCode());
        user.setUpdatedBy(operatorId);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKeySelective(user);

        log.info("✅ 帳號已停用：userId={}", userId);
    }

    /**
     * 綁定角色 (使用 Example)
     */
    private void bindRole(String adminUserId, RoleCode roleCode) {
        // 查找角色 (使用 Example)
        RoleExample roleExample = new RoleExample();
        roleExample.createCriteria().andCodeEqualTo(roleCode.getCode());
        List<Role> roles = roleMapper.selectByExample(roleExample);
        if (roles.isEmpty()) {
            throw new BusinessException(ErrorCodes.COMMON_INTERNAL_ERROR,
                    "角色不存在：" + roleCode.getCode());
        }
        Role targetRole = roles.get(0);

        // 建立關聯
        AdminUserRole userRole = new AdminUserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setAdminUserId(adminUserId);
        userRole.setRoleId(targetRole.getId());
        userRole.setCreatedAt(LocalDateTime.now());
        adminUserRoleMapper.insert(userRole);
    }

    /**
     * 取得使用者的角色列表 (使用 Example)
     */
    private List<AdminUserRes.RoleInfo> getUserRoleInfos(String userId) {
        AdminUserRoleExample example = new AdminUserRoleExample();
        example.createCriteria().andAdminUserIdEqualTo(userId);
        List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(example);
        List<AdminUserRes.RoleInfo> result = new ArrayList<>();

        for (AdminUserRole ur : userRoles) {
            Role role = roleMapper.selectByPrimaryKey(ur.getRoleId());
            if (role != null) {
                AdminUserRes.RoleInfo roleInfo = new AdminUserRes.RoleInfo();
                roleInfo.setId(role.getId());
                roleInfo.setCode(role.getCode());
                roleInfo.setName(role.getName());
                result.add(roleInfo);
            }
        }

        return result;
    }

    /**
     * 取得使用者關聯的店家列表 (使用 Example)
     */
    private List<AdminUserRes.StoreInfo> getUserStoreInfos(String userId) {
        StoreUserExample example = new StoreUserExample();
        example.createCriteria().andAdminUserIdEqualTo(userId);
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
        List<AdminUserRes.StoreInfo> result = new ArrayList<>();

        for (StoreUser su : storeUsers) {
            Store store = storeMapper.selectByPrimaryKey(su.getStoreId());
            if (store != null) {
                AdminUserRes.StoreInfo storeInfo = new AdminUserRes.StoreInfo();
                storeInfo.setId(store.getId());
                storeInfo.setStoreName(store.getStoreName());
                storeInfo.setRoleType(su.getRoleType());
                result.add(storeInfo);
            }
        }

        return result;
    }

    /**
     * 轉換為回應 DTO
     */
    private AdminUserRes toAdminUserRes(AdminUser user) {
        AdminUserRes res = new AdminUserRes();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setDisplayName(user.getDisplayName());
        res.setPhone(user.getPhone());
        res.setStatus(user.getStatus());
        res.setForceChangePassword(user.getForceChangePassword());
        res.setLastLoginAt(user.getLastLoginAt());
        res.setCreatedAt(user.getCreatedAt());
        res.setRoles(getUserRoleInfos(user.getId()));
        res.setStores(getUserStoreInfos(user.getId()));
        return res;
    }

    @Override
    public List<com.group.admin.res.common.EnumOption> getAllUserOptions() {
        AdminUserExample example = new AdminUserExample();
        example.createCriteria().andStatusEqualTo("ACTIVE");
        example.setOrderByClause("display_name ASC");
        List<AdminUser> users = adminUserMapper.selectByExample(example);

        return users.stream()
                .map(user -> {
                    AdminUserRoleExample userRoleExample = new AdminUserRoleExample();
                    userRoleExample.createCriteria().andAdminUserIdEqualTo(user.getId());
                    List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(userRoleExample);

                    String roleCode = "未知";
                    if (!userRoles.isEmpty()) {
                        String roleId = userRoles.get(0).getRoleId();
                        Role role = roleMapper.selectByPrimaryKey(roleId);
                        if (role != null) {
                            roleCode = role.getCode();
                        }
                    }

                    String roleName;
                    switch (roleCode) {
                        case "ROLE_ADMIN": roleName = "系統管理員"; break;
                        case "ROLE_STORE_OWNER": roleName = "店家負責人"; break;
                        case "ROLE_STORE_EDITOR": roleName = "店家編輯"; break;
                        default: roleName = "未知";
                    }

                    return com.group.admin.res.common.EnumOption.builder()
                            .label(String.format("%s (%s)", user.getDisplayName(), user.getEmail()))
                            .value(user.getId())
                            .description(String.format("ID: %s | 角色: %s", user.getId(), roleName))
                            .build();
                })
                .collect(Collectors.toList());
    }
}
