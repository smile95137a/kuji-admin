package com.group.admin.service;

import com.group.admin.req.address.UserAddressCreateReq;
import com.group.admin.req.address.UserAddressUpdateReq;
import com.group.admin.res.address.UserAddressRes;

import java.util.List;

/**
 * 使用者地址服務介面
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface UserAddressService {
    
    /**
     * 建立收件地址
     * 
     * @param userId 使用者 ID
     * @param req 建立請求
     * @return 地址回應
     */
    UserAddressRes create(String userId, UserAddressCreateReq req);
    
    /**
     * 更新收件地址
     * 
     * @param userId 使用者 ID
     * @param addressId 地址 ID
     * @param req 更新請求
     * @return 地址回應
     */
    UserAddressRes update(String userId, String addressId, UserAddressUpdateReq req);
    
    /**
     * 刪除收件地址
     * 
     * @param userId 使用者 ID
     * @param addressId 地址 ID
     */
    void delete(String userId, String addressId);
    
    /**
     * 取得地址詳情
     * 
     * @param userId 使用者 ID
     * @param addressId 地址 ID
     * @return 地址回應
     */
    UserAddressRes getById(String userId, String addressId);
    
    /**
     * 取得使用者的所有地址
     * 
     * @param userId 使用者 ID
     * @return 地址列表
     */
    List<UserAddressRes> getByUserId(String userId);
    
    /**
     * 取得使用者的預設地址
     * 
     * @param userId 使用者 ID
     * @return 預設地址（如果不存在返回 null）
     */
    UserAddressRes getDefaultByUserId(String userId);
    
    /**
     * 設定預設地址
     * 
     * @param userId 使用者 ID
     * @param addressId 地址 ID
     * @return 地址回應
     */
    UserAddressRes setDefault(String userId, String addressId);
}
