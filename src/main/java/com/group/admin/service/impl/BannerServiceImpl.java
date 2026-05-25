package com.group.admin.service.impl;

import com.group.admin.entity.Banner;
import com.group.admin.entity.Store;
import com.group.admin.example.BannerExample;
import com.group.admin.exception.BusinessException;
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

    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_UNPUBLISHED = "UNPUBLISHED";
    private static final String STATUS_SCHEDULED = "SCHEDULED";

    private final BannerMapper bannerMapper;
    private final StoreMapper storeMapper;

    @Override
    public List<BannerRes> queryBanners(QueryReq<BannerCondition> req) {
        BannerCondition condition = req != null ? req.getCondition() : null;

        BannerExample example = new BannerExample();
        BannerExample.Criteria criteria = example.createCriteria();

        if (condition != null) {
            if (isNotBlank(condition.getStoreId())) {
                criteria.andStoreIdEqualTo(condition.getStoreId());
            }
            if (isNotBlank(condition.getTitle())) {
                criteria.andTitleLike("%" + condition.getTitle().trim() + "%");
            }
            if (isNotBlank(condition.getKeyword())) {
                criteria.andTitleLike("%" + condition.getKeyword().trim() + "%");
            }

            String normalizedStatus = normalizeBannerStatus(condition.getStatus(), false);
            if (normalizedStatus != null) {
                criteria.andStatusIn(resolveQueryableStatuses(normalizedStatus));
            }

            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(condition.getCreatedAtStart().atStartOfDay());
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(condition.getCreatedAtEnd().atTime(23, 59, 59));
            }
        }

        if (req != null && isNotBlank(req.getSortBy())) {
            String sortOrder = isNotBlank(req.getSortOrder()) ? req.getSortOrder().trim() : "ASC";
            example.setOrderByClause(req.getSortBy().trim() + " " + sortOrder);
        } else {
            example.setOrderByClause("order_num ASC, created_at DESC");
        }

        return bannerMapper.selectByExample(example).stream()
                .map(this::convertToRes)
                .collect(Collectors.toList());
    }

    @Override
    public BannerRes getBannerById(String id) {
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new RuntimeException("Banner 不存在");
        }
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public BannerRes createBanner(BannerCreateReq req) {
        String normalizedStoreId = trimToNull(req.getStoreId());
        validateStoreIfNeeded(normalizedStoreId);
        validateStoreBannerUniqueness(normalizedStoreId, null);
        validateDisplayTimeRange(req.getStartTime(), req.getEndTime());

        Banner banner = new Banner();
        banner.setId(UUID.randomUUID().toString());
        banner.setStoreId(normalizedStoreId);
        banner.setTitle(req.getTitle());
        banner.setImageUrl(req.getImageUrl());
        banner.setLinkUrl(trimToNull(req.getLinkUrl()));
        banner.setOrderNum(normalizeOrderNum(req.getOrderNum()));
        banner.setStartTime(req.getStartTime());
        banner.setEndTime(req.getEndTime());
        banner.setStatus(resolveStatusForWrite(req.getStatus(), req.getStartTime(), req.getEndTime()));
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());

        bannerMapper.insert(banner);
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public BannerRes updateBanner(String id, BannerUpdateReq req) {
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new BusinessException("Banner 不存在");
        }

        if (req.getStoreId() != null) {
            String normalizedStoreId = trimToNull(req.getStoreId());
            validateStoreIfNeeded(normalizedStoreId);
            validateStoreBannerUniqueness(normalizedStoreId, id);
            banner.setStoreId(normalizedStoreId);
        }
        if (req.getTitle() != null) {
            banner.setTitle(req.getTitle());
        }
        if (req.getImageUrl() != null) {
            banner.setImageUrl(req.getImageUrl());
        }
        if (req.getLinkUrl() != null) {
            banner.setLinkUrl(trimToNull(req.getLinkUrl()));
        }
        if (req.getOrderNum() != null) {
            banner.setOrderNum(normalizeOrderNum(req.getOrderNum()));
        }
        if (req.getStartTime() != null) {
            banner.setStartTime(req.getStartTime());
        }
        if (req.getEndTime() != null) {
            banner.setEndTime(req.getEndTime());
        }
        validateDisplayTimeRange(banner.getStartTime(), banner.getEndTime());
        boolean shouldRecalculateStatus = req.getStatus() != null
                || req.getStartTime() != null
                || req.getEndTime() != null;
        if (shouldRecalculateStatus) {
            String statusInput = req.getStatus() != null ? req.getStatus() : banner.getStatus();
            banner.setStatus(resolveStatusForWrite(statusInput, banner.getStartTime(), banner.getEndTime()));
        }
        banner.setUpdatedAt(LocalDateTime.now());

        bannerMapper.updateByPrimaryKeySelective(banner);
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public void deleteBanner(String id) {
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new BusinessException("Banner 不存在");
        }
        bannerMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public BannerRes publishBanner(String id) {
        Banner banner = requireBanner(id);
        banner.setStatus(resolveStatusForPublishAction(banner));
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.updateByPrimaryKeySelective(banner);
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public BannerRes unpublishBanner(String id) {
        Banner banner = requireBanner(id);
        banner.setStatus(STATUS_UNPUBLISHED);
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.updateByPrimaryKeySelective(banner);
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public BannerRes updateBannerOrder(String id, Integer orderNum) {
        Banner banner = requireBanner(id);
        banner.setOrderNum(normalizeOrderNum(orderNum));
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.updateByPrimaryKeySelective(banner);
        return convertToRes(banner);
    }

    @Override
    @Transactional
    public void reorderBanners(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        int order = 0;
        for (String id : ids) {
            if (!isNotBlank(id)) {
                continue;
            }
            Banner banner = requireBanner(id);
            banner.setOrderNum(order++);
            banner.setUpdatedAt(LocalDateTime.now());
            bannerMapper.updateByPrimaryKeySelective(banner);
        }
    }

    @Override
    public List<BannerRes> getCarouselBanners() {
        autoPublishBanners();
        autoUnpublishBanners();
        return bannerMapper.selectActiveBanners();
    }

    @Override
    @Transactional
    public void autoPublishBanners() {
        int count = bannerMapper.autoPublishBanners();
        if (count > 0) {
            log.info("自動上架 Banner 完成，筆數={}", count);
        }
    }

    @Override
    @Transactional
    public void autoUnpublishBanners() {
        int count = bannerMapper.autoUnpublishBanners();
        if (count > 0) {
            log.info("自動下架 Banner 完成，筆數={}", count);
        }
    }

    @Override
    @Transactional
    public void unpublishBannersByStoreId(String storeId) {
        int count = bannerMapper.unpublishBannersByStoreId(storeId);
        log.info("店家停用後同步下架 Banner，storeId={}，筆數={}", storeId, count);
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

        String normalizedStatus = normalizeBannerStatus(banner.getStatus(), true);

        return BannerRes.builder()
                .id(banner.getId())
                .storeId(banner.getStoreId())
                .storeName(storeName)
                .storeLogoUrl(storeLogoUrl)
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .linkUrl(banner.getLinkUrl())
                .orderNum(banner.getOrderNum())
                .status(normalizedStatus)
                .statusName(getStatusName(normalizedStatus))
                .startTime(banner.getStartTime())
                .endTime(banner.getEndTime())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }

    private void validateStoreIfNeeded(String storeId) {
        if (!isNotBlank(storeId)) {
            return;
        }

        Store store = storeMapper.selectByPrimaryKey(storeId.trim());
        if (store == null) {
            throw new BusinessException("店家不存在");
        }
    }

    private void validateStoreBannerUniqueness(String storeId, String excludeBannerId) {
        if (!isNotBlank(storeId)) {
            return;
        }

        BannerExample example = new BannerExample();
        BannerExample.Criteria criteria = example.createCriteria().andStoreIdEqualTo(storeId);
        if (isNotBlank(excludeBannerId)) {
            criteria.andIdNotEqualTo(excludeBannerId);
        }

        long count = bannerMapper.countByExample(example);
        if (count > 0) {
            throw new BusinessException("同一店家只能建立一個 Banner");
        }
    }

    private void validateDisplayTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException("Banner 結束時間不可早於開始時間");
        }
    }

    private int normalizeOrderNum(Integer orderNum) {
        if (orderNum == null) {
            return 0;
        }
        if (orderNum < 0) {
            throw new BusinessException("Banner 排序不可小於 0");
        }
        return orderNum;
    }

    private String resolveStatusForWrite(String requestedStatus, LocalDateTime startTime, LocalDateTime endTime) {
        String normalized = normalizeBannerStatus(requestedStatus, true);
        if (!STATUS_PUBLISHED.equals(normalized)) {
            return normalized;
        }

        LocalDateTime now = LocalDateTime.now();
        if (isWithinDisplayWindow(now, startTime, endTime)) {
            return STATUS_PUBLISHED;
        }
        if (startTime != null && startTime.isAfter(now)) {
            return STATUS_SCHEDULED;
        }
        return STATUS_UNPUBLISHED;
    }

    private String resolveStatusForPublishAction(Banner banner) {
        LocalDateTime now = LocalDateTime.now();
        if (isWithinDisplayWindow(now, banner.getStartTime(), banner.getEndTime())) {
            return STATUS_PUBLISHED;
        }
        if (banner.getStartTime() != null && banner.getStartTime().isAfter(now)) {
            return STATUS_SCHEDULED;
        }
        if (banner.getEndTime() != null && banner.getEndTime().isBefore(now)) {
            throw new BusinessException("顯示時間已結束，無法手動上架 Banner");
        }
        throw new BusinessException("目前不在顯示時間區間，無法手動上架 Banner");
    }

    private boolean isWithinDisplayWindow(LocalDateTime now, LocalDateTime startTime, LocalDateTime endTime) {
        boolean started = startTime == null || !startTime.isAfter(now);
        boolean notEnded = endTime == null || !endTime.isBefore(now);
        return started && notEnded;
    }

    private Banner requireBanner(String id) {
        Banner banner = bannerMapper.selectByPrimaryKey(id);
        if (banner == null) {
            throw new BusinessException("Banner 不存在");
        }
        return banner;
    }

    private String normalizeBannerStatus(String status, boolean defaultUnpublished) {
        if (!isNotBlank(status)) {
            return defaultUnpublished ? STATUS_UNPUBLISHED : null;
        }

        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "PUBLISHED", "ACTIVE" -> STATUS_PUBLISHED;
            case "UNPUBLISHED", "INACTIVE", "ARCHIVED", "SCHEDULED", "EXPIRED" -> STATUS_UNPUBLISHED;
            default -> defaultUnpublished ? STATUS_UNPUBLISHED : null;
        };
    }

    private List<String> resolveQueryableStatuses(String normalizedStatus) {
        if (STATUS_PUBLISHED.equals(normalizedStatus)) {
            return List.of("PUBLISHED", "ACTIVE");
        }
        return List.of("UNPUBLISHED", "INACTIVE", "ARCHIVED", "SCHEDULED", "EXPIRED");
    }

    private String getStatusName(String status) {
        if (STATUS_PUBLISHED.equals(status)) {
            return "已上架";
        }
        if (STATUS_UNPUBLISHED.equals(status)) {
            return "已下架";
        }
        return status == null ? "-" : status;
    }

    private String trimToNull(String value) {
        if (!isNotBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
