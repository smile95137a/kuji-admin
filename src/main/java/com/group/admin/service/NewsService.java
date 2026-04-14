package com.group.admin.service;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.news.NewsCondition;
import com.group.admin.req.news.NewsCreateReq;
import com.group.admin.req.news.NewsUpdateReq;
import com.group.admin.res.news.NewsRes;

import java.util.List;

/**
 * News Service 介面
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface NewsService {
    
    /**
     * 查詢最新消息列表（支援條件查詢）
     * 
     * @param req 查詢請求
     * @return 最新消息列表
     */
    List<NewsRes> queryNews(QueryReq<NewsCondition> req);
    
    /**
     * 查詢單一最新消息詳情
     * 
     * @param id 最新消息 ID
     * @return 最新消息詳情
     */
    NewsRes getNewsById(String id);
    
    /**
     * 新增最新消息
     * 
     * @param req 新增請求
     * @return 新增結果
     */
    NewsRes createNews(NewsCreateReq req);
    
    /**
     * 更新最新消息
     * 
     * @param id 最新消息 ID
     * @param req 更新請求
     * @return 更新結果
     */
    NewsRes updateNews(String id, NewsUpdateReq req);
    
    /**
     * 刪除最新消息
     * 
     * @param id 最新消息 ID
     */
    void deleteNews(String id);
    
    /**
     * 上架最新消息
     * 
     * @param id 最新消息 ID
     * @return 更新結果
     */
    NewsRes publishNews(String id);
    
    /**
     * 下架最新消息
     * 
     * @param id 最新消息 ID
     * @return 更新結果
     */
    NewsRes unpublishNews(String id);
    
    /**
     * 查詢前台最新消息列表（僅 PUBLISHED 狀態）
     * 
     * @param limit 限制數量（首頁顯示用）
     * @return 最新消息列表
     */
    List<NewsRes> getPublishedNews(Integer limit);
    /**
     * 自動上架已到排程時間的草稿
     * 
     * @return 上架數量
     */
    int autoPublishScheduledNews();
    
    /**
     * 自動下架已過下架時間的消息
     * 
     * @return 下架數量
     */
    int autoUnpublishExpiredNews();
}
