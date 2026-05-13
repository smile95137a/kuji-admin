package com.group.admin.service;

import com.group.admin.constants.ErrorCodes;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.admin.ChangePasswordReq;
import com.group.admin.req.admin.CreateStoreEditorReq;
import com.group.admin.req.admin.UpdateAdminUserReq;
import com.group.admin.service.impl.AdminUserServiceImpl;
import com.group.admin.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService 單元測試")
class AdminUserServiceImplTest {

    @Mock private AdminUserMapper adminUserMapper;
    @Mock private AdminUserRoleMapper adminUserRoleMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private StoreMapper storeMapper;
    @Mock private StoreUserMapper storeUserMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordUtil passwordUtil;
    @Mock private AdminAuthService adminAuthService;
    @Mock private EmailService emailService;

    @InjectMocks
    private AdminUserServiceImpl service;

    @Test
    @DisplayName("Admin 可重設密碼，並強制下次修改密碼")
    void resetPassword_WhenOperatorIsAdmin_ShouldUpdatePasswordAndSendEmail() {
        AdminUser target = adminUser("editor-001", "editor@kuji.com", true);
        Role adminRole = role("role-admin", "ROLE_ADMIN");
        AdminUserRole adminLink = adminUserRole("admin-001", adminRole.getId());

        when(adminUserMapper.selectByPrimaryKey("editor-001")).thenReturn(target);
        when(adminUserRoleMapper.selectByExample(any())).thenReturn(List.of(adminLink));
        when(roleMapper.selectByPrimaryKey(adminRole.getId())).thenReturn(adminRole);
        when(passwordUtil.generateRandomPassword()).thenReturn("TempPass123");
        when(passwordEncoder.encode("TempPass123")).thenReturn("encoded-temp-password");

        String newPassword = service.resetPassword("editor-001", "admin-001");

        assertThat(newPassword).isEqualTo("TempPass123");

        ArgumentCaptor<AdminUser> updateCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserMapper).updateByPrimaryKey(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getPassword()).isEqualTo("encoded-temp-password");
        assertThat(updateCaptor.getValue().getForceChangePassword()).isTrue();
        assertThat(updateCaptor.getValue().getUpdatedBy()).isEqualTo("admin-001");

