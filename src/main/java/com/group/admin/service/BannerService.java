package com.group.admin.service;

import com.group.admin.req.banner.BannerCondition;
import com.group.admin.req.banner.BannerCreateReq;
import com.group.admin.req.banner.BannerUpdateReq;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.banner.BannerRes;

import java.util.List;

/**
 * Banner Service 介面
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface BannerService {
    
    /**
     * 查詢 Banner 列表（支援條件查詢）
     * 
     * @param req 查詢請求
     * @return Banner 列表
     */
    List<BannerRes> queryBanners(QueryReq<BannerCondition> req);
    
    /**
     * 查詢單一 Banner 詳情
     * 
     * @param id Banner ID
     * @return Banner 詳情
     */
    BannerRes getBannerById(String id);
    
    /**
     * 新增 Banner
     * 
     * @param req 新增請求
     * @return 新增結果
     */
    BannerRes createBanner(BannerCreateReq req);
    
    /**
     * 更新 Banner
     * 
     * @param id Banner ID
     * @param req 更新請求
     * @return 更新結果
     */
    BannerRes updateBanner(String id, BannerUpdateReq req);
    
    /**
     * 刪除 Banner
     * 
     * @param id Banner ID
     */
    void deleteBanner(String id);
    
    /**
     * 上架 Banner
     * 
     * @param id Banner ID
     * @return 更新結果
     */
    BannerRes publishBanner(String id);
    
    /**
     * 下架 Banner
     * 
     * @param id Banner ID
     * @return 更新結果
     */
    BannerRes unpublishBanner(String id);
    
    /**
     * 更新 Banner 排序
     * 
     * @param id Banner ID
     * @param orderNum 新的排序號
     * @return 更新結果
     */
    BannerRes updateBannerOrder(String id, Integer orderNum);
    
    /**
     * 查詢前台輪播 Banner（僅 PUBLISHED 狀態且店家 ACTIVE）
     * 
     * @return Banner 列表（按 order_num 升序）
     */
    List<BannerRes> getCarouselBanners();
}
