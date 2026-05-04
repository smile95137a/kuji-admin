package com.group.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.entity.AdminOperationLog;
import com.group.admin.entity.Menu;
import com.group.admin.entity.PermissionAuditLog;
import com.group.admin.entity.Role;
import com.group.admin.entity.RoleMenu;
import com.group.admin.example.MenuExample;
import com.group.admin.example.RoleExample;
import com.group.admin.example.RoleMenuExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.exception.UnprocessableEntityException;
import com.group.admin.mapper.AdminOperationLogMapper;
import com.group.admin.mapper.MenuMapper;
import com.group.admin.mapper.PermissionAuditLogMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.RoleMenuMapper;
import com.group.admin.req.UpdateRolePermissionsReq;
import com.group.admin.req.role.RoleCreateReq;
import com.group.admin.req.role.RoleMenuPermissionReq;
import com.group.admin.req.role.RoleUpdateReq;
import com.group.admin.res.RoleWithPermissionsRes;
import com.group.admin.res.role.RoleDetailRes;
import com.group.admin.res.role.RoleRes;
import com.group.admin.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服務實作
 * 
 * <p>使用 Example 類別進行動態查詢，遵循 MBG 最佳實踐</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final PermissionAuditLogMapper auditLogMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RoleRes createRole(RoleCreateReq req) {
        log.info("建立角色: {}", req.getName());

        // 使用 Example 檢查代碼是否重複
        RoleExample example = new RoleExample();
        example.createCriteria().andCodeEqualTo(req.getCode());
        List<Role> existingRoles = roleMapper.selectByExample(example);
        if (!existingRoles.isEmpty()) {
            throw new BusinessException("角色代碼已存在: " + req.getCode());
        }

        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setName(req.getName());
        role.setCode(req.getCode());
        role.setDescription(req.getDescription());
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());

        roleMapper.insert(role);
        log.info("角色建立成功: id={}", role.getId());

        return convertToRes(role);
    }

    @Override
    @Transactional
    public RoleRes updateRole(RoleUpdateReq req) {
        log.info("更新角色: id={}", req.getId());

        Role role = roleMapper.selectByPrimaryKey(req.getId());
        if (role == null) {
            throw new BusinessException("角色不存在: " + req.getId());
        }

        // ⚠️ 角色代碼為系統識別碼，禁止修改
        if (req.getName() != null) {
            role.setName(req.getName());
        }
        if (req.getDescription() != null) {
            role.setDescription(req.getDescription());
        }
        role.setUpdatedAt(LocalDateTime.now());

        roleMapper.updateByPrimaryKey(role);
        log.info("角色更新成功: id={}", role.getId());

        return convertToRes(role);
    }

    @Override
    @Transactional
    public void deleteRole(String id) {
        log.info("刪除角色: id={}", id);

        Role role = roleMapper.selectByPrimaryKey(id);
        if (role == null) {
            throw new BusinessException("角色不存在: " + id);
        }

        // 系統預設角色不可刪除
        if ("ADMIN".equals(role.getCode()) || "STORE_OWNER".equals(role.getCode()) || "STORE_EDITOR".equals(role.getCode())) {
            throw new BusinessException("系統預設角色不可刪除: " + role.getCode());
        }

        // 使用 Example 刪除角色的所有選單權限
        RoleMenuExample roleMenuExample = new RoleMenuExample();
        roleMenuExample.createCriteria().andRoleIdEqualTo(id);
        roleMenuMapper.deleteByExample(roleMenuExample);
        
        // 刪除角色
        roleMapper.deleteByPrimaryKey(id);
        log.info("角色刪除成功: id={}", id);
    }

    @Override
    public RoleRes getRoleById(String id) {
        Role role = roleMapper.selectByPrimaryKey(id);
        if (role == null) {
            throw new BusinessException("角色不存在: " + id);
        }
        return convertToRes(role);
    }

    @Override
    public RoleDetailRes getRoleDetailById(String id) {
        Role role = roleMapper.selectByPrimaryKey(id);
        if (role == null) {
            throw new BusinessException("角色不存在: " + id);
        }

        RoleDetailRes res = new RoleDetailRes();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setCode(role.getCode());
        res.setDescription(role.getDescription());
        res.setCreatedAt(role.getCreatedAt());
        res.setUpdatedAt(role.getUpdatedAt());

        // 使用 Example 查詢角色的選單權限
        RoleMenuExample roleMenuExample = new RoleMenuExample();
        roleMenuExample.createCriteria().andRoleIdEqualTo(id);
        List<RoleMenu> roleMenus = roleMenuMapper.selectByExample(roleMenuExample);
        
        List<RoleDetailRes.MenuPermissionRes> permissions = new ArrayList<>();
        for (RoleMenu rm : roleMenus) {
            RoleDetailRes.MenuPermissionRes permission = new RoleDetailRes.MenuPermissionRes();
            permission.setMenuId(rm.getMenuId());
            permission.setCanView(rm.getCanView());
            permission.setCanEdit(rm.getCanEdit());
            permission.setCanDelete(rm.getCanDelete());

            // 取得選單資訊
            Menu menu = menuMapper.selectByPrimaryKey(rm.getMenuId());
            if (menu != null) {
                permission.setMenuName(menu.getName());
                permission.setMenuCode(menu.getCode());
            }

            permissions.add(permission);
        }
        res.setMenuPermissions(permissions);

        return res;
    }

    @Override
    public List<RoleRes> getAllRoles() {
        RoleExample example = new RoleExample();
        List<Role> roles = roleMapper.selectByExample(example);
        return roles.stream()
                .map(this::convertToRes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setRoleMenuPermissions(RoleMenuPermissionReq req) {
        log.info("設定角色選單權限: roleId={}", req.getRoleId());

        Role role = roleMapper.selectByPrimaryKey(req.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在: " + req.getRoleId());
        }

        // 使用 Example 刪除現有權限
        RoleMenuExample roleMenuExample = new RoleMenuExample();
        roleMenuExample.createCriteria().andRoleIdEqualTo(req.getRoleId());
        roleMenuMapper.deleteByExample(roleMenuExample);

        // 新增新權限
        if (req.getPermissions() != null && !req.getPermissions().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();

            for (RoleMenuPermissionReq.MenuPermission perm : req.getPermissions()) {
                // 驗證選單是否存在
                Menu menu = menuMapper.selectByPrimaryKey(perm.getMenuId());
                if (menu == null) {
                    throw new BusinessException("選單不存在: " + perm.getMenuId());
                }

                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setId(UUID.randomUUID().toString());
                roleMenu.setRoleId(req.getRoleId());
                roleMenu.setMenuId(perm.getMenuId());
                roleMenu.setCanView(perm.getCanView() != null ? perm.getCanView() : false);
                roleMenu.setCanEdit(perm.getCanEdit() != null ? perm.getCanEdit() : false);
                roleMenu.setCanDelete(perm.getCanDelete() != null ? perm.getCanDelete() : false);
                roleMenu.setCreatedAt(now);
                
                roleMenuMapper.insert(roleMenu);
            }
        }

        log.info("角色選單權限設定成功: roleId={}, permissions={}", req.getRoleId(), 
                req.getPermissions() != null ? req.getPermissions().size() : 0);
    }

    @Override
    public RoleRes getRoleByCode(String code) {
        RoleExample example = new RoleExample();
        example.createCriteria().andCodeEqualTo(code);
        List<Role> roles = roleMapper.selectByExample(example);
        if (roles.isEmpty()) {
            throw new BusinessException("角色不存在: " + code);
        }
        return convertToRes(roles.get(0));
    }

    /**
     * 轉換 Entity 為 Response DTO
     */
    private RoleRes convertToRes(Role role) {
        RoleRes res = new RoleRes();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setCode(role.getCode());
        res.setDescription(role.getDescription());
        res.setCreatedAt(role.getCreatedAt());
        res.setUpdatedAt(role.getUpdatedAt());
        return res;
    }

    // ===== Feature 009: RBAC Permissions =====

    @Override
    public List<Role> getAllRoleEntities() {
        RoleExample example = new RoleExample();
        example.setOrderByClause("id ASC");
        return roleMapper.selectByExample(example);
    }

    @Override
    public RoleWithPermissionsRes getRolePermissions(String roleId) {
        Role role = roleMapper.selectByPrimaryKey(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在: " + roleId);
        }

        MenuExample menuExample = new MenuExample();
        menuExample.createCriteria().andIsVisibleEqualTo(true);
        menuExample.setOrderByClause("order_num ASC");
        List<Menu> allMenus = menuMapper.selectByExample(menuExample);

        List<RoleMenu> roleMenus = roleMenuMapper.selectByRoleId(roleId);
        Map<String, RoleMenu> rmMap = roleMenus.stream()
                .collect(Collectors.toMap(RoleMenu::getMenuId, rm -> rm, (a, b) -> a));

        List<RoleWithPermissionsRes.MenuPermissionItem> items = allMenus.stream()
                .map(menu -> {
                    RoleMenu rm = rmMap.get(menu.getId());
                    return RoleWithPermissionsRes.MenuPermissionItem.builder()
                            .menuId(menu.getId())
                            .menuName(menu.getName())
                            .menuCode(menu.getCode())
                            .canView(rm != null ? rm.getCanView() : false)
                            .canEdit(rm != null ? rm.getCanEdit() : false)
                            .canDelete(rm != null ? rm.getCanDelete() : false)
                            .build();
                })
                .collect(Collectors.toList());

        return RoleWithPermissionsRes.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .description(role.getDescription())
                .menuPermissions(items)
                .build();
    }

    @Override
    @Transactional
    public RoleWithPermissionsRes updateRolePermissions(String roleId, UpdateRolePermissionsReq req, String operatorId) {
        log.info("🔐 更新角色權限: roleId={}, operatorId={}", roleId, operatorId);

        Role role = roleMapper.selectByPrimaryKey(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在: " + roleId);
        }

        // 驗證所有 menuId 存在
        for (UpdateRolePermissionsReq.MenuPermissionItem item : req.getMenuPermissions()) {
            Menu menu = menuMapper.selectByPrimaryKey(item.getMenuId());
            if (menu == null) {
                throw new BusinessException("選單不存在: " + item.getMenuId());
            }
        }

        // 驗證：canEdit=true 時 canView 必須為 true；canDelete=true 時 canView 必須為 true
        validateViewRequiredForEditDelete(req);

        // 如果是 ROLE_STORE_EDITOR，驗證權限是 ROLE_STORE_OWNER 的子集
        if ("ROLE_STORE_EDITOR".equals(role.getCode())) {
            validateEditorPermissionsSubset(req);
        }

        // 快照：變更前
        String beforeSnapshot = snapshotPermissions(roleId);

        // Delete-then-insert
        roleMenuMapper.deleteByRoleId(roleId);

        List<RoleMenu> newRoleMenus = req.getMenuPermissions().stream()
                .map(item -> {
                    RoleMenu rm = new RoleMenu();
                    rm.setId(UUID.randomUUID().toString());
                    rm.setRoleId(roleId);
                    rm.setMenuId(item.getMenuId());
                    rm.setCanView(Boolean.TRUE.equals(item.getCanView()));
                    rm.setCanEdit(Boolean.TRUE.equals(item.getCanEdit()));
                    rm.setCanDelete(Boolean.TRUE.equals(item.getCanDelete()));
                    rm.setCreatedAt(LocalDateTime.now());
                    return rm;
                })
                .collect(Collectors.toList());

        if (!newRoleMenus.isEmpty()) {
            roleMenuMapper.batchInsert(newRoleMenus);
        }

        // 快照：變更後
        String afterSnapshot = snapshotPermissions(roleId);

        // 審計日誌 — permission_audit_log（專用）
        PermissionAuditLog auditLog = PermissionAuditLog.builder()
                .id(UUID.randomUUID().toString())
                .operatorId(operatorId)
                .targetRoleId(roleId)
                .action("UPDATE_PERMISSIONS")
                .beforeSnapshot(beforeSnapshot)
                .afterSnapshot(afterSnapshot)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogMapper.insert(auditLog);

        // 審計日誌 — admin_operation_log（通用）
        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(
                    java.util.Map.of("before", beforeSnapshot, "after", afterSnapshot));
        } catch (JsonProcessingException e) {
            contentJson = "{\"before\":[],\"after\":[]}";
        }
        AdminOperationLog opLog = new AdminOperationLog();
        opLog.setId(UUID.randomUUID().toString());
        opLog.setAdminId(operatorId);
        opLog.setOperationType("UPDATE_ROLE_PERMISSIONS");
        opLog.setTargetType("ROLE");
        opLog.setTargetId(roleId);
        opLog.setDescription(contentJson);
        opLog.setCreatedAt(LocalDateTime.now());
        adminOperationLogMapper.insert(opLog);

        log.info("✅ 角色權限更新成功: roleId={}, permissions={}", roleId, newRoleMenus.size());

        return getRolePermissions(roleId);
    }

    private void validateViewRequiredForEditDelete(UpdateRolePermissionsReq req) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (UpdateRolePermissionsReq.MenuPermissionItem item : req.getMenuPermissions()) {
            if (Boolean.TRUE.equals(item.getCanEdit()) && !Boolean.TRUE.equals(item.getCanView())) {
                errors.add(Map.of("menuId", item.getMenuId(), "field", "canEdit",
                        "message", "canEdit=true requires canView=true"));
            }
            if (Boolean.TRUE.equals(item.getCanDelete()) && !Boolean.TRUE.equals(item.getCanView())) {
                errors.add(Map.of("menuId", item.getMenuId(), "field", "canDelete",
                        "message", "canDelete=true requires canView=true"));
            }
        }
        if (!errors.isEmpty()) {
            throw new UnprocessableEntityException("Validation failed", errors);
        }
    }

    private void validateEditorPermissionsSubset(UpdateRolePermissionsReq req) {
        RoleExample ownerExample = new RoleExample();
        ownerExample.createCriteria().andCodeEqualTo("ROLE_STORE_OWNER");
        List<Role> ownerRoles = roleMapper.selectByExample(ownerExample);
        if (ownerRoles.isEmpty()) {
            return;
        }

        String ownerRoleId = ownerRoles.get(0).getId();
        List<RoleMenu> ownerPerms = roleMenuMapper.selectByRoleId(ownerRoleId);
        Map<String, RoleMenu> ownerMap = ownerPerms.stream()
                .collect(Collectors.toMap(RoleMenu::getMenuId, rm -> rm, (a, b) -> a));

        List<Map<String, String>> errors = new ArrayList<>();
        for (UpdateRolePermissionsReq.MenuPermissionItem item : req.getMenuPermissions()) {
            RoleMenu ownerPerm = ownerMap.get(item.getMenuId());
            if (ownerPerm == null) {
                if (Boolean.TRUE.equals(item.getCanView()) || Boolean.TRUE.equals(item.getCanEdit()) || Boolean.TRUE.equals(item.getCanDelete())) {
                    errors.add(Map.of("menuId", item.getMenuId(), "field", "canView",
                            "message", "StoreEditor cannot have access to a menu that StoreOwner cannot access"));
                }
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanView()) && !Boolean.TRUE.equals(ownerPerm.getCanView())) {
                errors.add(Map.of("menuId", item.getMenuId(), "field", "canView",
                        "message", "StoreEditor cannot have canView=true when StoreOwner has canView=false for this menu"));
            }
            if (Boolean.TRUE.equals(item.getCanEdit()) && !Boolean.TRUE.equals(ownerPerm.getCanEdit())) {
                errors.add(Map.of("menuId", item.getMenuId(), "field", "canEdit",
                        "message", "StoreEditor cannot have canEdit=true when StoreOwner has canEdit=false for this menu"));
            }
            if (Boolean.TRUE.equals(item.getCanDelete()) && !Boolean.TRUE.equals(ownerPerm.getCanDelete())) {
                errors.add(Map.of("menuId", item.getMenuId(), "field", "canDelete",
                        "message", "StoreEditor cannot have canDelete=true when StoreOwner has canDelete=false for this menu"));
            }
        }
        if (!errors.isEmpty()) {
            throw new UnprocessableEntityException("Validation failed", errors);
        }
    }

    private String snapshotPermissions(String roleId) {
        List<RoleMenu> perms = roleMenuMapper.selectByRoleId(roleId);
        try {
            return objectMapper.writeValueAsString(perms);
        } catch (JsonProcessingException e) {
            log.warn("無法序列化權限快照: {}", e.getMessage());
            return "[]";
        }
    }
}
