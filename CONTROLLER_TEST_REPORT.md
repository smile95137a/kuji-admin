# Controller 單元測試執行報告

## 執行日期
2025-12-25

## 測試架構

### 測試模式
使用純 Mockito 單元測試模式（無需啟動 Spring Context），優點：
- 執行速度快（毫秒級）
- 不依賴資料庫或外部服務
- 隔離性佳，專注測試 Controller 邏輯

### 基礎類別
`BaseControllerTest` 提供：
- `MockMvc` 手動設定
- `ObjectMapper` JSON 序列化
- `setupMockMvc(controller)` 初始化方法

## 已建立的測試檔案

### Admin Controller 測試 (20 個)
| 測試類別 | 路徑 |
|----------|------|
| AdminAuthControllerTest | `/admin/auth/*` |
| AdminBannerControllerTest | `/admin/banner/*` |
| AdminFrontendUserControllerTest | `/admin/frontend-users/*` |
| AdminLotteryControllerTest | `/admin/lottery/*` |
| AdminLotteryWithPrizesControllerTest | `/admin/lottery-with-prizes/*` |
| AdminMarqueeControllerTest | `/admin/marquee/*` |
| AdminNewsControllerTest | `/admin/news/*` |
| AdminOrderControllerTest | `/admin/order/*` |
| AdminPrizeBoxControllerTest | `/admin/prize-box/*` |
| AdminRechargePlanControllerTest | `/admin/recharge-plan/*` |
| AdminReferralCodeControllerTest | `/admin/referral-codes/*` |
| AdminReportControllerTest | `/admin/report/*` |
| AdminStoreControllerTest | `/admin/stores/*` |
| AdminSystemLogControllerTest | `/admin/system-log/*` |
| AdminUserControllerTest | `/admin/users/*` |
| AdminWalletControllerTest | `/admin/wallet/*` |
| LotteryPrizeControllerTest | `/admin/lotteries/*/prizes` |
| MenuControllerTest | `/admin/menus/*` |
| RoleControllerTest | `/admin/roles/*` |
| UploadControllerTest | `/admin/upload` |

### API Controller 測試 (13 個)
| 測試類別 | 路徑 |
|----------|------|
| ApiAuthControllerTest | `/auth/*` |
| BannerControllerTest | `/banner` |
| DistrictControllerTest | `/district/*` |
| EnumControllerTest | `/enums/*` |
| LotteryBrowseControllerTest | `/lottery/browse/*` |
| LotteryDrawControllerTest | `/lottery/draw` |
| MarqueeControllerTest | `/marquee` |
| NewsControllerTest | `/news/*` |
| OrderControllerTest | `/api/order/*` |
| PrizeBoxControllerTest | `/api/prize-box/*` |
| RechargePlanControllerTest | `/api/recharge-plan` |
| StoreOptionControllerTest | `/stores/*` |
| UserControllerTest | `/user/*` |
| WalletControllerTest | `/api/wallet/*` |

## 測試模板

```java
package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.SomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("XXX API 測試")
class XxxControllerTest extends BaseControllerTest {

    @Mock
    private SomeService someService;

    @InjectMocks
    private XxxController xxxController;

    @BeforeEach
    void setUp() {
        setupMockMvc(xxxController);
    }

    @Test
    @DisplayName("測試案例")
    void someTest() throws Exception {
        // Given
        when(someService.someMethod(any())).thenReturn(someResult);
        
        // When & Then
        mockMvc.perform(get("/path"))
                .andExpect(status().isOk());
    }
}
```

## 執行測試命令

```bash
# 執行特定測試
mvn test -Dtest=AdminAuthControllerTest

# 執行所有 Admin Controller 測試
mvn test -Dtest="Admin*ControllerTest"

# 執行所有 Controller 測試
mvn test -Dtest="*ControllerTest"
```

## 注意事項

1. **路徑必須正確**：測試中的 API 路徑必須與 Controller `@RequestMapping` 完全一致
2. **必填欄位**：請求 Body 需包含所有 `@NotBlank`/`@NotNull` 欄位
3. **Service Mock**：使用 `@Mock` 注入 Service，在測試中設定預期行為
4. **Controller 注入**：使用 `@InjectMocks` 讓 Mockito 自動注入 Mock 物件
