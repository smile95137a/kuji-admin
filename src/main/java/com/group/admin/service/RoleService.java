package com.group.admin.service;

import com.group.admin.entity.Role;
import com.group.admin.req.UpdateRolePermissionsReq;
import com.group.admin.req.role.RoleCreateReq;
import com.group.admin.req.role.RoleMenuPermissionReq;
import com.group.admin.req.role.RoleUpdateReq;
import com.group.admin.res.RoleWithPermissionsRes;
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

    RoleRes createRole(RoleCreateReq req);

    RoleRes updateRole(RoleUpdateReq req);

    void deleteRole(String id);

    RoleRes getRoleById(String id);

    RoleDetailRes getRoleDetailById(String id);

    List<RoleRes> getAllRoles();

    void setRoleMenuPermissions(RoleMenuPermissionReq req);

    RoleRes getRoleByCode(String code);

    // ===== Feature 009: RBAC Permissions =====

    List<Role> getAllRoleEntities();

    RoleWithPermissionsRes getRolePermissions(String roleId);

    RoleWithPermissionsRes updateRolePermissions(String roleId, UpdateRolePermissionsReq req, String operatorId);
}
