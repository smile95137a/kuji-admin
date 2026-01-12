package com.group.admin.service;

import com.group.admin.req.referral.ReferralCodeCreateReq;
import com.group.admin.req.referral.ReferralCodeUpdateReq;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralRecordRes;

import java.util.List;

/**
 * 推薦碼服務介面
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface ReferralCodeService {
    
    /**
     * 建立推薦碼
     * 
     * @param req 建立請求
     * @return 推薦碼回應
     */
    ReferralCodeRes create(ReferralCodeCreateReq req);
    
    /**
     * 更新推薦碼
     * 
     * @param id 推薦碼 ID
     * @param req 更新請求
     * @return 推薦碼回應
     */
    ReferralCodeRes update(String id, ReferralCodeUpdateReq req);
    
    /**
     * 刪除推薦碼
     * 
     * @param id 推薦碼 ID
     */
    void delete(String id);
    
    /**
     * 取得推薦碼詳情
     * 
     * @param id 推薦碼 ID
     * @return 推薦碼回應
     */
    ReferralCodeRes getById(String id);
    
    /**
     * 根據推薦碼查詢
     * 
     * @param code 推薦碼
     * @return 推薦碼回應（如果不存在返回 null）
     */
    ReferralCodeRes getByCode(String code);
    
    /**
     * 取得店家的所有推薦碼
     * 
     * @param storeId 店家 ID
     * @return 推薦碼列表
     */
    List<ReferralCodeRes> getByStoreId(String storeId);
    
    /**
     * 取得所有推薦碼（管理員使用）
     * 
     * @return 推薦碼列表
     */
    List<ReferralCodeRes> getAll();
    
    /**
     * 驗證推薦碼是否有效
     * 
     * @param code 推薦碼
     * @return 是否有效
     */
    boolean validateCode(String code);
    
    /**
     * 使用推薦碼（註冊時呼叫）
     * 
     * @param userId 新註冊使用者 ID
     * @param code 推薦碼
     * @return 是否成功使用
     */
    boolean useCode(String userId, String code);
    
    /**
     * 取得推薦碼的使用記錄
     * 
     * @param referralCodeId 推薦碼 ID
     * @return 推薦記錄列表
     */
    List<ReferralRecordRes> getRecordsByCodeId(String referralCodeId);
    
    /**
     * 取得店家的所有推薦記錄
     * 
     * @param storeId 店家 ID
     * @return 推薦記錄列表
     */
    List<ReferralRecordRes> getRecordsByStoreId(String storeId);
    
    /**
     * 取得使用者的推薦記錄（該使用者被誰推薦）
     * 
     * @param userId 使用者 ID
     * @return 推薦記錄（如果不存在返回 null）
     */
    ReferralRecordRes getRecordByUserId(String userId);
}
