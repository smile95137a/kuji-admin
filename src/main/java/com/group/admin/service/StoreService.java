package com.group.admin.service;

import com.group.admin.condition.StoreCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.store.UpdateStoreReq;
import com.group.admin.res.store.StoreRes;

import java.util.List;

/**
 * 店家服務
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface StoreService {

    /**
     * 查詢店家列表
     * 
     * @param req 查詢請求
     * @return 店家列表
     */
    List<StoreRes> queryStores(QueryReq<StoreCondition> req);

    /**
     * 查詢店家詳情
     * 
     * @param storeId 店家 ID
     * @return 店家詳情
     */
    StoreRes getStoreById(String storeId);

    /**
     * 更新店家資訊
     * 
     * @param storeId 店家 ID
     * @param req 更新請求
     * @return 更新後的店家資訊
     */
    StoreRes updateStore(String storeId, UpdateStoreReq req);

    /**
     * 啟用店家
     * 
     * @param storeId 店家 ID
     */
    void activateStore(String storeId);

    /**
     * 停用店家
     * 
     * @param storeId 店家 ID
     */
    void deactivateStore(String storeId);
}
