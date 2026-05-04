package com.group.admin.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.group.admin.annotation.AuditLog;
import com.group.admin.enums.AuditLogType;
import com.group.admin.req.auth.AdminLoginReq;
import com.group.admin.security.UserPrincipal;
import com.group.admin.service.AuditLogService;
import com.group.admin.util.AuditContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("AuditLogAspect 測試")
class AuditLogAspectTest {

    private AuditLogService auditLogService;
    private AuditLogAspect auditLogAspect;

    @BeforeEach
    void setUp() {
        auditLogService = mock(AuditLogService.class);
        auditLogAspect = new AuditLogAspect(
                auditLogService,
                new ObjectMapper().registerModule(new JavaTimeModule())
        );
    }

    @AfterEach
    void tearDown() {
        AuditContext.clear();
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("後台登入審計應優先寫入實際 email，不應落成 anonymousUser")
    void shouldLogResolvedEmailForAdminLogin() {
        setRequest("POST", "/admin/auth/login", "36.224.58.1", "Mozilla/5.0");

        LoginAuditTarget target = createProxy(new LoginAuditTarget());
        AdminLoginReq req = new AdminLoginReq();
        req.setUsername("admin-login-name");
        req.setPassword("secret");

        target.login(req);

        verify(auditLogService).logAuth(
                eq("admin-1"),
                eq("ADMIN"),
                eq("admin@kuji.com"),
                eq("EMAIL"),
                eq("SUCCESS"),
                isNull(),
                eq("36.224.58.1"),
                eq("Mozilla/5.0")
        );
    }

    @Test
    @DisplayName("後台操作審計應補 targetId、targetName 與前後快照")
    void shouldInferTargetAndSnapshotsForAdminAction() {
        setRequest("PUT", "/admin/stores/store-1", "127.0.0.1", "JUnit");
        setupAuthentication();

        AdminActionTarget target = createProxy(new AdminActionTarget());
        UpdateStoreCommand command = new UpdateStoreCommand("KUJI 一號店");

        target.updateStore("store-1", command);

        ArgumentCaptor<String> beforeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> afterCaptor = ArgumentCaptor.forClass(String.class);

        verify(auditLogService).logAdminAction(
                eq("admin-1"),
                eq("admin@kuji.com"),
                eq("ROLE_ADMIN"),
                eq("STORE"),
                eq("store-1"),
                eq("KUJI 一號店"),
                eq("UPDATE"),
                beforeCaptor.capture(),
                afterCaptor.capture(),
                eq("SUCCESS"),
                isNull(),
                eq("127.0.0.1")
        );

        String beforeSnapshot = beforeCaptor.getValue();
        String afterSnapshot = afterCaptor.getValue();
        assertNotNull(beforeSnapshot);
        assertNotNull(afterSnapshot);
        assertTrue(beforeSnapshot.contains("store-1"));
        assertTrue(beforeSnapshot.contains("KUJI 一號店"));
        assertTrue(afterSnapshot.contains("store-1"));
        assertTrue(afterSnapshot.contains("KUJI 一號店"));
    }

    private void setRequest(String method, String path, String remoteAddr, String userAgent) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(remoteAddr);
        request.addHeader("User-Agent", userAgent);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void setupAuthentication() {
        UserPrincipal principal = UserPrincipal.builder()
                .userId("admin-1")
                .username("admin-login-name")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .adminUser(new AdminProfile("admin@kuji.com"))
                .build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private <T> T createProxy(T target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(auditLogAspect);
        return factory.getProxy();
    }

    static class LoginAuditTarget {
        @AuditLog(type = AuditLogType.AUTH, action = "EMAIL")
        public void login(@RequestBody AdminLoginReq req) {
            AuditContext.setAuthAttemptedUsername(req.getUsername());
            AuditContext.setAuthResolvedUser("admin-1", "admin@kuji.com", "admin-login-name", "ADMIN");
        }
    }

    static class AdminActionTarget {
        @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "UPDATE", targetType = "STORE")
        public ResponseEntity<StoreResult> updateStore(@PathVariable("storeId") String storeId,
                                                       @RequestBody UpdateStoreCommand command) {
            return ResponseEntity.ok(new StoreResult(storeId, command.getStoreName()));
        }
    }

    static class UpdateStoreCommand {
        private final String storeName;

        UpdateStoreCommand(String storeName) {
            this.storeName = storeName;
        }

        public String getStoreName() {
            return storeName;
        }
    }

    static class StoreResult {
        private final String id;
        private final String storeName;

        StoreResult(String id, String storeName) {
            this.id = id;
            this.storeName = storeName;
        }

        public String getId() {
            return id;
        }

        public String getStoreName() {
            return storeName;
        }
    }

    static class AdminProfile {
        private final String email;

        AdminProfile(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }
}
