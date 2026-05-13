package com.group.admin.mapper;

import com.group.admin.entity.Role;
import com.group.admin.example.RoleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
public interface RoleMapper {

    /**
     * 依條件查詢筆數
     *
     * @param example 查詢條件
     * @return 筆數
     */
    long countByExample(RoleExample example);

    /**
     * 依條件刪除
     *
     * @param example 查詢條件
     * @return 影響筆數
     */
    int deleteByExample(RoleExample example);

    /**
     * 依主鍵刪除
     *
     * @param id 主鍵 UUID
     * @return 影響筆數
     */
    int deleteByPrimaryKey(String id);

    /**
     * 新增角色
     *
     * @param row 角色資料
     * @return 影響筆數
     */
    int insert(Role row);

    /**
     * 選擇性新增角色
     *
     * @param row 角色資料
     * @return 影響筆數
     */
    int insertSelective(Role row);

    /**
     * 依條件查詢角色
     *
     * @param example 查詢條件
     * @return 角色列表
     */
    List<Role> selectByExample(RoleExample example);

    /**
     * 依主鍵查詢角色
     *
     * @param id 主鍵 UUID
     * @return 角色資料
     */
    Role selectByPrimaryKey(String id);

    /**
     * 依角色代碼查詢角色
     *
     * 轉成廠商帳號時會用 code = VENDOR 查詢廠商角色。
     *
     * @param code 角色代碼
     * @return 角色資料
     */
    Role selectByCode(@Param("code") String code);

    /**
     * 依條件選擇性更新角色
     *
     * @param row 更新資料
     * @param example 更新條件
     * @return 影響筆數
     */
    int updateByExampleSelective(@Param("row") Role row,
                                 @Param("example") RoleExample example);

    /**
     * 依條件更新角色
     *
     * @param row 更新資料
     * @param example 更新條件
     * @return 影響筆數
     */
    int updateByExample(@Param("row") Role row,
                        @Param("example") RoleExample example);

    /**
     * 依主鍵選擇性更新角色
     *
     * @param row 角色資料
     * @return 影響筆數
     */
    int updateByPrimaryKeySelective(Role row);

    /**
     * 依主鍵更新角色
     *
     * @param row 角色資料
     * @return 影響筆數
     */
    int updateByPrimaryKey(Role row);
}