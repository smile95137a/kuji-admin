# 研究報告：店家管理 (Store Management)

**Feature**: `014-store-management`  
**Phase**: 0 — 研究  
**Date**: 2026-03-22  

---

## 1. 原子性店家 + 負責人帳號建立

### 決策
將店家列新增**與** `AdminUser` + 角色指派列新增，包裝在單一 `@Transactional` 服務方法中。Spring 的 `PlatformTransactionManager` 會向 MySQL 發出單一 `BEGIN / COMMIT`；任何未捕獲的例外均會觸發自動 `ROLLBACK`。

### 原因說明
- 規格要求「任何失敗時不得留下孤立的店家或帳號」（AC-1.2）。
- 現有程式庫已對多步驟寫入使用 `@Transactional`（例如 `LotteryServiceImpl.deleteLottery`）。
- MySQL 8.3 在 InnoDB 上支援完整 ACID 交易（所有專案資料表均使用 InnoDB）。

### 實作模式

```java
// StoreServiceImpl.java
@Transactional(rollbackFor = Exception.class)
public StoreRes createStore(CreateStoreReq req, String operatorId) {
    // 1. Build + insert Store
    Store store = buildStore(req, operatorId);
    storeMapper.insert(store);

    // 2. Build + insert AdminUser (owner account)
    AdminUser owner = buildOwnerAccount(req.getOwner(), store.getId(), operatorId);
    adminUserMapper.insert(owner);

    // 3. Assign ROLE_STORE_OWNER
    AdminUserRole role = new AdminUserRole(owner.getId(), RoleCode.ROLE_STORE_OWNER.getCode());
    adminUserRoleMapper.insert(role);

    // 4. Return combined response
    return toStoreRes(store, owner);
}
```

- **失敗情境**：帳號名稱重複 → `DataIntegrityViolationException` → 回滾；S3 上傳失敗（在 DB 寫入前）→ 在交易開始前拋出例外；圖片上傳在交易**開始前**完成，以縮短交易持有時間。
- **無需補償邏輯**：MySQL 回滾會自動移除兩筆資料列。

### 已考慮的替代方案
| 替代方案 | 拒絕原因 |
|-------------|-----------------|
| 兩個獨立 API 呼叫（先建立店家，再建立使用者） | 違反 FR-002；部分失敗時系統會處於不一致狀態 |
| Saga / 事件驅動補償 | 對單一資料庫的 monolith 來說過度設計；增加延遲但無實際效益 |
| 預存程序 | 繞過 Spring Security 上下文與稽核機制；難以測試 |

---

## 2. 連鎖停用 — 批次更新模式

### 決策
執行 `PUT /admin/stores/{id}/status`（status=DISABLED）時：

1. 設定 `store.status = DISABLED`。
2. 批次設定該店家所有**商品**（`lottery.status = OFF_SHELF`），使用 MyBatis `updateByExample`。
3. 批次設定該店家所有**橫幅**（`news_banner.status = DISABLED`），使用 MyBatis `updateByExample`。
4. 三個寫入操作均在單一 `@Transactional` 方法中執行。

### 原因說明
- 本專案對所有批次查詢／更新使用 **MyBatis Example** 模式（`LotteryExample`、`StoreExample`、`NewsBannerExample`）。以 `store_id` 條件篩選的 `updateByExampleSelective` 是阻力最小的做法。
- 單一 `UPDATE lottery SET status='OFF_SHELF' WHERE store_id=?` 遠比抓取後逐筆更新更有效率（無 N+1 問題）。
- SC-002 要求整個連鎖操作在 < 10 秒內完成。在已建立索引的 `store_id` 欄位上執行批次 `UPDATE`，即使有數千筆資料也可在毫秒內完成。

### 實作模式

```java
@Transactional(rollbackFor = Exception.class)
public void disableStore(String storeId, String operatorId) {
    // 1. Disable the store
    Store store = storeMapper.selectByPrimaryKey(storeId);
    if (store == null) throw new BusinessException("店家不存在");
    store.setStatus(StoreStatusEnum.DISABLED.getCode());
    store.setUpdatedAt(LocalDateTime.now());
    store.setUpdatedBy(operatorId);
    storeMapper.updateByPrimaryKeySelective(store);

    // 2. Batch-set all products to OFF_SHELF
    Lottery lotterySetter = new Lottery();
    lotterySetter.setStatus(LotteryStatusEnum.OFF_SHELF.getCode());
    lotterySetter.setUpdatedAt(LocalDateTime.now());
    LotteryExample lotteryFilter = new LotteryExample();
    lotteryFilter.createCriteria().andStoreIdEqualTo(storeId);
    lotteryMapper.updateByExampleSelective(lotterySetter, lotteryFilter);

    // 3. Batch-set all banners to DISABLED
    NewsBanner bannerSetter = new NewsBanner();
    bannerSetter.setStatus("DISABLED");
    bannerSetter.setUpdatedAt(LocalDateTime.now());
    NewsBannerExample bannerFilter = new NewsBannerExample();
    bannerFilter.createCriteria().andStoreIdEqualTo(storeId);
    newsBannerMapper.updateByExampleSelective(bannerSetter, bannerFilter);

    log.info("🔴 店家停用完成（含連鎖效應）: storeId={}", storeId);
}
```

