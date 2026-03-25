package com.group.admin.service;

import com.group.admin.req.referral.ReferralCodeCreateReq;
import com.group.admin.req.referral.ReferralCodeUpdateReq;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralRecordRes;
import com.group.admin.res.referral.ReferralStatsRes;

import java.util.List;

/**
 * 推薦碼服務介面
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface ReferralCodeService {
    
    ReferralCodeRes create(ReferralCodeCreateReq req);
    
    ReferralCodeRes update(String id, ReferralCodeUpdateReq req);
    
    void delete(String id);
    
    ReferralCodeRes getById(String id);
    
    ReferralCodeRes getByCode(String code);
    
    List<ReferralCodeRes> getByStoreId(String storeId);
    
    List<ReferralCodeRes> getAll();
    
    boolean validateCode(String code);
    
    boolean useCode(String userId, String code);
    
    List<ReferralRecordRes> getRecordsByCodeId(String referralCodeId);
    
    List<ReferralRecordRes> getRecordsByStoreId(String storeId);
    
    ReferralRecordRes getRecordByUserId(String userId);

    // ========== 012-referral-code new methods ==========

    /**
     * 為使用者產生專屬推薦碼
     */
    ReferralCodeRes generateCode(String userId);

    /**
     * 驗證推薦碼（進階：檢查是否有效、未超過上限、非自己推薦自己）
     */
    ReferralCodeRes validateCodeForUser(String code, String userId);

    /**
     * 套用推薦碼，雙方皆獲得獎勵
     */
    void applyReferral(String refereeId, String code);

    /**
     * 取得使用者的推薦統計
     */
    ReferralStatsRes getMyReferralStats(String userId);

    /**
     * 停用推薦碼
     */
    void disableCode(String codeId, String userId);
}
