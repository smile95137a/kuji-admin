package com.group.admin.service;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.user.FrontendUserCondition;
import com.group.admin.req.user.FrontendUserUpdateReq;
import com.group.admin.res.user.FrontendUserRes;

import java.util.List;

/**
 * 前台會員管理服務
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface FrontendUserService {
    
    /**
     * 查詢前台會員列表
     * 
     * @param req 查詢請求
     * @return 會員列表
     */
    List<FrontendUserRes> queryUsers(QueryReq<FrontendUserCondition> req);
    
    /**
     * 取得會員詳情
     * 
     * @param id 會員 ID
     * @return 會員詳情
     */
    FrontendUserRes getUserById(String id);
    
    /**
     * 更新會員資訊
     * 
     * @param id 會員 ID
     * @param req 更新請求
     * @return 更新後的會員資訊
     */
    FrontendUserRes updateUser(String id, FrontendUserUpdateReq req);
    
    /**
     * 軟刪除會員（標記為 DELETED）
     * 
     * @param id 會員 ID
     */
    void deleteUser(String id);
    
    /**
     * 啟用會員
     * 
     * @param id 會員 ID
     */
    void activateUser(String id);
    
    /**
     * 停用會員
     * 
     * @param id 會員 ID
     */
    void deactivateUser(String id);
    
    /**
     * 暫停會員使用
     * 
     * @param id 會員 ID
     */
    void suspendUser(String id);
}
