package com.group.admin.service.impl;

import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Menu;
import com.group.admin.entity.Role;
import com.group.admin.entity.RoleMenu;
import com.group.admin.entity.StoreUser;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.example.MenuExample;
import com.group.admin.example.RoleMenuExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.MenuMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.RoleMenuMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 權限檢查服務實作
 * 
 * <p>使用 Example 類別進行動態查詢，遵循 MBG 最佳實踐</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STORE_OWNER = "STORE_OWNER";
    private static final String ROLE_STORE_EDITOR = "STORE_EDITOR";

    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final StoreUserMapper storeUserMapper;
    private final MenuMapper menuMapper;

    @Override
    public boolean canView(String adminUserId, String menuCode) {
        return checkPermission(adminUserId, menuCode, "view");
    }

    @Override
    public boolean canEdit(String adminUserId, String menuCode) {
        return checkPermission(adminUserId, menuCode, "edit");
    }

    @Override
    public boolean canDelete(String adminUserId, String menuCode) {
        return checkPermission(adminUserId, menuCode, "delete");
    }

    @Override
    public boolean hasRole(String adminUserId, String roleCode) {
        List<String> roleCodes = getUserRoleCodes(adminUserId);
        return roleCodes.contains(roleCode);
    }

    @Override
    public boolean isAdmin(String adminUserId) {
        return hasRole(adminUserId, ROLE_ADMIN);
    }

    @Override
    public boolean isStoreOwner(String adminUserId) {
        return hasRole(adminUserId, ROLE_STORE_OWNER);
    }

    @Override
    public boolean isStoreEditor(String adminUserId) {
        return hasRole(adminUserId, ROLE_STORE_EDITOR);
    }

    @Override
    public List<String> getUserRoleIds(String adminUserId) {
        // 使用 Example 查詢用戶的角色關聯
        AdminUserRoleExample example = new AdminUserRoleExample();
        example.createCriteria().andAdminUserIdEqualTo(adminUserId);
        List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(example);
        
        return userRoles.stream()
                .map(AdminUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserRoleCodes(String adminUserId) {
        List<String> roleIds = getUserRoleIds(adminUserId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 根據角色 ID 查詢角色代碼
        return roleIds.stream()
                .map(roleId -> {
                    Role role = roleMapper.selectByPrimaryKey(roleId);
                    return role != null ? role.getCode() : null;
                })
                .filter(code -> code != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAccessibleStoreIds(String adminUserId) {
        // Admin 可訪問所有店鋪
        if (isAdmin(adminUserId)) {
            return null; // null 表示全部
        }

        // 使用 Example 查詢用戶所屬的店鋪
        StoreUserExample example = new StoreUserExample();
        example.createCriteria().andAdminUserIdEqualTo(adminUserId);
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
        
        return storeUsers.stream()
                .map(StoreUser::getStoreId)
                .collect(Collectors.toList());
    }

    /**
     * 檢查權限
     */
    private boolean checkPermission(String adminUserId, String menuCode, String permissionType) {
        // Admin 擁有全部權限
        if (isAdmin(adminUserId)) {
            log.debug("用戶 {} 是 Admin，擁有全部權限", adminUserId);
            return true;
        }

        // 查詢用戶的角色 ID
        List<String> roleIds = getUserRoleIds(adminUserId);
        if (roleIds.isEmpty()) {
            log.debug("用戶 {} 沒有任何角色", adminUserId);
            return false;
        }

        // 先查詢選單 ID
        MenuExample menuExample = new MenuExample();
        menuExample.createCriteria().andCodeEqualTo(menuCode);
        List<Menu> menus = menuMapper.selectByExample(menuExample);
        if (menus.isEmpty()) {
            log.debug("找不到選單 code: {}", menuCode);
            return false;
        }
        String menuId = menus.get(0).getId();

        // 使用 Example 查詢角色對該選單的權限
        RoleMenuExample roleMenuExample = new RoleMenuExample();
        roleMenuExample.createCriteria()
                .andRoleIdIn(roleIds)
                .andMenuIdEqualTo(menuId);
        List<RoleMenu> permissions = roleMenuMapper.selectByExample(roleMenuExample);
        
        if (permissions.isEmpty()) {
            log.debug("用戶 {} 的角色對選單 {} 沒有任何權限配置", adminUserId, menuCode);
            return false;
        }

        // 檢查是否有對應權限（任一角色有權限即可）
        boolean hasPermission = permissions.stream().anyMatch(p -> {
            switch (permissionType) {
                case "view":
                    return Boolean.TRUE.equals(p.getCanView());
                case "edit":
                    return Boolean.TRUE.equals(p.getCanEdit());
                case "delete":
                    return Boolean.TRUE.equals(p.getCanDelete());
                default:
                    return false;
            }
        });

        log.debug("用戶 {} 對選單 {} 的 {} 權限: {}", adminUserId, menuCode, permissionType, hasPermission);
        return hasPermission;
    }
}
