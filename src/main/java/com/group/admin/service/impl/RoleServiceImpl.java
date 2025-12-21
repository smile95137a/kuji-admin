package com.group.admin.service.impl;

import com.group.admin.entity.Menu;
import com.group.admin.entity.Role;
import com.group.admin.entity.RoleMenu;
import com.group.admin.example.RoleExample;
import com.group.admin.example.RoleMenuExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.MenuMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.RoleMenuMapper;
import com.group.admin.req.role.RoleCreateReq;
import com.group.admin.req.role.RoleMenuPermissionReq;
import com.group.admin.req.role.RoleUpdateReq;
import com.group.admin.res.role.RoleDetailRes;
import com.group.admin.res.role.RoleRes;
import com.group.admin.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

        // 如果修改了代碼，檢查是否重複
        if (req.getCode() != null && !req.getCode().equals(role.getCode())) {
            RoleExample checkExample = new RoleExample();
            checkExample.createCriteria().andCodeEqualTo(req.getCode());
            List<Role> existingRoles = roleMapper.selectByExample(checkExample);
            if (!existingRoles.isEmpty()) {
                throw new BusinessException("角色代碼已存在: " + req.getCode());
            }
            role.setCode(req.getCode());
        }

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
}
