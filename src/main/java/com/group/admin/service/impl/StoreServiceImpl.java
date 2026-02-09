package com.group.admin.service.impl;

import com.group.admin.condition.StoreCondition;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.StoreExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.store.UpdateStoreReq;
import com.group.admin.res.store.StoreRes;
import com.group.admin.service.StoreService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 店家服務實作
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;
    private final StoreUserMapper storeUserMapper;
    private final AdminUserMapper adminUserMapper;

    @Override
    public List<StoreRes> queryStores(QueryReq<StoreCondition> req) {
        StoreCondition condition = req != null ? req.getCondition() : null;
        
        String userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        
        log.info("🔍 查詢店家列表，userId：{}，isAdmin：{}", userId, isAdmin);
        
        StoreExample example = new StoreExample();
        StoreExample.Criteria criteria = example.createCriteria();
        
        // 權限過濾：非 Admin 只能看自己的店家
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
        
        // 條件過濾
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
        
        // 排序
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
            log.error("❌ 店家不存在：storeId={}", storeId);
            throw new BusinessException("店家不存在");
        }
        
        // 權限檢查：非 Admin 只能查看自己的店家
        if (!SecurityUtils.isAdmin()) {
            String userId = SecurityUtils.getCurrentUserId();
            StoreUserExample example = new StoreUserExample();
            example.createCriteria()
                    .andAdminUserIdEqualTo(userId)
                    .andStoreIdEqualTo(storeId);
            
            if (storeUserMapper.countByExample(example) == 0) {
                log.error("❌ 無權限查看此店家：userId={}，storeId={}", userId, storeId);
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
            log.error("❌ 店家不存在：storeId={}", storeId);
            throw new BusinessException("店家不存在");
        }
        
        // 權限檢查：非 Admin 只能更新自己的店家
        if (!SecurityUtils.isAdmin()) {
            String userId = SecurityUtils.getCurrentUserId();
            StoreUserExample example = new StoreUserExample();
            example.createCriteria()
                    .andAdminUserIdEqualTo(userId)
                    .andStoreIdEqualTo(storeId);
            
            if (storeUserMapper.countByExample(example) == 0) {
                log.error("❌ 無權限更新此店家：userId={}，storeId={}", userId, storeId);
                throw new BusinessException("無權限更新此店家");
            }
        }
        
        // 更新店家資訊
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
        
        storeMapper.updateByPrimaryKey(store);
        
        log.info("✅ 店家資訊更新成功：storeId={}", storeId);
        
        return toStoreRes(store);
    }

    @Override
    @Transactional
    public void activateStore(String storeId) {
        updateStoreStatus(storeId, "ACTIVE");
    }

    @Override
    @Transactional
    public void deactivateStore(String storeId) {
        updateStoreStatus(storeId, "INACTIVE");
    }

    /**
     * 更新店家狀態
     */
    private void updateStoreStatus(String storeId, String status) {
        Store store = storeMapper.selectByPrimaryKey(storeId);
        
        if (store == null) {
            log.error("❌ 店家不存在：storeId={}", storeId);
            throw new BusinessException("店家不存在");
        }
        
        // 權限檢查：只有 Admin 可以啟用/停用店家
        if (!SecurityUtils.isAdmin()) {
            log.error("❌ 只有管理員可以啟用/停用店家");
            throw new BusinessException("只有管理員可以啟用/停用店家");
        }
        
        store.setStatus(status);
        store.setUpdatedAt(LocalDateTime.now());
        
        storeMapper.updateByPrimaryKey(store);
        
        log.info("✅ 店家狀態更新：storeId={}，status={}", storeId, status);
    }

    /**
     * 轉換為 StoreRes
     */
    private StoreRes toStoreRes(Store store) {
        StoreRes res = new StoreRes();
        BeanUtils.copyProperties(store, res);
        
        // 設定狀態顯示名稱
        res.setStatusDisplayName("ACTIVE".equals(store.getStatus()) ? "啟用" : "停用");
        
        // 查詢店家負責人資訊
        StoreUserExample storeUserExample = new StoreUserExample();
        storeUserExample.createCriteria().andStoreIdEqualTo(store.getId());
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);
        
        if (!storeUsers.isEmpty()) {
            String ownerId = storeUsers.get(0).getAdminUserId();
            AdminUser owner = adminUserMapper.selectByPrimaryKey(ownerId);
            
            if (owner != null) {
                StoreRes.OwnerInfo ownerInfo = StoreRes.OwnerInfo.builder()
                        .id(Long.parseLong(owner.getId()))
                        .email(owner.getEmail())
                        .displayName(owner.getDisplayName())
                        .build();
                res.setOwner(ownerInfo);
            }
        }
        
        return res;
    }

    /**
     * 駝峰轉蛇形
     */
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