        verify(emailService).sendInitialPasswordEmailSync("editor@kuji.com", target.getDisplayName(), "TempPass123");
    }

    @Test
    @DisplayName("店家主帳號不可重設其他店家小編密碼")
    void resetPassword_WhenStoreOwnerManagesEditorInOtherStore_ShouldThrowStoreAccessDenied() {
        AdminUser target = adminUser("editor-002", "editor2@kuji.com", false);
        Role ownerRole = role("role-owner", "ROLE_STORE_OWNER");
        Role editorRole = role("role-editor", "ROLE_STORE_EDITOR");
        Store ownerStore = store("store-001");
        Store targetStore = store("store-002");
        when(adminUserMapper.selectByPrimaryKey("editor-002")).thenReturn(target);

        when(adminUserRoleMapper.selectByExample(any())).thenReturn(
            List.of(adminUserRole("owner-001", ownerRole.getId())),
            List.of(adminUserRole("editor-002", editorRole.getId()))
        );
        when(roleMapper.selectByPrimaryKey(ownerRole.getId())).thenReturn(ownerRole);
        when(roleMapper.selectByPrimaryKey(editorRole.getId())).thenReturn(editorRole);

        when(storeUserMapper.selectByExample(any())).thenReturn(
            List.of(storeUser("owner-001", ownerStore.getId())),
            List.of(storeUser("editor-002", targetStore.getId()))
        );
        when(storeMapper.selectByPrimaryKey(ownerStore.getId())).thenReturn(ownerStore);
        when(storeMapper.selectByPrimaryKey(targetStore.getId())).thenReturn(targetStore);

        assertThatThrownBy(() -> service.resetPassword("editor-002", "owner-001"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.STORE_ACCESS_DENIED);

        verify(adminUserMapper, never()).updateByPrimaryKey(any());
        verify(emailService, never()).sendInitialPasswordEmailSync(any(), any(), any());
    }

    @Test
    @DisplayName("店家主帳號不可管理 Admin 帳號")
    void updateAdminUser_WhenStoreOwnerAttemptsToManageAdmin_ShouldThrowAccessDenied() {
        AdminUser target = adminUser("admin-002", "admin2@kuji.com", false);
        Role ownerRole = role("role-owner", "ROLE_STORE_OWNER");
        Role adminRole = role("role-admin", "ROLE_ADMIN");
        when(adminUserMapper.selectByPrimaryKey("admin-002")).thenReturn(target);

        when(adminUserRoleMapper.selectByExample(any())).thenReturn(
            List.of(adminUserRole("owner-001", ownerRole.getId())),
            List.of(adminUserRole("admin-002", adminRole.getId()))
        );
        when(roleMapper.selectByPrimaryKey(ownerRole.getId())).thenReturn(ownerRole);
        when(roleMapper.selectByPrimaryKey(adminRole.getId())).thenReturn(adminRole);

        UpdateAdminUserReq req = new UpdateAdminUserReq();
        req.setDisplayName("Should Fail");

        assertThatThrownBy(() -> service.updateAdminUser("admin-002", req, "owner-001"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.COMMON_ACCESS_DENIED);

        verify(adminUserMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    @DisplayName("只能修改自己的密碼")
    void changePassword_WhenOperatorDiffersFromTarget_ShouldThrowAccessDenied() {
        ChangePasswordReq req = new ChangePasswordReq();
        req.setCurrentPassword("OldPass123");
        req.setNewPassword("NewPass123");

        assertThatThrownBy(() -> service.changePassword("editor-001", req, "owner-001"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.COMMON_ACCESS_DENIED);

        verify(adminUserMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    @DisplayName("本人修改密碼成功後應清除 forceChangePassword")
    void changePassword_WhenCurrentPasswordMatches_ShouldUpdatePasswordAndClearForceFlag() {
        AdminUser user = adminUser("editor-001", "editor@kuji.com", true);
        user.setPassword("encoded-old-password");
        when(adminUserMapper.selectByPrimaryKey("editor-001")).thenReturn(user);
        when(passwordEncoder.matches("OldPass123", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("encoded-new-password");

        ChangePasswordReq req = new ChangePasswordReq();
        req.setCurrentPassword("OldPass123");
        req.setNewPassword("NewPass123");

        service.changePassword("editor-001", req, "editor-001");

        ArgumentCaptor<AdminUser> updateCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserMapper).updateByPrimaryKeySelective(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getPassword()).isEqualTo("encoded-new-password");
        assertThat(updateCaptor.getValue().getForceChangePassword()).isFalse();
        assertThat(updateCaptor.getValue().getUpdatedBy()).isEqualTo("editor-001");
    }

    @Test
    @DisplayName("建立店家小編應支援舊版 storeIds[0] 回填 storeId")
    void createStoreEditor_ShouldFallbackToFirstStoreId() {
        CreateStoreEditorReq req = new CreateStoreEditorReq();
        req.setEmail("editor@kuji.com");
        req.setDisplayName("店家小編");
        req.setStoreIds(List.of("store-001"));

        Role editorRole = role("role-editor", "ROLE_STORE_EDITOR");
        Store store = store("store-001");
        when(adminAuthService.getCurrentUserId()).thenReturn("admin-001");
        when(storeMapper.selectByPrimaryKey("store-001")).thenReturn(store);
        when(adminUserMapper.selectByExample(any())).thenReturn(List.of());
        when(passwordUtil.generateRandomPassword()).thenReturn("TempPass123");
        when(passwordEncoder.encode("TempPass123")).thenReturn("encoded-temp-password");
        when(roleMapper.selectByExample(any())).thenReturn(List.of(editorRole));

        service.createStoreEditor(req);

        ArgumentCaptor<StoreUser> storeUserCaptor = ArgumentCaptor.forClass(StoreUser.class);
        verify(storeUserMapper).insert(storeUserCaptor.capture());
        assertThat(storeUserCaptor.getValue().getStoreId()).isEqualTo("store-001");
        verify(emailService).sendInitialPasswordEmailSync("editor@kuji.com", "店家小編", "TempPass123");
    }

    @Test
    @DisplayName("更新小編可修改備註與綁定店家")
    void updateAdminUser_ShouldUpdateRemarkAndRebindStoreForEditor() {
        AdminUser target = adminUser("editor-003", "editor3@kuji.com", false);
        target.setRemark("old");
        Role adminRole = role("role-admin", "ROLE_ADMIN");
        Role editorRole = role("role-editor", "ROLE_STORE_EDITOR");
        StoreUser binding = storeUser("editor-003", "store-001");
        binding.setId("binding-001");
        Store newStore = store("store-002");

        when(adminUserMapper.selectByPrimaryKey("editor-003")).thenReturn(target);
        when(adminUserRoleMapper.selectByExample(any())).thenReturn(
                List.of(adminUserRole("admin-001", adminRole.getId())),
                List.of(adminUserRole("editor-003", editorRole.getId())),
                List.of(adminUserRole("editor-003", editorRole.getId())));
        when(roleMapper.selectByPrimaryKey(adminRole.getId())).thenReturn(adminRole);
        when(roleMapper.selectByPrimaryKey(editorRole.getId())).thenReturn(editorRole);
        when(storeMapper.selectByPrimaryKey("store-002")).thenReturn(newStore);
        when(storeUserMapper.selectByExample(any())).thenReturn(List.of(binding), List.of(binding));

        UpdateAdminUserReq req = new UpdateAdminUserReq();
        req.setPhone("0911222333");
        req.setRemark("new remark");
        req.setStoreId("store-002");

        service.updateAdminUser("editor-003", req, "admin-001");

        ArgumentCaptor<StoreUser> storeUserCaptor = ArgumentCaptor.forClass(StoreUser.class);
        verify(storeUserMapper).updateByPrimaryKey(storeUserCaptor.capture());
        assertThat(storeUserCaptor.getValue().getStoreId()).isEqualTo("store-002");

        ArgumentCaptor<AdminUser> userCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserMapper).updateByPrimaryKeySelective(userCaptor.capture());
        assertThat(userCaptor.getValue().getRemark()).isEqualTo("new remark");
        assertThat(userCaptor.getValue().getPhone()).isEqualTo("0911222333");
    }

    private AdminUserRole adminUserRole(String userId, String roleId) {
        AdminUserRole link = new AdminUserRole();
        link.setAdminUserId(userId);
        link.setRoleId(roleId);
        return link;
    }

    private StoreUser storeUser(String userId, String storeId) {
        StoreUser storeUser = new StoreUser();
        storeUser.setAdminUserId(userId);
        storeUser.setStoreId(storeId);
        return storeUser;
    }

    private AdminUser adminUser(String id, String email, boolean forceChangePassword) {
        AdminUser user = new AdminUser();
        user.setId(id);
        user.setUsername(email);
        user.setEmail(email);
        user.setDisplayName(id);
        user.setForceChangePassword(forceChangePassword);
        return user;
    }

    private Role role(String id, String code) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        return role;
    }

    private Store store(String id) {
        Store store = new Store();
        store.setId(id);
        store.setStoreName(id);
        return store;
    }
}
