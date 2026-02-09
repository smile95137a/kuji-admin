package com.group.admin.service.impl;

import com.group.admin.entity.News;
import com.group.admin.example.NewsExample;
import com.group.admin.mapper.NewsMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.news.NewsCondition;
import com.group.admin.req.news.NewsCreateReq;
import com.group.admin.req.news.NewsUpdateReq;
import com.group.admin.res.news.NewsRes;
import com.group.admin.service.NewsService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 最新消息 Service 實作
 * 
 * <p>提供最新消息的 CRUD 操作與狀態管理</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;

    /**
     * 查詢最新消息列表（後台用，支援動態條件）
     */
    @Override
    public List<NewsRes> queryNews(QueryReq<NewsCondition> req) {
        log.info("🔍 查詢最新消息列表，條件：{}", req);
        
        NewsCondition condition = req != null ? req.getCondition() : null;
        
        NewsExample example = new NewsExample();
        NewsExample.Criteria criteria = example.createCriteria();
        
        // 動態條件查詢
        if (condition != null) {
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
            
            // 關鍵字搜尋（僅搜尋標題）
            if (isNotBlank(condition.getKeyword())) {
                criteria.andTitleLike("%" + condition.getKeyword() + "%");
            }
            
            // ⚠️ category 與 important 為新增欄位，NewsExample 尚未更新
            // 改為查詢後在 Java 層篩選
        }
        
        // 排序
        if (req != null && req.getSortBy() != null) {
            String sortOrder = req.getSortOrder() != null ? req.getSortOrder() : "DESC";
            example.setOrderByClause(req.getSortBy() + " " + sortOrder);
        } else {
            // 預設按建立時間降序
            example.setOrderByClause("created_at DESC");
        }
        
        List<News> newsList = newsMapper.selectByExampleWithBLOBs(example);
        
        // Java 層篩選 category 與 important
        if (condition != null) {
            if (isNotBlank(condition.getCategory())) {
                String cat = condition.getCategory();
                newsList = newsList.stream()
                    .filter(n -> cat.equals(n.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (condition.getImportant() != null && condition.getImportant()) {
                newsList = newsList.stream()
                    .filter(n -> n.getImportant() != null && n.getImportant())
                    .collect(java.util.stream.Collectors.toList());
            }
        }
        
        log.info("✅ 查詢到 {} 則最新消息", newsList.size());
        
        return newsList.stream()
                .map(this::convertToRes)
                .collect(Collectors.toList());
    }

    /**
     * 根據 ID 查詢最新消息詳情
     */
    @Override
    public NewsRes getNewsById(String id) {
        log.info("🔍 查詢最新消息詳情，ID：{}", id);
        
        News news = newsMapper.selectByPrimaryKey(id);
        if (news == null) {
            log.warn("❌ 最新消息不存在，ID：{}", id);
            throw new RuntimeException("最新消息不存在");
        }
        
        log.info("✅ 查詢成功：{}", news.getTitle());
        return convertToRes(news);
    }

    /**
     * 新增最新消息
     */
    @Override
    @Transactional
    public NewsRes createNews(NewsCreateReq req) {
        log.info("➕ 新增最新消息：{}", req.getTitle());
        
        // 取得當前使用者 ID
        String currentUserId = SecurityUtils.getCurrentAdminUserId();
        
        News news = new News();
        news.setId(UUID.randomUUID().toString());
        news.setTitle(req.getTitle());
        news.setContent(req.getContent());
        news.setImageUrl(req.getImageUrl());
        news.setStatus(req.getStatus() != null ? req.getStatus() : "DRAFT");
        news.setCategory(req.getCategory() != null ? req.getCategory() : "ANNOUNCEMENT");
        news.setImportant(req.getImportant() != null && req.getImportant() ? true : false);
        news.setScheduledAt(req.getScheduledAt());
        news.setEndTime(req.getEndTime());
        news.setCreatedBy(currentUserId);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        
        newsMapper.insert(news);
        
        log.info("✅ 最新消息新增成功，ID：{}", news.getId());
        return convertToRes(news);
    }

    /**
     * 更新最新消息
     */
    @Override
    @Transactional
    public NewsRes updateNews(String id, NewsUpdateReq req) {
        log.info("✏️ 更新最新消息，ID：{}", id);
        
        News news = newsMapper.selectByPrimaryKey(id);
        if (news == null) {
            log.warn("❌ 最新消息不存在，ID：{}", id);
            throw new RuntimeException("最新消息不存在");
        }
        
        // 更新欄位（只更新非 null 的欄位）
        if (req.getTitle() != null) {
            news.setTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            news.setContent(req.getContent());
        }
        if (req.getImageUrl() != null) {
            news.setImageUrl(req.getImageUrl());
        }
        if (req.getStatus() != null) {
            news.setStatus(req.getStatus());
        }
        if (req.getCategory() != null) {
            news.setCategory(req.getCategory());
        }
        if (req.getImportant() != null) {
            news.setImportant(req.getImportant());
        }
        if (req.getScheduledAt() != null) {
            news.setScheduledAt(req.getScheduledAt());
        }
        if (req.getEndTime() != null) {
            news.setEndTime(req.getEndTime());
        }
        
        news.setUpdatedAt(LocalDateTime.now());
        
        newsMapper.updateByPrimaryKeyWithBLOBs(news);
        
        log.info("✅ 最新消息更新成功：{}", news.getTitle());
        return convertToRes(news);
    }

    /**
     * 刪除最新消息
     */
    @Override
    @Transactional
    public void deleteNews(String id) {
        log.info("🗑️ 刪除最新消息，ID：{}", id);
        
        News news = newsMapper.selectByPrimaryKey(id);
        if (news == null) {
            log.warn("❌ 最新消息不存在，ID：{}", id);
            throw new RuntimeException("最新消息不存在");
        }
        
        newsMapper.deleteByPrimaryKey(id);
        log.info("✅ 最新消息刪除成功：{}", news.getTitle());
    }

    /**
     * 上架最新消息
     */
    @Override
    @Transactional
    public NewsRes publishNews(String id) {
        log.info("📢 上架最新消息，ID：{}", id);
        
        News news = newsMapper.selectByPrimaryKey(id);
        if (news == null) {
            log.warn("❌ 最新消息不存在，ID：{}", id);
            throw new RuntimeException("最新消息不存在");
        }
        
        news.setStatus("PUBLISHED");
        news.setScheduledAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        
        newsMapper.updateByPrimaryKeySelective(news);
        
        log.info("✅ 最新消息上架成功：{}", news.getTitle());
        return convertToRes(news);
    }

    /**
     * 下架最新消息
     */
    @Override
    @Transactional
    public NewsRes unpublishNews(String id) {
        log.info("📦 下架最新消息，ID：{}", id);
        
        News news = newsMapper.selectByPrimaryKey(id);
        if (news == null) {
            log.warn("❌ 最新消息不存在，ID：{}", id);
            throw new RuntimeException("最新消息不存在");
        }
        
        news.setStatus("ARCHIVED");
        news.setEndTime(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        
        newsMapper.updateByPrimaryKeySelective(news);
        
        log.info("✅ 最新消息下架成功：{}", news.getTitle());
        return convertToRes(news);
    }

    /**
     * 查詢已上架的最新消息（前台用）
     */
    @Override
    public List<NewsRes> getPublishedNews(Integer limit) {
        log.info("🔍 查詢前台最新消息列表，限制數量：{}", limit);
        
        NewsExample example = new NewsExample();
        NewsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("PUBLISHED");
        
        // 已經到了上架時間
        criteria.andScheduledAtLessThanOrEqualTo(LocalDateTime.now());
        
        // 檢查下架時間
        NewsExample.Criteria criteria2 = example.or();
        criteria2.andStatusEqualTo("PUBLISHED");
        criteria2.andScheduledAtLessThanOrEqualTo(LocalDateTime.now());
        criteria2.andEndTimeIsNull();
        
        NewsExample.Criteria criteria3 = example.or();
        criteria3.andStatusEqualTo("PUBLISHED");
        criteria3.andScheduledAtLessThanOrEqualTo(LocalDateTime.now());
        criteria3.andEndTimeGreaterThan(LocalDateTime.now());
        
        example.setOrderByClause("scheduled_at DESC");
        
        List<News> newsList = newsMapper.selectByExampleWithBLOBs(example);
        
        // 限制數量
        if (limit != null && limit > 0 && newsList.size() > limit) {
            newsList = newsList.subList(0, limit);
        }
        
        log.info("✅ 查詢到 {} 則已上架最新消息", newsList.size());
        
        return newsList.stream()
                .map(this::convertToRes)
                .collect(Collectors.toList());
    }

    /**
     * 轉換 Entity 為 Response DTO
     */
    private NewsRes convertToRes(News news) {
        return NewsRes.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .imageUrl(news.getImageUrl())
                .status(news.getStatus())
                .statusName(getStatusName(news.getStatus()))
                .category(news.getCategory())
                .categoryName(getCategoryName(news.getCategory()))
                .important(news.getImportant() != null && news.getImportant())
                .scheduledAt(news.getScheduledAt())
                .endTime(news.getEndTime())
                .createdBy(news.getCreatedBy())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
    }

    /**
     * 取得狀態名稱
     */
    private String getStatusName(String status) {
        switch (status) {
            case "DRAFT":
                return "草稿";
            case "PUBLISHED":
                return "已上架";
            case "ARCHIVED":
                return "已下架";
            default:
                return status;
        }
    }

    /**
     * 取得分類名稱
     */
    private String getCategoryName(String category) {
        if (category == null) return "公告";
        switch (category) {
            case "ANNOUNCEMENT":
                return "公告";
            case "EVENT":
                return "活動";
            case "SYSTEM":
                return "系統";
            default:
                return category;
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
