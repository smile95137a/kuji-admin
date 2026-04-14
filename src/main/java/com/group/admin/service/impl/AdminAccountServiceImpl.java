package com.group.admin.service.impl;

import com.group.admin.dto.AdminAccountDetailDO;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.enums.AdminUserStatus;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.admin.AccountFilterCondition;
import com.group.admin.req.admin.CreateAdminAccountReq;
import com.group.admin.req.admin.UpdateAccountRoleReq;
import com.group.admin.req.admin.UpdateAccountStatusReq;
import com.group.admin.res.admin.AdminAccountRes;
import com.group.admin.service.AdminAccountService;
import com.group.admin.service.EmailService;
import com.group.admin.service.TokenBlacklistService;
import com.group.admin.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAccountServiceImpl implements AdminAccountService {

    private static final String ROLE_STORE_OWNER = "STORE_OWNER";
    private static final String ROLE_STORE_EDITOR = "STORE_EDITOR";

    private final AdminUserMapper adminUserMapper;
    private final StoreMapper storeMapper;
    private final StoreUserMapper storeUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil;
    private final EmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public AdminAccountRes createAccount(CreateAdminAccountReq req, String adminUserId) {
        log.info("建立店家帳號: email={}, roleType={}, storeId={}", req.getEmail(), req.getRoleType(), req.getStoreId());

        String roleType = req.getRoleType();
        if (!ROLE_STORE_OWNER.equals(roleType) && !ROLE_STORE_EDITOR.equals(roleType)) {
            throw new BusinessException("VALIDATION_ERROR", "roleType 必須是 STORE_OWNER 或 STORE_EDITOR");
        }

        AdminUserExample emailCheck = new AdminUserExample();
        emailCheck.createCriteria().andEmailEqualTo(req.getEmail());
        if (!adminUserMapper.selectByExample(emailCheck).isEmpty()) {
            throw new BusinessException("EMAIL_EXISTS", "Email already registered");
        }

        Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
        if (store == null) {
            throw new BusinessException("STORE_NOT_FOUND", "Store not found");
        }

        if (ROLE_STORE_OWNER.equals(roleType)) {
            if (store.getOwnerId() != null && !store.getOwnerId().isEmpty()) {
                throw new BusinessException("STORE_HAS_OWNER", "Store already has an owner");
            }
            StoreUserExample ownerCheck = new StoreUserExample();
            ownerCheck.createCriteria().andStoreIdEqualTo(req.getStoreId()).andRoleTypeEqualTo(ROLE_STORE_OWNER);
            if (!storeUserMapper.selectByExample(ownerCheck).isEmpty()) {
                throw new BusinessException("STORE_HAS_OWNER", "Store already has an owner");
            }
        }

        String initialPassword = passwordUtil.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(initialPassword);

        AdminUser adminUser = new AdminUser();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setUsername(req.getEmail());
        adminUser.setEmail(req.getEmail());
        adminUser.setDisplayName(req.getDisplayName());
        adminUser.setPhone(req.getPhone());
        adminUser.setPassword(encodedPassword);
        adminUser.setStatus(AdminUserStatus.PENDING.getCode());
        adminUser.setForceChangePassword(true);
        adminUser.setCreatedBy(adminUserId);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setRemark(req.getRemark());
        adminUserMapper.insertSelective(adminUser);

        StoreUser storeUser = new StoreUser();
        storeUser.setId(UUID.randomUUID().toString());
        storeUser.setStoreId(req.getStoreId());
        storeUser.setAdminUserId(adminUser.getId());
        storeUser.setRoleType(roleType);
        storeUser.setCreatedAt(LocalDateTime.now());
        storeUserMapper.insertSelective(storeUser);

        if (ROLE_STORE_OWNER.equals(roleType)) {
            store.setOwnerId(adminUser.getId());
            storeMapper.updateByPrimaryKeySelective(store);
        }

        emailService.sendInitialPasswordEmail(req.getEmail(), req.getDisplayName(), initialPassword);

        log.info("✅ 店家帳號建立成功: userId={}, roleType={}", adminUser.getId(), roleType);

        return AdminAccountRes.builder()
                .id(adminUser.getId())
                .email(adminUser.getEmail())
                .displayName(adminUser.getDisplayName())
                .phone(adminUser.getPhone())
                .status(adminUser.getStatus())
                .forceChangePassword(adminUser.getForceChangePassword())
                .roleType(roleType)
                .storeId(req.getStoreId())
                .storeName(store.getStoreName())
                .createdBy(adminUserId)
                .createdAt(adminUser.getCreatedAt())
                .build();
    }

    @Override
    public Map<String, Object> listAccounts(AccountFilterCondition filters, int page, int size) {
        int pageSize = Math.min(size, 100);
        int offset = page * pageSize;
        filters.setOffset(offset);
        filters.setPageSize(pageSize);

        if (filters.getSortBy() == null || filters.getSortBy().isEmpty()) {
            filters.setSortBy("au.created_at");
        } else {
            switch (filters.getSortBy()) {
                case "email" -> filters.setSortBy("au.email");
                case "displayName" -> filters.setSortBy("au.display_name");
                case "lastLoginAt" -> filters.setSortBy("au.last_login_at");
                default -> filters.setSortBy("au.created_at");
            }
        }
        if (filters.getSortDir() == null || filters.getSortDir().isEmpty()) {
            filters.setSortDir("DESC");
        } else {
            filters.setSortDir("ASC".equalsIgnoreCase(filters.getSortDir()) ? "ASC" : "DESC");
        }

        List<AdminAccountDetailDO> items = adminUserMapper.selectAccountsWithRole(filters);
        Long total = adminUserMapper.countAccountsWithRole(filters);

        List<AdminAccountRes> content = items.stream()
                .map(d -> AdminAccountRes.builder()
                        .id(d.getId())
                        .email(d.getEmail())
                        .displayName(d.getDisplayName())
                        .phone(d.getPhone())
                        .status(d.getStatus())
                        .forceChangePassword(d.getForceChangePassword())
                        .lastLoginAt(d.getLastLoginAt())
                        .roleType(d.getRoleType())
                        .storeId(d.getStoreId())
                        .storeName(d.getStoreName())
                        .createdBy(d.getCreatedBy())
                        .createdAt(d.getCreatedAt())
                        .updatedBy(d.getUpdatedBy())
                        .updatedAt(d.getUpdatedAt())
                        .remark(d.getRemark())
                        .build())
                .collect(Collectors.toList());

        int totalElements = total != null ? total.intValue() : 0;
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("page", page);
        result.put("size", pageSize);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        return result;
    }

    @Override
    @Transactional
    public AdminAccountRes updateStatus(String id, UpdateAccountStatusReq req, String adminUserId) {
        log.info("更新帳號狀態: id={}, status={}", id, req.getStatus());

        String status = req.getStatus();
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new BusinessException("INVALID_STATUS", "Invalid status value");
        }

        AdminUser user = adminUserMapper.selectByPrimaryKey(id);
        if (user == null) {
            throw new BusinessException("NOT_FOUND", "Account not found");
        }

        if (id.equals(adminUserId) && "INACTIVE".equals(status)) {
            throw new BusinessException("SELF_DISABLE", "Cannot disable your own account");
        }

        user.setStatus(status);
        user.setUpdatedBy(adminUserId);
        user.setUpdatedAt(LocalDateTime.now());
        if (req.getRemark() != null) {
            user.setRemark(req.getRemark());
        }
        adminUserMapper.updateByPrimaryKeySelective(user);

        if ("INACTIVE".equals(status)) {
            tokenBlacklistService.invalidateUser(id);
            log.info("🚫 帳號已停用，Token 立即失效: userId={}", id);
        }

        return AdminAccountRes.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .updatedBy(user.getUpdatedBy())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminAccountRes updateRole(String id, UpdateAccountRoleReq req, String adminUserId) {
        log.info("更新帳號角色: id={}, roleType={}, storeId={}", id, req.getRoleType(), req.getStoreId());

        String roleType = req.getRoleType();
        if (!ROLE_STORE_OWNER.equals(roleType) && !ROLE_STORE_EDITOR.equals(roleType)) {
            throw new BusinessException("VALIDATION_ERROR", "roleType 必須是 STORE_OWNER 或 STORE_EDITOR");
        }

        AdminUser user = adminUserMapper.selectByPrimaryKey(id);
        if (user == null) {
            throw new BusinessException("NOT_FOUND", "Account not found");
        }

        Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
        if (store == null) {
            throw new BusinessException("STORE_NOT_FOUND", "Store not found");
        }

        StoreUserExample oldBindings = new StoreUserExample();
        oldBindings.createCriteria().andAdminUserIdEqualTo(id);
        List<StoreUser> existingBindings = storeUserMapper.selectByExample(oldBindings);
        for (StoreUser su : existingBindings) {
            if (ROLE_STORE_OWNER.equals(su.getRoleType())) {
                Store oldStore = storeMapper.selectByPrimaryKey(su.getStoreId());
                if (oldStore != null && id.equals(oldStore.getOwnerId())) {
                    oldStore.setOwnerId(null);
                    storeMapper.updateByPrimaryKeySelective(oldStore);
                }
            }
        }
        storeUserMapper.deleteByExample(oldBindings);

        if (ROLE_STORE_OWNER.equals(roleType)) {
            if (store.getOwnerId() != null && !store.getOwnerId().isEmpty()) {
                throw new BusinessException("STORE_HAS_OWNER", "Store already has an owner");
            }
            store.setOwnerId(id);
            storeMapper.updateByPrimaryKeySelective(store);
        }

        StoreUser newBinding = new StoreUser();
        newBinding.setId(UUID.randomUUID().toString());
        newBinding.setStoreId(req.getStoreId());
        newBinding.setAdminUserId(id);
        newBinding.setRoleType(roleType);
        newBinding.setCreatedAt(LocalDateTime.now());
        storeUserMapper.insertSelective(newBinding);

        user.setUpdatedBy(adminUserId);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateByPrimaryKeySelective(user);

        return AdminAccountRes.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .roleType(roleType)
                .storeId(req.getStoreId())
                .storeName(store.getStoreName())
                .updatedBy(adminUserId)
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
