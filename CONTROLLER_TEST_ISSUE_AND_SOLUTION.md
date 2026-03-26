# Controller 測試問題與解決方案

## 問題分析

### 當前狀況
使用 `@WebMvcTest` 進行 Controller 測試時遇到以下錯誤：

```
Error creating bean with name 'jwtAuthenticationFilter'
Caused by: Error creating bean with name 'adminUserRoleMapper'
Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required
```

### 根本原因

**@WebMvcTest 的限制：**
1. **只載入 Web 層組件**：`@Controller`, `@RestController`, `@ControllerAdvice` 等
2. **不自動配置 MyBatis**：不會建立 `SqlSessionFactory`, `SqlSessionTemplate`
3. **Filter 依賴問題**：`JwtAuthenticationFilter` 需要 `AdminUserRoleMapper`，而該 Mapper 需要 MyBatis

**為什麼 Filter 會被載入：**
```java
@Import(SecurityConfig.class)  // ← SecurityConfig 註冊了 Filter
```

SecurityConfig 中註冊的 Filter 會被實例化，進而觸發依賴注入，但 MyBatis Mapper 未被配置。

## 解決方案比較

### 方案 1：Mock 所有 Mapper（不推薦）

```java
@WebMvcTest(AdminLotteryController.class)
@Import(SecurityConfig.class)
class AdminLotteryControllerTest {
    @MockBean private LotteryService lotteryService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AdminUserMapper adminUserMapper;
    @MockBean private AdminUserRoleMapper adminUserRoleMapper;  // Filter 需要
    @MockBean private RoleMapper roleMapper;                     // Filter 需要
    @MockBean private StoreUserMapper storeUserMapper;
    @MockBean private UserMapper userMapper;
    // ... 需要 mock 很多 Mapper
}
```

**缺點：**
- 需要 mock 大量 Mapper
- 每個測試都要重複配置
- 維護成本高

### 方案 2：使用 @SpringBootTest（推薦）

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminLotteryControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private LotteryService lotteryService;  // 只 mock Service
}
```

**優點：**
- 載入完整 Spring 環境
- 自動配置 MyBatis
- Filter 正常工作
- 只需 mock Service 層

**缺點：**
- 啟動稍慢（約 3-5 秒）
- 需要測試用資料庫配置

### 方案 3：排除 SecurityConfig（不推薦）

```java
@WebMvcTest(AdminLotteryController.class)
// 不 @Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminLotteryControllerTest {
    // ...
}
```

**缺點：**
- 無法測試真實的權限控制邏輯
- 無法測試 JWT 驗證
- 測試不完整

## 最終採用方案

### 採用：@SpringBootTest + @AutoConfigureMockMvc + 測試配置檔

#### 1. 建立測試配置檔

**src/test/resources/application-test.yml:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  h2:
    console:
      enabled: true
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

jwt:
  secret: test-secret-key-for-testing-purposes-only
  expiration: 3600000
```

#### 2. 更新測試類別模板

```java
package com.group.admin.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.req.lottery.*;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("後台商品管理 API 測試")
class AdminLotteryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LotteryService lotteryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin@kuji.com", roles = "ADMIN")
    @DisplayName("查詢商品列表 - 管理員可看所有商品")
    void queryLotteries_ShouldReturnAllLotteries_WhenUserIsAdmin() throws Exception {
        // Given
        List<LotteryRes> mockList = Arrays.asList(new LotteryRes(), new LotteryRes());
        when(lotteryService.queryLotteries(any())).thenReturn(mockList);

        // When & Then
        mockMvc.perform(post("/admin/lottery/list")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(lotteryService, times(1)).queryLotteries(any());
    }
}
```

## 執行步驟

### 1. 添加 H2 測試依賴（pom.xml）

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. 建立測試配置檔

建立 `src/test/resources/application-test.yml`（如上）

### 3. 更新所有測試類別

將 `@WebMvcTest` 改為 `@SpringBootTest` + `@AutoConfigureMockMvc`

### 4. 執行測試

```bash
mvn clean test -Dtest=*ControllerTest
```

## 測試策略總結

| 測試類型 | 使用註解 | Mock 範圍 | 適用場景 |
|---------|---------|-----------|---------|
| 單元測試 | @WebMvcTest | Mock 所有依賴 | 純 Controller 邏輯 |
| 整合測試 | @SpringBootTest | 只 Mock Service | 包含 Filter/Security |
| E2E 測試 | @SpringBootTest | 不 Mock | 完整流程測試 |

**本專案採用：整合測試（@SpringBootTest）**
- 原因：需要測試 JWT Filter、Security Config、權限控制
- Mock 策略：只 Mock Service 層，其他使用真實 Bean

## 測試覆蓋率目標

- Line Coverage: >80%
- Branch Coverage: >70%
- Method Coverage: >90%

## 參考資料

- [Spring Boot Testing Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [MockMvc vs WebTestClient](https://spring.io/guides/gs/testing-web/)
- [Testing Spring Security](https://docs.spring.io/spring-security/reference/servlet/test/method.html)
