package com.group.admin.mapper;

import com.group.admin.dto.AdminAccountDetailDO;
import com.group.admin.entity.AdminUser;
import com.group.admin.example.AdminUserExample;
import com.group.admin.req.admin.AccountFilterCondition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 後台使用者 Mapper
 */
public interface AdminUserMapper {

    /**
     * 依條件查詢筆數
     *
     * @param example 查詢條件
     * @return 筆數
     */
    long countByExample(AdminUserExample example);

    /**
     * 依條件刪除
     *
     * @param example 查詢條件
     * @return 影響筆數
     */
    int deleteByExample(AdminUserExample example);

    /**
     * 依主鍵刪除
     *
     * @param id 主鍵 UUID
     * @return 影響筆數
     */
    int deleteByPrimaryKey(String id);

    /**
     * 新增後台使用者
     *
     * @param row 後台使用者
     * @return 影響筆數
     */
    int insert(AdminUser row);

    /**
     * 選擇性新增後台使用者
     *
     * @param row 後台使用者
     * @return 影響筆數
     */
    int insertSelective(AdminUser row);

    /**
     * 依條件查詢後台使用者，含 BLOB 欄位
     *
     * @param example 查詢條件
     * @return 後台使用者列表
     */
    List<AdminUser> selectByExampleWithBLOBs(AdminUserExample example);

    /**
     * 依條件查詢後台使用者，不含 BLOB 欄位
     *
     * @param example 查詢條件
     * @return 後台使用者列表
     */
    List<AdminUser> selectByExample(AdminUserExample example);

    /**
     * 依主鍵查詢後台使用者
     *
     * @param id 主鍵 UUID
     * @return 後台使用者
     */
    AdminUser selectByPrimaryKey(String id);

    /**
     * 依 Email 查詢後台使用者
     *
     * 轉成廠商時用來檢查 Email 是否已存在。
     *
     * @param email 電子郵件
     * @return 後台使用者
     */
    AdminUser selectByEmail(@Param("email") String email);

    /**
     * 依帳號查詢後台使用者
     *
     * 轉成廠商時用來檢查 username 是否已存在。
     *
     * @param username 使用者帳號
     * @return 後台使用者
     */
    AdminUser selectByUsername(@Param("username") String username);

    /**
     * 依條件選擇性更新後台使用者
     *
     * @param row 更新資料
     * @param example 更新條件
     * @return 影響筆數
     */
    int updateByExampleSelective(@Param("row") AdminUser row,
                                 @Param("example") AdminUserExample example);

    /**
     * 依條件更新後台使用者，含 BLOB 欄位
     *
     * @param row 更新資料
     * @param example 更新條件
     * @return 影響筆數
     */
    int updateByExampleWithBLOBs(@Param("row") AdminUser row,
                                 @Param("example") AdminUserExample example);

    /**
     * 依條件更新後台使用者，不含 BLOB 欄位
     *
     * @param row 更新資料
     * @param example 更新條件
     * @return 影響筆數
     */
    int updateByExample(@Param("row") AdminUser row,
                        @Param("example") AdminUserExample example);

    /**
     * 依主鍵選擇性更新後台使用者
     *
     * @param row 後台使用者
     * @return 影響筆數
     */
    int updateByPrimaryKeySelective(AdminUser row);

    /**
     * 依主鍵更新後台使用者，含 BLOB 欄位
     *
     * @param row 後台使用者
     * @return 影響筆數
     */
    int updateByPrimaryKeyWithBLOBs(AdminUser row);

    /**
     * 依主鍵更新後台使用者，不含 BLOB 欄位
     *
     * @param row 後台使用者
     * @return 影響筆數
     */
    int updateByPrimaryKey(AdminUser row);

    // ---------- custom methods ----------

    /**
     * 查詢帳號列表，並帶出角色資訊
     *
     * @param condition 查詢條件
     * @return 帳號與角色資料列表
     */
    List<AdminAccountDetailDO> selectAccountsWithRole(AccountFilterCondition condition);

    /**
     * 查詢帳號列表筆數，並帶出角色篩選條件
     *
     * @param condition 查詢條件
     * @return 筆數
     */
    long countAccountsWithRole(AccountFilterCondition condition);
}