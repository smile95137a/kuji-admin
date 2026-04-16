package com.group.admin.service.impl;

import com.group.admin.condition.StoreCondition;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Lottery;
import com.group.admin.entity.Role;
import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.enums.AdminUserStatus;
import com.group.admin.enums.RoleCode;
import com.group.admin.enums.StoreUserRoleType;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.LotteryExample;
import com.group.admin.example.RoleExample;
import com.group.admin.example.StoreExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.example.BannerExample;
import com.group.admin.entity.Banner;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.BannerMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.store.CreateStoreReq;
import com.group.admin.req.store.UpdateStoreReq;
import com.group.admin.req.store.UpdateStoreStatusReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.lottery.LotteryListItemRes;
import com.group.admin.res.store.StoreDetailRes;
import com.group.admin.res.store.StoreListItemRes;
import com.group.admin.res.store.StoreRes;
import com.group.admin.service.StoreService;
import com.group.admin.util.PasswordUtil;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final LotteryMapper lotteryMapper;
    private final BannerMapper bannerMapper;
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

        String callerId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();

        if (!isAdmin) {
            StoreUserExample example = new StoreUserExample();
            example.createCriteria()
                    .andAdminUserIdEqualTo(callerId)
                    .andStoreIdEqualTo(storeId);

            if (storeUserMapper.countByExample(example) == 0) {
                throw new AccessDeniedException("無權限編輯此店家");
            }
            // STORE_OWNER cannot update remark
            req.setRemark(null);
        }

        if (req.getStoreName() != null) store.setStoreName(req.getStoreName());
        if (req.getShortDescription() != null) store.setShortDescription(req.getShortDescription());
        if (req.getLongDescription() != null) store.setLongDescription(req.getLongDescription());
        if (req.getLogoUrl() != null) store.setLogoUrl(req.getLogoUrl());
        if (req.getCoverImageUrl() != null) store.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getEmail() != null) store.setEmail(req.getEmail());
        if (req.getPhone() != null) store.setPhone(req.getPhone());
        if (req.getAddress() != null) store.setAddress(req.getAddress());
        if (req.getBusinessHours() != null) store.setBusinessHours(req.getBusinessHours());
        if (req.getFacebookUrl() != null) store.setFacebookUrl(req.getFacebookUrl());
        if (req.getInstagramUrl() != null) store.setInstagramUrl(req.getInstagramUrl());
        if (req.getLineId() != null) store.setLineId(req.getLineId());
        if (req.getRemark() != null) store.setRemark(req.getRemark());
        store.setUpdatedBy(callerId);
        store.setUpdatedAt(LocalDateTime.now());

        storeMapper.updateByPrimaryKeyWithBLOBs(store);

        log.info("✅ 店家資訊更新成功：storeId={}", storeId);

        return toStoreRes(store);
    }

    @Override
    @Transactional
    public void activateStore(String storeId) {
        updateStoreStatusInternal(storeId, "ACTIVE");
    }

    @Override
    @Transactional
    public void deactivateStore(String storeId) {
        updateStoreStatusInternal(storeId, "INACTIVE");
    }

    // ========== 014-store-management new implementations ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreRes createStore(CreateStoreReq req, String operatorId) {
        log.info("🏪 建立店家: storeName={}, operatorId={}", req.getStoreName(), operatorId);

        // Step 1: Create AdminUser (owner)
        AdminUser owner = null;
        if (req.getOwner() != null) {
            CreateStoreReq.OwnerAccountReq ownerReq = req.getOwner();

            String rawPassword = (ownerReq.getPassword() != null && !ownerReq.getPassword().isEmpty())
                    ? ownerReq.getPassword()
                    : passwordUtil.generateRandomPassword();

            owner = new AdminUser();
            owner.setId(UUID.randomUUID().toString());
            owner.setUsername(ownerReq.getUsername());
            owner.setEmail(ownerReq.getEmail() != null ? ownerReq.getEmail() : ownerReq.getUsername());
            owner.setPassword(passwordEncoder.encode(rawPassword));
            owner.setDisplayName(ownerReq.getDisplayName());
            owner.setPhone(ownerReq.getPhone());
            owner.setStatus(AdminUserStatus.ACTIVE.getCode());
            owner.setForceChangePassword(true);
            owner.setCreatedBy(operatorId);
            owner.setCreatedAt(LocalDateTime.now());

            try {
                adminUserMapper.insertSelective(owner);
            } catch (DataIntegrityViolationException e) {
                throw new BusinessException("USERNAME_CONFLICT", "帳號名稱已存在: " + ownerReq.getUsername());
            }

            // Assign ROLE_STORE_OWNER
            RoleExample roleExample = new RoleExample();
            roleExample.createCriteria().andCodeEqualTo(RoleCode.ROLE_STORE_OWNER.getCode());
            List<Role> roles = roleMapper.selectByExample(roleExample);
            if (!roles.isEmpty()) {
                AdminUserRole userRole = new AdminUserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setAdminUserId(owner.getId());
                userRole.setRoleId(roles.get(0).getId());
                userRole.setCreatedAt(LocalDateTime.now());
                adminUserRoleMapper.insertSelective(userRole);
            }
        }

        // Step 2: Create Store
        Store store = new Store();
        store.setId(UUID.randomUUID().toString());
        store.setOwnerId(owner != null ? owner.getId() : null);
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
        store.setStatus("ACTIVE");
        store.setCreatedBy(operatorId);
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());

        try {
            storeMapper.insertSelective(store);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("CONFLICT", "店家建立失敗");
        }

        // Step 3: Link store_user
        if (owner != null) {
            StoreUser storeUser = new StoreUser();
            storeUser.setId(UUID.randomUUID().toString());
            storeUser.setStoreId(store.getId());
            storeUser.setAdminUserId(owner.getId());
            storeUser.setRoleType(StoreUserRoleType.OWNER.getCode());
            storeUser.setCreatedAt(LocalDateTime.now());
            storeUserMapper.insertSelective(storeUser);
        }

        log.info("✅ 店家及負責人帳號建立成功: storeId={}, ownerId={}", store.getId(),
                owner != null ? owner.getId() : "N/A");

        return toStoreResWithOwner(store, owner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateStoreStatus(String storeId, UpdateStoreStatusReq req,
            boolean force, String operatorId) {
        log.info("🔄 更新店家狀態: storeId={}, status={}, force={}", storeId, req.getStatus(), force);

        Store store = storeMapper.selectByPrimaryKey(storeId);
        if (store == null) {
            throw new BusinessException("店家不存在");
        }

        Map<String, Object> result = new HashMap<>();

        if ("INACTIVE".equals(req.getStatus())) {
            // Check active lotteries before disabling
            if (!force) {
                LotteryExample countExample = new LotteryExample();
                countExample.createCriteria()
                        .andStoreIdEqualTo(storeId)
                        .andStatusEqualTo("ON_SHELF");
                long activeLotteryCount = lotteryMapper.countByExample(countExample);

                if (activeLotteryCount > 0) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("activeLotteryCount", activeLotteryCount);
                    throw new BusinessException("ACTIVE_LOTTERIES",
                            "店家有 " + activeLotteryCount + " 個上架中商品，請先下架或使用 force=true 強制停用");
                }
            }

            // Cascade: set lotteries OFF_SHELF (only non-off-shelf, non-draft)
            LotteryExample lotteryFilter = new LotteryExample();
            lotteryFilter.createCriteria()
                    .andStoreIdEqualTo(storeId)
                    .andStatusEqualTo("ON_SHELF");
            Lottery lotterySetter = new Lottery();
            lotterySetter.setStatus("OFF_SHELF");
            lotterySetter.setUpdatedAt(LocalDateTime.now());
            int productsOffShelf = lotteryMapper.updateByExampleSelective(lotterySetter, lotteryFilter);

            // Cascade: set banners INACTIVE
            BannerExample bannerFilter = new BannerExample();
            bannerFilter.createCriteria()
                    .andStoreIdEqualTo(storeId)
                    .andStatusEqualTo("ACTIVE");
            Banner bannerSetter = new Banner();
            bannerSetter.setStatus("INACTIVE");
            bannerSetter.setUpdatedAt(LocalDateTime.now());
            int bannersDisabled = bannerMapper.updateByExampleSelective(bannerSetter, bannerFilter);

            // Deactivate store
            store.setStatus("INACTIVE");
            store.setUpdatedBy(operatorId);
            store.setUpdatedAt(LocalDateTime.now());
            storeMapper.updateByPrimaryKeySelective(store);

            Map<String, Object> cascadeResult = new HashMap<>();
            cascadeResult.put("productsOffShelf", productsOffShelf);
            cascadeResult.put("bannersDisabled", bannersDisabled);

            result.put("id", storeId);
            result.put("status", "INACTIVE");
            result.put("updatedAt", store.getUpdatedAt());
            result.put("cascadeResult", cascadeResult);

            log.info("✅ 店家已停用: storeId={}, productsOffShelf={}, bannersDisabled={}",
                    storeId, productsOffShelf, bannersDisabled);

        } else {
            // ACTIVE: only update store status, do NOT restore products or banners (FR-005)
            store.setStatus("ACTIVE");
            store.setUpdatedBy(operatorId);
            store.setUpdatedAt(LocalDateTime.now());
            storeMapper.updateByPrimaryKeySelective(store);

            result.put("id", storeId);
            result.put("status", "ACTIVE");
            result.put("updatedAt", store.getUpdatedAt());
            result.put("cascadeResult", null);
            result.put("note", "商品與橫幅狀態未自動恢復，需手動重新啟用");

            log.info("✅ 店家已啟用: storeId={}", storeId);
        }

        return result;
    }

    @Override
    public PageResult<StoreListItemRes> listEnabledStores(int page, int size) {
        log.info("📋 查詢公開店家列表: page={}, size={}", page, size);

        int offset = (page - 1) * size;
        List<Store> stores = storeMapper.selectEnabledStores(offset, size);
        long total = storeMapper.countEnabledStores();

        List<StoreListItemRes> items = stores.stream()
                .map(s -> StoreListItemRes.builder()
                        .id(s.getId())
                        .storeName(s.getStoreName())
                        .shortDescription(s.getShortDescription())
                        .logoUrl(s.getLogoUrl())
                        .build())
                .collect(Collectors.toList());

        return PageResult.of(page, size, total, items);
    }

    @Override
    public StoreDetailRes getPublicStoreDetail(String storeId) {
        Store store = storeMapper.selectByPrimaryKey(storeId);

        if (store == null || !"ACTIVE".equals(store.getStatus())) {
            throw new BusinessException("NOT_FOUND", "店家不存在");
        }

        // Query ON_SHELF lotteries for this store
        LotteryExample lotteryExample = new LotteryExample();
        lotteryExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusEqualTo("ON_SHELF");
        lotteryExample.setOrderByClause("created_at DESC");
        List<Lottery> lotteries = lotteryMapper.selectByExample(lotteryExample);

        List<LotteryListItemRes> products = lotteries.stream()
                .map(l -> LotteryListItemRes.builder()
                        .id(l.getId())
                        .storeId(l.getStoreId())
                        .title(l.getTitle())
                        .imageUrl(l.getImageUrl())
                        .category(l.getCategory())
                        .pricePerDraw(l.getPricePerDraw())
                        .maxDraws(l.getMaxDraws())
                        .status(l.getStatus())
                        .build())
                .collect(Collectors.toList());

        return StoreDetailRes.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .shortDescription(store.getShortDescription())
                .longDescription(store.getLongDescription())
                .logoUrl(store.getLogoUrl())
                .coverImageUrl(store.getCoverImageUrl())
                .email(store.getEmail())
                .phone(store.getPhone())
                .address(store.getAddress())
                .businessHours(store.getBusinessHours())
                .facebookUrl(store.getFacebookUrl())
                .instagramUrl(store.getInstagramUrl())
                .lineId(store.getLineId())
                .products(products)
                .build();
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
        StoreRes res = StoreRes.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .shortDescription(store.getShortDescription())
                .longDescription(store.getLongDescription())
                .logoUrl(store.getLogoUrl())
                .coverImageUrl(store.getCoverImageUrl())
                .email(store.getEmail())
                .phone(store.getPhone())
                .address(store.getAddress())
                .businessHours(store.getBusinessHours())
                .facebookUrl(store.getFacebookUrl())
                .instagramUrl(store.getInstagramUrl())
                .lineId(store.getLineId())
                .status(store.getStatus())
                .remark(store.getRemark())
                .createdAt(store.getCreatedAt())
                .createdBy(store.getCreatedBy())
                .updatedAt(store.getUpdatedAt())
                .build();

        // Owner info
        if (store.getOwnerId() != null) {
            AdminUser owner = adminUserMapper.selectByPrimaryKey(store.getOwnerId());
            if (owner != null) {
                res.setOwnerId(owner.getId());
                res.setOwnerUsername(owner.getUsername());
                res.setOwnerDisplayName(owner.getDisplayName());
                res.setOwner(StoreRes.OwnerInfo.builder()
                        .id(owner.getId())
                        .displayName(owner.getDisplayName())
                        .email(owner.getEmail())
                        .build());
            }
        } else {
            // Fallback: query via store_user
            StoreUserExample storeUserExample = new StoreUserExample();
            storeUserExample.createCriteria()
                    .andStoreIdEqualTo(store.getId())
                    .andRoleTypeEqualTo(StoreUserRoleType.OWNER.getCode());
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);

            if (!storeUsers.isEmpty()) {
                AdminUser owner = adminUserMapper.selectByPrimaryKey(storeUsers.get(0).getAdminUserId());
                if (owner != null) {
                    res.setOwnerId(owner.getId());
                    res.setOwnerUsername(owner.getUsername());
                    res.setOwnerDisplayName(owner.getDisplayName());
                    res.setOwner(StoreRes.OwnerInfo.builder()
                            .id(owner.getId())
                            .displayName(owner.getDisplayName())
                            .email(owner.getEmail())
                            .build());
                }
            }
        }

        return res;
    }

    private StoreRes toStoreResWithOwner(Store store, AdminUser owner) {
        StoreRes res = toStoreRes(store);
        if (owner != null) {
            res.setOwnerId(owner.getId());
            res.setOwnerUsername(owner.getUsername());
            res.setOwnerDisplayName(owner.getDisplayName());
            res.setOwner(StoreRes.OwnerInfo.builder()
                    .id(owner.getId())
                    .displayName(owner.getDisplayName())
                    .email(owner.getEmail())
                    .build());
        }
        return res;
    }

    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    // ========== 店家選項相關 ==========

    @Override
    public List<com.group.admin.res.common.EnumOption> getStoreOptionsForUser(String userId, boolean isAdmin, Boolean activeOnly) {
        StoreExample example = new StoreExample();
        StoreExample.Criteria criteria = example.createCriteria();
        if (Boolean.TRUE.equals(activeOnly)) {
            criteria.andStatusEqualTo("ACTIVE");
        }
        if (!isAdmin) {
            StoreUserExample sue = new StoreUserExample();
            sue.createCriteria().andAdminUserIdEqualTo(userId);
            List<StoreUser> su = storeUserMapper.selectByExample(sue);
            if (su.isEmpty()) return java.util.Collections.emptyList();
            List<String> ids = su.stream().map(StoreUser::getStoreId).collect(Collectors.toList());
            criteria.andIdIn(ids);
        }
        example.setOrderByClause("store_name ASC");
        return storeMapper.selectByExample(example).stream()
                .map(s -> com.group.admin.res.common.EnumOption.builder()
                        .label(s.getStoreName())
                        .value(s.getId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<com.group.admin.res.common.EnumOption> searchStoreOptions(String userId, boolean isAdmin, List<String> storeIds, String keyword, Boolean activeOnly) {
        StoreExample example = new StoreExample();
        StoreExample.Criteria criteria = example.createCriteria();
        if (Boolean.TRUE.equals(activeOnly)) {
            criteria.andStatusEqualTo("ACTIVE");
        }
        if (keyword != null && !keyword.isBlank()) {
            criteria.andStoreNameLike("%" + keyword + "%");
        }
        if (!isAdmin && storeIds != null && !storeIds.isEmpty()) {
            criteria.andIdIn(storeIds);
        }
        example.setOrderByClause("store_name ASC");
        return storeMapper.selectByExample(example).stream()
                .map(s -> com.group.admin.res.common.EnumOption.builder()
                        .label(s.getStoreName())
                        .value(s.getId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<com.group.admin.res.common.EnumOption> getAllActiveStoreOptions() {
        StoreExample example = new StoreExample();
        example.createCriteria().andStatusEqualTo("ACTIVE");
        example.setOrderByClause("store_name ASC");
        return storeMapper.selectByExample(example).stream()
                .map(s -> com.group.admin.res.common.EnumOption.builder()
                        .label(s.getStoreName())
                        .value(s.getId())
                        .build())
                .collect(Collectors.toList());
    }
}

