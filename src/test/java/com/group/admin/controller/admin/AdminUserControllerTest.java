package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.req.admin.AdminUserCondition;
import com.group.admin.req.admin.ChangePasswordReq;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.admin.AdminUserRes;
import com.group.admin.security.UserPrincipal;
import com.group.admin.service.AdminUserService;
import com.group.admin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("後台帳號管理 API 測試")
class AdminUserControllerTest extends BaseControllerTest {

	@Mock
	private AdminUserService adminUserService;

	@Mock
	private UserService userService;

	@InjectMocks
	private AdminUserController adminUserController;

	private static final String OPERATOR_ID = "admin-001";
	private static final String OWNER_ID = "owner-001";

	@BeforeEach
	void setUp() {
		setupMockMvcWithExceptionHandler(adminUserController);
	}

	@Test
	@DisplayName("取得本人資料 - 成功")
	void getMyProfile_ShouldReturnCurrentUserProfile() throws Exception {
		setupAuthentication(OPERATOR_ID, "ADMIN");

		AdminUserRes profile = sampleUser(OPERATOR_ID, "系統管理員");
		when(adminUserService.getMyProfile(OPERATOR_ID)).thenReturn(profile);

		mockMvc.perform(get("/admin/users/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(OPERATOR_ID))
				.andExpect(jsonPath("$.displayName").value("系統管理員"));

		verify(adminUserService).getMyProfile(OPERATOR_ID);
	}

	@Test
	@DisplayName("更新本人資料 - 應使用目前登入者 ID")
	void updateMyProfile_ShouldUseCurrentOperatorId() throws Exception {
		setupAuthentication(OPERATOR_ID, "STORE_EDITOR");

		AdminUserRes updated = sampleUser(OPERATOR_ID, "新名字");
		when(adminUserService.updateMyProfile(eq(OPERATOR_ID), any())).thenReturn(updated);

		mockMvc.perform(put("/admin/users/me")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  \"displayName\": \"新名字\",
								  \"phone\": \"0911111111\"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("新名字"));

		verify(adminUserService).updateMyProfile(eq(OPERATOR_ID), any());
	}

	@Test
	@DisplayName("本人修改密碼 - 應固定以本人身分執行")
	void changeMyPassword_ShouldUseCurrentOperatorIdAsTargetAndOperator() throws Exception {
		setupAuthentication(OPERATOR_ID, "STORE_OWNER");

		mockMvc.perform(post("/admin/users/me/change-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  \"currentPassword\": \"oldPassword1\",
								  \"newPassword\": \"newPassword1\"
								}
								"""))
				.andExpect(status().isOk());

		verify(adminUserService).changePassword(eq(OPERATOR_ID), any(ChangePasswordReq.class), eq(OPERATOR_ID));
	}

	@Test
	@DisplayName("店家主帳號查詢小編 - 未綁店家時回空列表")
	void queryAdminUsers_ShouldReturnEmpty_WhenStoreOwnerWithoutStoreBinding() throws Exception {
		setupAuthentication(OWNER_ID, "STORE_OWNER");

		mockMvc.perform(post("/admin/users/list")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));

		verify(adminUserService, never()).queryAdminUsers(any());
	}

	@Test
	@DisplayName("店家主帳號查詢小編 - 自動帶入 storeId 與角色")
	void queryAdminUsers_ShouldInjectStoreScope_WhenStoreOwnerWithStoreBinding() throws Exception {
		setupStoreOwnerAuthentication(OWNER_ID, "store-001");

		when(adminUserService.queryAdminUsers(any())).thenReturn(List.of(sampleUser("editor-001", "店家小編")));

		mockMvc.perform(post("/admin/users/list")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("editor-001"));

		ArgumentCaptor<QueryReq> captor = ArgumentCaptor.forClass(QueryReq.class);
		verify(adminUserService).queryAdminUsers(captor.capture());

		@SuppressWarnings("unchecked")
		QueryReq<AdminUserCondition> actualReq = captor.getValue();
		assertNotNull(actualReq.getCondition());
		assertEquals("store-001", actualReq.getCondition().getStoreId());
		assertEquals("ROLE_STORE_EDITOR", actualReq.getCondition().getRoleCode());
	}

	@Test
	@DisplayName("重設密碼 - 應帶入操作者 ID")
	void resetPassword_ShouldPassOperatorId() throws Exception {
		setupAuthentication(OPERATOR_ID, "ADMIN");
		when(adminUserService.resetPassword("target-001", OPERATOR_ID)).thenReturn("Temp1234");

		mockMvc.perform(post("/admin/users/target-001/reset-password"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.newPassword").value("Temp1234"));

		verify(adminUserService).resetPassword("target-001", OPERATOR_ID);
	}

	private AdminUserRes sampleUser(String id, String displayName) {
		AdminUserRes res = new AdminUserRes();
		res.setId(id);
		res.setDisplayName(displayName);
		res.setEmail(id + "@kuji.com");
		res.setUsername(id + "@kuji.com");
		return res;
	}

	private void setupStoreOwnerAuthentication(String userId, String storeId) {
		UserPrincipal principal = UserPrincipal.builder()
				.userId(userId)
				.username("owner@kuji.com")
				.roles(List.of("STORE_OWNER"))
				.storeIds(List.of(storeId))
				.build();

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_STORE_OWNER"))
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
