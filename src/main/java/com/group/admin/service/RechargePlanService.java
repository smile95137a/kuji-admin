package com.group.admin.service;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.recharge.RechargePlanCondition;
import com.group.admin.req.recharge.RechargePlanCreateReq;
import com.group.admin.req.recharge.RechargePlanUpdateReq;
import com.group.admin.res.wallet.RechargePlanRes;

import java.util.List;

/**
 * 儲值方案服務介面
 * 管理儲值方案的 CRUD
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
public interface RechargePlanService {
    
    /**
     * 新增儲值方案
     * 
     * @param req 新增請求
     * @return 方案 ID
     */
    String createPlan(RechargePlanCreateReq req);
    
    /**
     * 更新儲值方案
     * 
     * @param id 方案 ID
     * @param req 更新請求
     */
    void updatePlan(String id, RechargePlanUpdateReq req);
    
    /**
     * 刪除儲值方案（軟刪除）
     * 
     * @param id 方案 ID
     */
    void deletePlan(String id);
    
    /**
     * 取得所有有效的儲值方案（前台顯示）
     * 自動過濾：is_active=true、未刪除、活動期間內
     * 
     * @return 有效方案列表
     */
    List<RechargePlanRes> getActivePlans();
    
    /**
     * 取得所有儲值方案（後台管理）
     * 
     * @return 全部方案列表
     */
    List<RechargePlanRes> getAllPlans();
    
    /**
     * 取得儲值方案詳情
     * 
     * @param id 方案 ID
     * @return 方案詳情
     */
    RechargePlanRes getPlanDetail(String id);
    
    /**
     * 查詢儲值方案列表（支援條件查詢）
     * 
     * @param req 查詢請求
     * @return 方案列表
     */
    List<RechargePlanRes> queryPlans(QueryReq<RechargePlanCondition> req);
}
