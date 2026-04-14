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

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;
    private final StoreMapper storeMapper;

    @Override
    public List<BannerRes> queryBanners(QueryReq<BannerCondition> req) {
        log.info("🔍 查詢 Banner 列表，條件：{}", req);

        BannerCondition condition = req != null ? req.getCondition() : null;

        BannerExample example = new BannerExample();
        BannerExample.Criteria criteria = example.createCriteria();

        if (condition != null) {
            if (isNotBlank(condition.getStoreId())) {
                criteria.andStoreIdEqualTo(condition.getStoreId());
            }
            if (isNotBlank(condition.getTitle())) {
                criteria.andTitleLike("%" + condition.getTitle() + "%");
            }
            if (isNotBlank(condition.getStatus())) {
                criteria.andStatusEqualTo(condition.getStatus());
            }
            if (isNotBlank(condition.getKeyword())) {
                criteria.andTitleLike("%" + condition.getKeyword() + "%");
            }
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(condition.getCreatedAtStart().atStartOfDay());
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(condition.getCreatedAtEnd().atTime(23, 59, 59));
            }
        }

        if (req != null && isNotBlank(req.getSortBy())) {
            String sortOrder = isNotBlank(req.getSortOrder()) ? req.getSortOrder() : "ASC";
            example.setOrderByClause(req.getSortBy() + " " + sortOrder);
        } else {
            example.setOrderByClause("order_num ASC, created_at DESC");
        }

        List<Banner> banners = bannerMapper.selectByExample(example);
        log.info("✅ 查詢到 {} 個 Banner", banners.size());

        return banners.stream()
                .map(this::convertToRes)
                .collect(Collectors.toList());
    }

    @Override
    public BannerRes getBannerById(String id) {
        log.info("🔍 查詢 Banner 詳情，ID：{}", id);
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new RuntimeException("Banner 不存在");
        }
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public BannerRes createBanner(BannerCreateReq req) {
        log.info("➕ 新增 Banner：{}", req.getTitle());

        if (isNotBlank(req.getStoreId())) {
            Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
            if (store == null) {
                throw new RuntimeException("店家不存在");
            }
        }

        Banner banner = new Banner();
        banner.setId(UUID.randomUUID().toString());
        banner.setStoreId(req.getStoreId());
        banner.setTitle(req.getTitle());
        banner.setImageUrl(req.getImageUrl());
        banner.setLinkUrl(req.getLinkUrl());
        banner.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
        // DRAFT if scheduled, ACTIVE immediately otherwise
        banner.setStatus(req.getStartTime() != null ? "DRAFT" : "ACTIVE");
        banner.setStartTime(req.getStartTime());
        banner.setEndTime(req.getEndTime());
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());

        bannerMapper.insert(banner);
        log.info("✅ Banner 新增成功，ID：{}", banner.getId());
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public BannerRes updateBanner(String id, BannerUpdateReq req) {
        log.info("✏️ 更新 Banner，ID：{}", id);
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new RuntimeException("Banner 不存在");
        }

        if (req.getStoreId() != null) {
            Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
            if (store == null) {
                throw new RuntimeException("店家不存在");
            }
            banner.setStoreId(req.getStoreId());
        }
        if (req.getTitle() != null) banner.setTitle(req.getTitle());
        if (req.getImageUrl() != null) banner.setImageUrl(req.getImageUrl());
        if (req.getLinkUrl() != null) banner.setLinkUrl(req.getLinkUrl());
        if (req.getOrderNum() != null) banner.setOrderNum(req.getOrderNum());
        if (req.getStartTime() != null) banner.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) banner.setEndTime(req.getEndTime());
        banner.setUpdatedAt(LocalDateTime.now());

        bannerMapper.updateByPrimaryKeySelective(banner);
        log.info("✅ Banner 更新成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public void deleteBanner(String id) {
        log.info("🗑️ 刪除 Banner，ID：{}", id);
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new RuntimeException("Banner 不存在");
        }
        bannerMapper.deleteByPrimaryKey(id);
        log.info("✅ Banner 刪除成功：{}", banner.getTitle());
    }

    @Override
    @Transactional
    public BannerRes publishBanner(String id) {
        log.info("📢 上架 Banner，ID：{}", id);
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new RuntimeException("Banner 不存在");
        }
        banner.setStatus("ACTIVE");
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.updateByPrimaryKeySelective(banner);
        log.info("✅ Banner 上架成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public BannerRes unpublishBanner(String id) {
        log.info("📦 下架 Banner，ID：{}", id);
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new RuntimeException("Banner 不存在");
        }
        banner.setStatus("INACTIVE");
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.updateByPrimaryKeySelective(banner);
        log.info("✅ Banner 下架成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public BannerRes updateBannerOrder(String id, Integer orderNum) {
        log.info("🔢 更新 Banner 排序，ID：{}，新排序：{}", id, orderNum);
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new RuntimeException("Banner 不存在");
        }
        banner.setOrderNum(orderNum);
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.updateByPrimaryKeySelective(banner);
        log.info("✅ Banner 排序更新成功：{}", banner.getTitle());
        return convertToRes(banner);
    }

    @Override
    public List<BannerRes> getCarouselBanners() {
        log.info("🔍 查詢前台輪播 Banner");
        List<BannerRes> results = bannerMapper.selectActiveBanners();
        log.info("✅ 查詢到 {} 個前台輪播 Banner", results.size());
        return results;
    }

    @Override
    @Transactional
    public void autoPublishBanners() {
        int count = bannerMapper.autoPublishBanners();
        if (count > 0) {
            log.info("⏰ 自動上架 Banner: {} 筆", count);
        }
    }

    @Override
    @Transactional
    public void autoUnpublishBanners() {
        int count = bannerMapper.autoUnpublishBanners();
        if (count > 0) {
            log.info("⏰ 自動下架 Banner: {} 筆", count);
        }
    }

    @Override
    @Transactional
    public void unpublishBannersByStoreId(String storeId) {
        int count = bannerMapper.unpublishBannersByStoreId(storeId);
        log.info("🏪 店家停用，連帶下架 Banner: storeId={}, count={}", storeId, count);
    }

    private BannerRes convertToRes(Banner banner) {
        String storeName = null;
        String storeLogoUrl = null;
        if (isNotBlank(banner.getStoreId())) {
            Store store = storeMapper.selectByPrimaryKey(banner.getStoreId());
            if (store != null) {
                storeName = store.getStoreName();
                storeLogoUrl = store.getLogoUrl();
            }
        }

        return BannerRes.builder()
                .id(banner.getId())
                .storeId(banner.getStoreId())
                .storeName(storeName)
                .storeLogoUrl(storeLogoUrl)
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .linkUrl(banner.getLinkUrl())
                .orderNum(banner.getOrderNum())
                .status(banner.getStatus())
                .statusName(getStatusName(banner.getStatus()))
                .startTime(banner.getStartTime())
                .endTime(banner.getEndTime())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }

    private String getStatusName(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "ACTIVE":   return "已上架";
            case "INACTIVE": return "已下架";
            case "DRAFT":    return "草稿";
            default:         return status;
        }
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
