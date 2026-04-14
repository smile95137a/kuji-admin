package com.group.admin.service;

import com.group.admin.condition.StoreCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.store.CreateStoreReq;
import com.group.admin.req.store.UpdateStoreReq;
import com.group.admin.req.store.UpdateStoreStatusReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.store.StoreDetailRes;
import com.group.admin.res.store.StoreListItemRes;
import com.group.admin.res.store.StoreRes;

import java.util.List;
import java.util.Map;

public interface StoreService {

    List<StoreRes> queryStores(QueryReq<StoreCondition> req);

    StoreRes getStoreById(String storeId);

    StoreRes updateStore(String storeId, UpdateStoreReq req);

    void activateStore(String storeId);

    void deactivateStore(String storeId);

    // ========== 014-store-management new methods ==========

    StoreRes createStore(CreateStoreReq req, String operatorId);

    Map<String, Object> updateStoreStatus(String storeId, UpdateStoreStatusReq req, boolean force, String operatorId);

    PageResult<StoreListItemRes> listEnabledStores(int page, int size);

    StoreDetailRes getPublicStoreDetail(String storeId);

    // ========== 店家選項相關 ==========

    List<com.group.admin.res.common.EnumOption> getStoreOptionsForUser(String userId, boolean isAdmin, Boolean activeOnly);

    List<com.group.admin.res.common.EnumOption> searchStoreOptions(String userId, boolean isAdmin, java.util.List<String> storeIds, String keyword, Boolean activeOnly);

    List<com.group.admin.res.common.EnumOption> getAllActiveStoreOptions();
}