### 重新啟用行為
`enableStore()` 僅設定 `store.status = ENABLED`。商品和橫幅維持現有狀態。此為 FR-005 的預期行為 — 店家負責人必須手動重新上架每個商品／橫幅。

### 已考慮的替代方案
| 替代方案 | 拒絕原因 |
|-------------|-----------------|
| 抓取所有商品 ID → 逐筆更新 | N+1 寫入；不符合 SC-002 效能需求 |
| 在 `store.status` 上設定資料庫觸發器 | 繞過應用層稽核日誌；在 RDS 上難以測試和部署 |
| 商品軟性旗標（`store_disabled_override`） | 增加第二個狀態維度；使所有商品查詢更複雜 |

---

## 3. 依角色限制編輯 — 管理員 vs 店家負責人

### 決策
單一 `PUT /admin/stores/{id}` 端點接收 `UpdateStoreReq`。**服務層**依呼叫者角色強制執行欄位層級限制：

- `ADMIN`：可更新**所有**欄位，包括 `ownerId`（未來轉移功能）、狀態等。
- `STORE_OWNER`：只能更新 `storeName`、`shortDescription`、`longDescription`、`logoUrl`、`coverImageUrl`、`address`、`businessHours`、`facebookUrl`、`instagramUrl`、`lineId`、`email`、`phone`。`ownerId` 欄位**靜默忽略**（非錯誤 — 冪等處理）。

此外，服務層驗證 `STORE_OWNER` 呼叫者的 `storeId`（來自 JWT 或 `StoreUser` 對應）與目標 `{id}` 相符。跨店家存取返回 `403 Forbidden`（AC-3.3）。

### 實作模式

```java
// Service
if (!SecurityUtils.isAdmin()) {
    // a. Ownership check
    if (!storeUserService.ownsStore(userId, storeId)) {
        throw new AccessDeniedException("無權限編輯此店家");
    }
    // b. Strip admin-only fields (ownerId unchanged)
    req.setOwnerId(null);
}
// Apply all non-null fields from req
BeanUtils.copyProperties(req, store, getNullPropertyNames(req));
```

### 已考慮的替代方案
| 替代方案 | 拒絕原因 |
|-------------|-----------------|
| 兩個獨立端點（`/admin/stores/{id}` vs `/owner/stores/{id}`） | 維護面加倍；現有程式庫使用單一端點 + 角色檢查 |
| 前端對店家負責人隱藏 owner 欄位 | 縱深防禦要求伺服器端強制執行（AC-3.2） |

---

## 4. 公開店家端點 — 查詢策略

### 決策
- `GET /api/stores`：`SELECT ... FROM store WHERE status = 'ENABLED' ORDER BY created_at DESC`；僅返回 `(id, storeName, shortDescription, logoUrl)`。
- `GET /api/stores/{id}`：JOIN 或第二次查詢，取得 `Lottery` 列表 `WHERE store_id = ? AND status = 'ON_SHELF'`；返回完整店家資訊 + 商品列表。

兩個端點均為**不需認證**（API 安全鏈中設定 `permitAll()`）。

### 原因說明
- 這些為唯讀端點，不含敏感資料。公開存取可支援 SEO 友善用途及行動應用程式，無需管理 Token。
- 列表使用輕量的 `StoreListItemRes` DTO，避免不必要地傳輸 BLOB `longDescription`（節省頻寬＋提升安全性）。
- 列表與詳情使用不同 DTO，遵循本專案現有的 `Res` 分離模式。

### 效能考量
針對 SC-003（500 間店家 < 2 秒）：在 `status` 上的單一索引查詢可立即返回 ≤ 500 筆資料。v1 不需分頁，但回應合約保留 `page`/`size` 參數供未來使用。

---

## 5. 停用時的進行中訂單

### 決策
停用店家時，**不**取消現有訂單。僅封鎖新訂單建立（訂單建立服務會檢查 `store.status == ENABLED`）。規格邊界條件確認此行為（第 3 頁：「現有訂單保留並可繼續處理」）。

### 實作備注
本功能不需修改訂單服務。訂單服務應已在訂單建立時檢查店家狀態（或將來需加入）。此部分超出功能 014 範圍，此處記錄以避免混淆。

---

## 6. 停用時的進行中抽獎活動警告

### 決策
執行連鎖停用前，服務層查詢是否存在 `store_id = ? AND status = 'ON_SHELF'`（或 `ACTIVE`）的抽獎活動。若有，API 返回 **HTTP 409 Conflict**，附帶列出進行中抽獎活動數量的警告訊息。管理員需加上 `?force=true` 查詢參數才能強制執行。

### 原因說明
AC-4.3 規定：「系統警告有進行中活動並要求確認」。兩步驟確認（第一次呼叫返回警告，第二次帶 `?force=true` 實際執行）可避免新增獨立確認端點，同時保持無狀態性。

```
PUT /admin/stores/{id}/status
Body: { "status": "DISABLED" }

→ HTTP 409: { "code": "ACTIVE_LOTTERIES", "count": 5, "message": "請確認後再停用" }

PUT /admin/stores/{id}/status?force=true
Body: { "status": "DISABLED" }

→ HTTP 200: OK
```
