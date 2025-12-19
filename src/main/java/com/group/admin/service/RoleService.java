package com.group.admin.service;

import com.group.admin.req.role.RoleCreateReq;
import com.group.admin.req.role.RoleMenuPermissionReq;
import com.group.admin.req.role.RoleUpdateReq;
import com.group.admin.res.role.RoleDetailRes;
import com.group.admin.res.role.RoleRes;

import java.util.List;

/**
 * 角色服務介面
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface RoleService {

    /**
     * 建立角色
     *
     * @param req 建立請求
     * @return 建立後的角色資料
     */
    RoleRes createRole(RoleCreateReq req);

    /**
     * 更新角色
     *
     * @param req 更新請求
     * @return 更新後的角色資料
     */
    RoleRes updateRole(RoleUpdateReq req);

    /**
     * 刪除角色
     *
     * @param id 角色ID (UUID)
     */
    void deleteRole(String id);

    /**
     * 根據ID查詢角色
     *
     * @param id 角色ID (UUID)
     * @return 角色資料
     */
    RoleRes getRoleById(String id);

    /**
     * 根據ID查詢角色詳情（包含權限）
     *
     * @param id 角色ID (UUID)
     * @return 角色詳情
     */
    RoleDetailRes getRoleDetailById(String id);

    /**
     * 查詢所有角色
     *
     * @return 角色列表
     */
    List<RoleRes> getAllRoles();

    /**
     * 設定角色的選單權限
     *
     * @param req 權限設定請求
     */
    void setRoleMenuPermissions(RoleMenuPermissionReq req);

    /**
     * 根據角色代碼查詢
     *
     * @param code 角色代碼
     * @return 角色資料
     */
    RoleRes getRoleByCode(String code);
}
