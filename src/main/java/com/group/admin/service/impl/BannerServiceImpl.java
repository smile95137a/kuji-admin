package com.group.admin.service.impl;

import com.group.admin.entity.Banner;
import com.group.admin.entity.Store;
import com.group.admin.example.BannerExample;
import com.group.admin.mapper.BannerMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.req.banner.BannerCondition;
import com.group.admin.req.banner.BannerCreateReq;
import com.group.admin.req.banner.BannerUpdateReq;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.banner.BannerRes;
import com.group.admin.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Banner Service 實作
 * 
 * <p>提供 Banner 的 CRUD 操作與狀態管理</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;
    private final StoreMapper storeMapper;

    /**
     * 查詢 Banner 列表（後台用，支援動態條件）
     */
    @Override
    public List<BannerRes> queryBanners(QueryReq<BannerCondition> req) {
        log.info("🔍 查詢 Banner 列表，條件：{}", req);
        
        BannerCondition condition = req != null ? req.getCondition() : null;
        
        BannerExample example = new BannerExample();
        BannerExample.Criteria criteria = example.createCriteria();
        
        // 動態條件查詢（空字串視為 null）
        if (condition != null) {
            // 店家 ID 查詢
            if (isNotBlank(condition.getStoreId())) {
                criteria.andStoreIdEqualTo(condition.getStoreId());
            }
            
            // 標題模糊查詢
            if (isNotBlank(condition.getTitle())) {
                criteria.andTitleLike("%" + condition.getTitle() + "%");
            }
            
            // 狀態查詢
            if (isNotBlank(condition.getStatus())) {
                criteria.andStatusEqualTo(condition.getStatus());
            }
            
            // 日期範圍（LocalDate 轉 LocalDateTime）
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(
                    condition.getCreatedAtStart().atStartOfDay()
                );
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(
                    condition.getCreatedAtEnd().atTime(23, 59, 59)
                );
            }
            
            // 關鍵字搜尋
            if (isNotBlank(condition.getKeyword())) {
                criteria.andTitleLike("%" + condition.getKeyword() + "%");
            }
        }
        
        // 排序
        if (req != null && isNotBlank(req.getSortBy())) {
            String sortOrder = isNotBlank(req.getSortOrder()) ? req.getSortOrder() : "ASC";
            example.setOrderByClause(req.getSortBy() + " " + sortOrder);
        } else {
            // 預設按排序號升序
            example.setOrderByClause("order_num ASC, created_at DESC");
        }
        
        List<Banner> banners = bannerMapper.selectByExample(example);
        log.info("✅ 查詢到 {} 個 Banner", banners.size());
        
        return banners.stream()
                .map(this::convertToRes)
                .collect(Collectors.toList());
    }

    /**
     * 根據 ID 查詢 Banner 詳情
     */
    @Override
    public BannerRes getBannerById(String id) {
        log.info("🔍 查詢 Banner 詳情，ID：{}", id);
        
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            log.warn("❌ Banner 不存在，ID：{}", id);
            throw new RuntimeException("Banner 不存在");
        }
        
        log.info("✅ 查詢成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    /**
     * 新增 Banner
     */
    @Override
    @Transactional
    public BannerRes createBanner(BannerCreateReq req) {
        log.info("➕ 新增 Banner：{}", req.getTitle());
        
        // 檢查店家是否存在
        Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
        if (store == null) {
            log.warn("❌ 店家不存在，ID：{}", req.getStoreId());
            throw new RuntimeException("店家不存在");
        }
        
        Banner banner = new Banner();
        banner.setId(UUID.randomUUID().toString());
        banner.setStoreId(req.getStoreId());
        banner.setTitle(req.getTitle());
        banner.setImageUrl(req.getImageUrl());
        banner.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
        banner.setStatus(req.getStatus() != null ? req.getStatus() : "UNPUBLISHED");
        banner.setStartTime(req.getStartTime());
        banner.setEndTime(req.getEndTime());
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        
        bannerMapper.insert(banner);
        
        log.info("✅ Banner 新增成功，ID：{}", banner.getId());
        return convertToRes(banner);
    }

    /**
     * 更新 Banner
     */
    @Override
    @Transactional
    public BannerRes updateBanner(String id, BannerUpdateReq req) {
        log.info("✏️ 更新 Banner，ID：{}", id);
        
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            log.warn("❌ Banner 不存在，ID：{}", id);
            throw new RuntimeException("Banner 不存在");
        }
        
        // 更新欄位（只更新非 null 的欄位）
        if (req.getStoreId() != null) {
            // 檢查店家是否存在
            Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
            if (store == null) {
                log.warn("❌ 店家不存在，ID：{}", req.getStoreId());
                throw new RuntimeException("店家不存在");
            }
            banner.setStoreId(req.getStoreId());
        }
        if (req.getTitle() != null) {
            banner.setTitle(req.getTitle());
        }
        if (req.getImageUrl() != null) {
            banner.setImageUrl(req.getImageUrl());
        }
        if (req.getOrderNum() != null) {
            banner.setOrderNum(req.getOrderNum());
        }
        if (req.getStatus() != null) {
            banner.setStatus(req.getStatus());
        }
        if (req.getStartTime() != null) {
            banner.setStartTime(req.getStartTime());
        }
        if (req.getEndTime() != null) {
            banner.setEndTime(req.getEndTime());
        }
        
        banner.setUpdatedAt(LocalDateTime.now());
        
        bannerMapper.updateByPrimaryKeySelective(banner);
        
        log.info("✅ Banner 更新成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    /**
     * 刪除 Banner
     */
    @Override
    @Transactional
    public void deleteBanner(String id) {
        log.info("🗑️ 刪除 Banner，ID：{}", id);
        
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            log.warn("❌ Banner 不存在，ID：{}", id);
            throw new RuntimeException("Banner 不存在");
        }
        
        bannerMapper.deleteByPrimaryKey(id);
        log.info("✅ Banner 刪除成功：{}", banner.getTitle());
    }

    /**
     * 上架 Banner
     */
    @Override
    @Transactional
    public BannerRes publishBanner(String id) {
        log.info("📢 上架 Banner，ID：{}", id);
        
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            log.warn("❌ Banner 不存在，ID：{}", id);
            throw new RuntimeException("Banner 不存在");
        }
        
        banner.setStatus("PUBLISHED");
        banner.setStartTime(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        
        bannerMapper.updateByPrimaryKeySelective(banner);
        
        log.info("✅ Banner 上架成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    /**
     * 下架 Banner
     */
    @Override
    @Transactional
    public BannerRes unpublishBanner(String id) {
        log.info("📦 下架 Banner，ID：{}", id);
        
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            log.warn("❌ Banner 不存在，ID：{}", id);
            throw new RuntimeException("Banner 不存在");
        }
        
        banner.setStatus("UNPUBLISHED");
        banner.setEndTime(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        
        bannerMapper.updateByPrimaryKeySelective(banner);
        
        log.info("✅ Banner 下架成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    /**
     * 更新 Banner 排序
     */
    @Override
    @Transactional
    public BannerRes updateBannerOrder(String id, Integer orderNum) {
        log.info("🔢 更新 Banner 排序，ID：{}，新排序：{}", id, orderNum);
        
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            log.warn("❌ Banner 不存在，ID：{}", id);
            throw new RuntimeException("Banner 不存在");
        }
        
        banner.setOrderNum(orderNum);
        banner.setUpdatedAt(LocalDateTime.now());
        
        bannerMapper.updateByPrimaryKeySelective(banner);
        
        log.info("✅ Banner 排序更新成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    /**
     * 查詢前台輪播 Banner（僅 PUBLISHED 且店家 ACTIVE）
     */
    @Override
    public List<BannerRes> getCarouselBanners() {
        log.info("🔍 查詢前台輪播 Banner");
        
        BannerExample example = new BannerExample();
        BannerExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("PUBLISHED");
        
        // 已經到了上架時間
        criteria.andStartTimeLessThanOrEqualTo(LocalDateTime.now());
        
        // 檢查下架時間
        BannerExample.Criteria criteria2 = example.or();
        criteria2.andStatusEqualTo("PUBLISHED");
        criteria2.andStartTimeLessThanOrEqualTo(LocalDateTime.now());
        criteria2.andEndTimeIsNull();
        
        BannerExample.Criteria criteria3 = example.or();
        criteria3.andStatusEqualTo("PUBLISHED");
        criteria3.andStartTimeLessThanOrEqualTo(LocalDateTime.now());
        criteria3.andEndTimeGreaterThan(LocalDateTime.now());
        
        example.setOrderByClause("order_num ASC");
        
        List<Banner> banners = bannerMapper.selectByExample(example);
        
        // 過濾店家狀態（只顯示 ACTIVE 的店家）
        List<BannerRes> results = banners.stream()
                .filter(banner -> {
                    Store store = storeMapper.selectByPrimaryKey(banner.getStoreId());
                    return store != null && "ACTIVE".equals(store.getStatus());
                })
                .map(this::convertToRes)
                .collect(Collectors.toList());
        
        log.info("✅ 查詢到 {} 個前台輪播 Banner", results.size());
        return results;
    }

    /**
     * 轉換 Entity 為 Response DTO
     */
    private BannerRes convertToRes(Banner banner) {
        // 查詢店家名稱
        Store store = storeMapper.selectByPrimaryKey(banner.getStoreId());
        String storeName = store != null ? store.getStoreName() : "未知店家";
        
        return BannerRes.builder()
                .id(banner.getId())
                .storeId(banner.getStoreId())
                .storeName(storeName)
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .orderNum(banner.getOrderNum())
                .status(banner.getStatus())
                .statusName(getStatusName(banner.getStatus()))
                .startTime(banner.getStartTime())
                .endTime(banner.getEndTime())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }

    /**
     * 取得狀態名稱
     */
    private String getStatusName(String status) {
        switch (status) {
            case "PUBLISHED":
                return "已上架";
            case "UNPUBLISHED":
                return "未上架";
            default:
                return status;
        }
    }

    /**
     * 檢查字串是否非空白
     * 空字串 "" 會被視為 null 處理
     */
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
