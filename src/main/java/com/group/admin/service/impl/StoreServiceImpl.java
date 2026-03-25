package com.group.admin.service.impl;

import com.group.admin.condition.StoreCondition;
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
import com.group.admin.example.RoleExample;
import com.group.admin.example.StoreExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.store.CreateStoreReq;
import com.group.admin.req.store.UpdateStoreReq;
import com.group.admin.req.store.UpdateStoreStatusReq;
import com.group.admin.res.store.StoreRes;
import com.group.admin.service.StoreService;
import com.group.admin.util.PasswordUtil;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;
    private final StoreUserMapper storeUserMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil;

    @Override
    public List<StoreRes> queryStores(QueryReq<StoreCondition> req) {
        StoreCondition condition = req != null ? req.getCondition() : null;
        
        String userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        
        log.info("🔍 查詢店家列表，userId：{}，isAdmin：{}", userId, isAdmin);
        
        StoreExample example = new StoreExample();
        StoreExample.Criteria criteria = example.createCriteria();
        
        if (!isAdmin) {
            StoreUserExample storeUserExample = new StoreUserExample();
            storeUserExample.createCriteria().andAdminUserIdEqualTo(userId);
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);
            
            if (storeUsers.isEmpty()) {
                log.warn("⚠️ 使用者沒有關聯的店家：userId={}", userId);
                return List.of();
            }
            
            List<String> storeIds = storeUsers.stream()
                    .map(StoreUser::getStoreId)
                    .collect(Collectors.toList());
            
            criteria.andIdIn(storeIds);
        }
        
        if (condition != null) {
            if (condition.getStoreName() != null && !condition.getStoreName().isEmpty()) {
                criteria.andStoreNameLike("%" + condition.getStoreName() + "%");
            }
            if (condition.getStatus() != null && !condition.getStatus().isEmpty()) {
                criteria.andStatusEqualTo(condition.getStatus());
            }
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(condition.getCreatedAtStart().atStartOfDay());
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(condition.getCreatedAtEnd().plusDays(1).atStartOfDay());
            }
        }
        
        if (req != null && req.getSortBy() != null) {
            String sortOrder = req.getSortOrder() != null ? req.getSortOrder() : "ASC";
            example.setOrderByClause(toSnakeCase(req.getSortBy()) + " " + sortOrder);
        } else {
            example.setOrderByClause("created_at DESC");
        }
        
        List<Store> stores = storeMapper.selectByExample(example);
        
        log.info("✅ 查詢到 {} 個店家", stores.size());
        
        return stores.stream()
                .map(this::toStoreRes)
                .collect(Collectors.toList());
    }

    @Override
    public StoreRes getStoreById(String storeId) {
        Store store = storeMapper.selectByPrimaryKey(storeId);
        
        if (store == null) {
            throw new BusinessException("店家不存在");
        }
        
        if (!SecurityUtils.isAdmin()) {
            String userId = SecurityUtils.getCurrentUserId();
            StoreUserExample example = new StoreUserExample();
            example.createCriteria()
                    .andAdminUserIdEqualTo(userId)
                    .andStoreIdEqualTo(storeId);
            
            if (storeUserMapper.countByExample(example) == 0) {
                throw new BusinessException("無權限查看此店家");
            }
        }
        
        return toStoreRes(store);
    }

    @Override
    @Transactional
    public StoreRes updateStore(String storeId, UpdateStoreReq req) {
        Store store = storeMapper.selectByPrimaryKey(storeId);
        
        if (store == null) {
            throw new BusinessException("店家不存在");
        }
        
        if (!SecurityUtils.isAdmin()) {
            String userId = SecurityUtils.getCurrentUserId();
            StoreUserExample example = new StoreUserExample();
            example.createCriteria()
                    .andAdminUserIdEqualTo(userId)
                    .andStoreIdEqualTo(storeId);
            
            if (storeUserMapper.countByExample(example) == 0) {
                throw new BusinessException("無權限更新此店家");
            }
        }
        
        store.setStoreName(req.getStoreName());
        store.setShortDescription(req.getShortDescription());
        store.setLongDescription(req.getLongDescription());
        store.setLogoUrl(req.getLogoUrl());
        store.setCoverImageUrl(req.getCoverImageUrl());
        store.setEmail(req.getEmail());
        store.setPhone(req.getPhone());
        store.setAddress(req.getAddress());
        store.setBusinessHours(req.getBusinessHours());
        store.setFacebookUrl(req.getFacebookUrl());
        store.setInstagramUrl(req.getInstagramUrl());
        store.setLineId(req.getLineId());
        store.setRemark(req.getRemark());
        store.setUpdatedAt(LocalDateTime.now());
        
        storeMapper.updateByPrimaryKeyWithBLOBs(store);
        
        log.info("✅ 店家資訊更新成功：storeId={}", storeId);
        
        return toStoreRes(store);
    }

    @Override
    @Transactional
    public void activateStore(String storeId) {
        updateStoreStatusInternal(storeId, StoreStatus.ACTIVE.getCode());
    }

    @Override
    @Transactional
    public void deactivateStore(String storeId) {
        updateStoreStatusInternal(storeId, StoreStatus.INACTIVE.getCode());
    }

    // ========== 014-store-management new implementations ==========

    @Override
    @Transactional
    public StoreRes createStore(CreateStoreReq req, String operatorId) {
        log.info("🏪 建立店家: storeName={}, operatorId={}", req.getStoreName(), operatorId);

        // Create store
        Store store = new Store();
        store.setId(UUID.randomUUID().toString());
        store.setStoreName(req.getStoreName());
        store.setShortDescription(req.getShortDescription());
        store.setLongDescription(req.getLongDescription());
        store.setLogoUrl(req.getLogoUrl());
        store.setCoverImageUrl(req.getCoverImageUrl());
        store.setEmail(req.getEmail());
        store.setPhone(req.getPhone());
        store.setAddress(req.getAddress());
        store.setBusinessHours(req.getBusinessHours());
        store.setFacebookUrl(req.getFacebookUrl());
        store.setInstagramUrl(req.getInstagramUrl());
        store.setLineId(req.getLineId());
        store.setRemark(req.getRemark());
        store.setStatus(StoreStatus.ACTIVE.getCode());
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());

        // Create owner account if provided
        if (req.getOwner() != null) {
            CreateStoreReq.OwnerAccountReq ownerReq = req.getOwner();

            // Check duplicate username/email
            AdminUserExample emailCheck = new AdminUserExample();
            emailCheck.createCriteria().andUsernameEqualTo(ownerReq.getUsername());
            if (!adminUserMapper.selectByExample(emailCheck).isEmpty()) {
                throw new BusinessException("CONFLICT", "帳號已存在: " + ownerReq.getUsername());
            }

            String password = ownerReq.getPassword() != null && !ownerReq.getPassword().isEmpty()
                    ? ownerReq.getPassword()
                    : passwordUtil.generateRandomPassword();

            AdminUser adminUser = new AdminUser();
            adminUser.setId(UUID.randomUUID().toString());
            adminUser.setUsername(ownerReq.getUsername());
            adminUser.setEmail(ownerReq.getEmail() != null ? ownerReq.getEmail() : ownerReq.getUsername());
            adminUser.setPassword(passwordEncoder.encode(password));
            adminUser.setDisplayName(ownerReq.getDisplayName());
            adminUser.setPhone(ownerReq.getPhone());
            adminUser.setStatus(AdminUserStatus.PENDING.getCode());
            adminUser.setForceChangePassword(true);
            adminUser.setCreatedBy(operatorId);
            adminUser.setCreatedAt(LocalDateTime.now());

            try {
                adminUserMapper.insertSelective(adminUser);
            } catch (DataIntegrityViolationException e) {
                throw new BusinessException("CONFLICT", "帳號已存在: " + ownerReq.getUsername());
            }

            // Assign ROLE_STORE_OWNER
            RoleExample roleExample = new RoleExample();
            roleExample.createCriteria().andCodeEqualTo(RoleCode.ROLE_STORE_OWNER.getCode());
            List<Role> roles = roleMapper.selectByExample(roleExample);
            if (!roles.isEmpty()) {
                AdminUserRole userRole = new AdminUserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setAdminUserId(adminUser.getId());
                userRole.setRoleId(roles.get(0).getId());
                userRole.setCreatedAt(LocalDateTime.now());
                adminUserRoleMapper.insertSelective(userRole);
            }

            store.setOwnerId(adminUser.getId());
            storeMapper.insertSelective(store);

            // Link store_user
            StoreUser storeUser = new StoreUser();
            storeUser.setId(UUID.randomUUID().toString());
            storeUser.setStoreId(store.getId());
            storeUser.setAdminUserId(adminUser.getId());
            storeUser.setRoleType(StoreUserRoleType.OWNER.getCode());
            storeUser.setCreatedAt(LocalDateTime.now());
            storeUserMapper.insertSelective(storeUser);

            log.info("✅ 店家及負責人帳號建立成功: storeId={}, ownerId={}", store.getId(), adminUser.getId());
        } else {
            storeMapper.insertSelective(store);
            log.info("✅ 店家建立成功（無負責人）: storeId={}", store.getId());
        }

        return toStoreRes(store);
    }

    @Override
    @Transactional
    public void updateStoreStatus(String storeId, UpdateStoreStatusReq req, String operatorId) {
        log.info("🔄 更新店家狀態: storeId={}, status={}, operatorId={}", storeId, req.getStatus(), operatorId);

        Store store = storeMapper.selectByPrimaryKey(storeId);
        if (store == null) {
            throw new BusinessException("店家不存在");
        }

        store.setStatus(req.getStatus());
        store.setUpdatedBy(operatorId);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateByPrimaryKeySelective(store);

        log.info("✅ 店家狀態更新成功: storeId={}, status={}", storeId, req.getStatus());
    }

    @Override
    public List<StoreRes> getPublicStoreList(int page, int size) {
        log.info("📋 查詢公開店家列表: page={}, size={}", page, size);

        StoreExample example = new StoreExample();
        example.createCriteria().andStatusEqualTo(StoreStatus.ACTIVE.getCode());
        example.setOrderByClause("created_at DESC");

        List<Store> stores = storeMapper.selectByExample(example);

        // Simple offset-based sub-list for public API
        int fromIndex = Math.min(page * size, stores.size());
        int toIndex = Math.min(fromIndex + size, stores.size());
        List<Store> paged = stores.subList(fromIndex, toIndex);

        return paged.stream()
                .map(this::toPublicStoreRes)
                .collect(Collectors.toList());
    }

    // ========== Helper methods ==========

    private void updateStoreStatusInternal(String storeId, String status) {
        Store store = storeMapper.selectByPrimaryKey(storeId);
        
        if (store == null) {
            throw new BusinessException("店家不存在");
        }
        
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException("只有管理員可以啟用/停用店家");
        }
        
        store.setStatus(status);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateByPrimaryKeySelective(store);
        
        log.info("✅ 店家狀態更新：storeId={}，status={}", storeId, status);
    }

    private StoreRes toStoreRes(Store store) {
        StoreRes res = new StoreRes();
        BeanUtils.copyProperties(store, res);
        
        // Override id — StoreRes.id is String-type but BeanUtils may fail
        // if types differ. Let's set manually for safety.
        res.setId(null); // clear any bad copy
        res.setStoreName(store.getStoreName());
        res.setShortDescription(store.getShortDescription());
        res.setLongDescription(store.getLongDescription());
        res.setLogoUrl(store.getLogoUrl());
        res.setCoverImageUrl(store.getCoverImageUrl());
        res.setEmail(store.getEmail());
        res.setPhone(store.getPhone());
        res.setAddress(store.getAddress());
        res.setBusinessHours(store.getBusinessHours());
        res.setFacebookUrl(store.getFacebookUrl());
        res.setInstagramUrl(store.getInstagramUrl());
        res.setLineId(store.getLineId());
        res.setStatus(store.getStatus());
        res.setRemark(store.getRemark());
        res.setCreatedAt(store.getCreatedAt());
        res.setUpdatedAt(store.getUpdatedAt());
        
        res.setStatusDisplayName(StoreStatus.ACTIVE.getCode().equals(store.getStatus()) ? "啟用" : "停用");
        
        // Owner info
        StoreUserExample storeUserExample = new StoreUserExample();
        storeUserExample.createCriteria()
                .andStoreIdEqualTo(store.getId())
                .andRoleTypeEqualTo(StoreUserRoleType.OWNER.getCode());
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);
        
        if (!storeUsers.isEmpty()) {
            String ownerId = storeUsers.get(0).getAdminUserId();
            AdminUser owner = adminUserMapper.selectByPrimaryKey(ownerId);
            
            if (owner != null) {
                StoreRes.OwnerInfo ownerInfo = StoreRes.OwnerInfo.builder()
                        .id(null)
                        .email(owner.getEmail())
                        .displayName(owner.getDisplayName())
                        .build();
                res.setOwner(ownerInfo);
            }
        }
        
        return res;
    }

    private StoreRes toPublicStoreRes(Store store) {
        StoreRes res = new StoreRes();
        res.setStoreName(store.getStoreName());
        res.setShortDescription(store.getShortDescription());
        res.setLogoUrl(store.getLogoUrl());
        res.setCoverImageUrl(store.getCoverImageUrl());
        res.setEmail(store.getEmail());
        res.setPhone(store.getPhone());
        res.setAddress(store.getAddress());
        res.setBusinessHours(store.getBusinessHours());
        res.setFacebookUrl(store.getFacebookUrl());
        res.setInstagramUrl(store.getInstagramUrl());
        res.setLineId(store.getLineId());
        res.setStatus(store.getStatus());
        return res;
    }

    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
