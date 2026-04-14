package com.group.admin.service;

import com.group.admin.condition.report.ReferralReportCondition;
import com.group.admin.req.referral.ReferralCodeCreateReq;
import com.group.admin.req.referral.ReferralCodeUpdateReq;
import com.group.admin.res.referral.AdminReferralStatsRes;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralRecordRes;
import com.group.admin.res.referral.ReferralStatsRes;
import com.group.admin.res.referral.ReferralValidateRes;

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

    /** 取得所有推薦碼（支援 storeId / isActive 篩選，null 表示不篩選） */
    List<ReferralCodeRes> getAll(String storeId, Boolean isActive);

    boolean validateCode(String code);

    boolean useCode(String userId, String code);

    List<ReferralRecordRes> getRecordsByCodeId(String referralCodeId);

    List<ReferralRecordRes> getRecordsByStoreId(String storeId);

    ReferralRecordRes getRecordByUserId(String userId);

    // ========== 012-referral-code new methods ==========

    /** 為使用者產生專屬推薦碼 */
    ReferralCodeRes generateCode(String userId);

    /** 驗證推薦碼（進階：檢查是否有效、未超過上限、非自己推薦自己） */
    ReferralCodeRes validateCodeForUser(String code, String userId);

    /** 套用推薦碼，雙方皆獲得獎勵 */
    void applyReferral(String refereeId, String code);

    /** 取得使用者的推薦統計 */
    ReferralStatsRes getMyReferralStats(String userId);

    /** 停用推薦碼（舊版：附帶擁有者驗證） */
    void disableCode(String codeId, String userId);

    // ===== US1: Admin disable code =====

    /** 停用推薦碼（管理員版，僅需 id，回傳更新後的 Res） */
    ReferralCodeRes disableCode(String id);

    // ===== US2: Validate & registration =====

    /** 驗證推薦碼是否可用（公開端點用，回傳 storeName） */
    ReferralValidateRes validateForRegistration(String code);

    /**
     * 使用推薦碼（含店家狀態與自我推薦防護）。
     * 不同於舊版 boolean useCode()，此版本在無效時拋出 BusinessException。
     */
    void useCode(String userId, String code, String registrationEmail);

    // ===== US3: Admin referral stats =====

    /** 取得各店家推薦統計（含每日時間軸） */
    List<AdminReferralStatsRes> getReferralStats(ReferralReportCondition condition);
}
