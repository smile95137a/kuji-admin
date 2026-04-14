package com.group.admin.service;

import com.group.admin.condition.StoreCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.store.CreateStoreReq;
import com.group.admin.req.store.UpdateStoreReq;
import com.group.admin.req.store.UpdateStoreStatusReq;
import com.group.admin.res.store.StoreRes;

import java.util.List;

public interface StoreService {

    List<StoreRes> queryStores(QueryReq<StoreCondition> req);

    StoreRes getStoreById(String storeId);

    StoreRes updateStore(String storeId, UpdateStoreReq req);

    void activateStore(String storeId);

    void deactivateStore(String storeId);

    // ========== 014-store-management new methods ==========

    /**
     * 建立店家（含負責人帳號）
     */
    StoreRes createStore(CreateStoreReq req, String operatorId);

    /**
     * 更新店家狀態
     */
    void updateStoreStatus(String storeId, UpdateStoreStatusReq req, String operatorId);

    /**
     * 前台公開店家列表（僅啟用的）
     */
    List<StoreRes> getPublicStoreList(int page, int size);

    /**
     * 取得店家選項（根據用戶權限過濾）
     */
    List<com.group.admin.res.common.EnumOption> getStoreOptionsForUser(String userId, boolean isAdmin, boolean activeOnly);

    /**
     * 搜尋店家選項（根據關鍵字和用戶權限過濾）
     */
    List<com.group.admin.res.common.EnumOption> searchStoreOptions(String userId, boolean isAdmin, java.util.List<String> storeIds, String keyword, boolean activeOnly);

    /**
     * 取得所有啟用的店家選項（Admin 專用）
     */
    List<com.group.admin.res.common.EnumOption> getAllActiveStoreOptions();
}
