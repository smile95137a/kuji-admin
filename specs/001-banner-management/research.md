# Research: 橫幅廣告管理 (Banner Management)

**功能**: `001-banner-management`  
**階段**: 0 — 調查與未知問題解決  
**日期**: 2026-03-22

---

## 1. 排程狀態轉換

### 決策

使用現有 `ScheduledTasks.java` bean 中的 Spring `@Scheduled` cron，每分鐘執行一次，查詢 `startTime` 已過但狀態仍為 `UNPUBLISHED` 的廣告，或 `endTime` 已過但狀態仍為 `PUBLISHED` 的廣告，並相應地翻轉狀態。

### 理由
- `@EnableScheduling` 已在 `AsyncConfig.java` 中配置——無需任何新基礎設施。
- 1 分鐘的執行週期滿足 SC-002（狀態在排程時間 1 分鐘內完成轉換）。
- 透過 MyBatis 批次 UPDATE 是每個方向一次 SQL 呼叫——對 ≤ 50 則廣告開銷極低。
- Quartz/DB 排程任務在此規模下帶來不必要的維運負擔。

### 已考慮的替代方案
| 選項 | 拒絕原因 |
|--------|-----------------|
| Quartz Scheduler | 需要額外相依套件 + job-store 資料表；< 50 則廣告不值得 |
| 資料庫事件/觸發器 | AWS RDS 預設停用 MySQL 事件；不具可移植性 |
| 響應式/事件驅動（WebFlux） | 專案採用 servlet-based Spring MVC；混用響應式增加複雜度 |

---

## 2. 公開輪播的店家狀態篩選

### 決策

公開輪播端點僅查詢以下條件的廣告：
1. `status = 'PUBLISHED'`
2. 目前時間介於 `startTime` 與 `endTime` 之間（或 `startTime`/`endTime` 為 null）
3. 連結店家的 `status = 'ACTIVE'`

透過 `BannerMapper.xml` 中的單一 JOIN 查詢實作，而非兩步驟 service 呼叫。

### 理由
- FR-008 規定非活躍店家的廣告不得顯示——必須在查詢時檢查店家狀態。
- SQL JOIN 可避免 N+1 問題，並純粹透過排程執行更新店家狀態來滿足 1 分鐘 SLA（SC-004）；輪播查詢在每次請求時反映最新狀態。
- 在記憶體中篩選在並發店家停用的情況下容易出現問題。

### 已考慮的替代方案
| 選項 | 拒絕原因 |
|--------|-----------------|
| 在廣告表中快取店家狀態 | 需要額外的同步邏輯；資料反正規化 |
| 在 service 層篩選（N+1） | O(n) 次店家查詢；不必要 |
| 店家停用時軟刪除廣告 | 重新啟用店家時操作複雜；依 FR-008 規格意圖，這是可見性規則而非資料刪除 |

---

## 3. 圖片上傳策略

### 決策

廣告圖片透過現有的 `UploadController`（`POST /admin/upload`）使用 `S3Service`（資料夾：`banner/`）上傳。儲存在 `banner` 資料表中的 `imageUrl` 是該端點回傳的完整 CDN/S3 URL。廣告建立/更新請求將 `imageUrl` 作為字串欄位接受（預先上傳的 URL），而非直接接受 multipart 檔案。

### 理由
- 將圖片上傳與廣告 CRUD 分離，遵循新聞與抽獎模組已有的模式。
- S3 服務已為開發環境提供本地回退（`LocalFileServiceImpl`）。
- 在資料庫中儲存已解析的 URL，使廣告資料表對儲存後端保持無狀態。

### 已考慮的替代方案
| 選項 | 拒絕原因 |
|--------|-----------------|
| 在廣告建立請求中使用 multipart | 使請求驗證複雜化；耦合上傳 + 建立交易 |
| 僅儲存相對路徑 | 前端需要完整 URL；解析邏輯會重複 |

---

## 4. 廣告與店家連結的完整性

### 決策

當店家被**刪除**時，所有連結的廣告在 service 層自動設為 `status = 'UNPUBLISHED'`（級聯軟下架）。當店家**停用**（`status = 'INACTIVE'`）時，廣告在資料庫中保持 `PUBLISHED` 狀態，但被公開輪播端點的 JOIN 查詢排除。當店家**重新啟用**時，廣告自動恢復顯示，無需管理員介入。

### 理由
- 符合規格中的邊緣情況規則：「刪除店家 → 廣告自動下架」。
- 重新啟用後自動恢復符合規格：「重新啟用店家 → 排程中的廣告恢復顯示」。
- 廣告不使用 FK 硬刪除級聯（廣告即使店家已消失，仍為計費目的保留為歷史記錄）。

### 已考慮的替代方案
| 選項 | 拒絕原因 |
|--------|-----------------|
| FK ON DELETE CASCADE | 永久丟失廣告資料；違反計費稽核軌跡需求 |
| FK ON DELETE RESTRICT | 防止店家刪除；破壞現有店家管理流程 |

---

## 5. 顯示順序的相同排序處理

### 決策

當兩則廣告的 `orderNum` 相同時，次要排序為 `createdAt ASC`（較早建立的廣告先顯示）。此規則編碼在所有輪播與管理員列表查詢的 ORDER BY 子句中。

### 理由
- 直接處理規格中的邊緣情況：「兩個廣告顯示順序相同時以建立時間作為第二排序依據」。
- `createdAt` 在插入時始終設定；無 nullable 邊緣情況。

---

## 6. 連結目標：店家頁面 URL 慣例

### 決策

`BannerRes` 中的 `linkUrl` 欄位由伺服器端計算為 `/stores/{storeId}`，而非儲存原始使用者輸入。FR-004 禁止外部 URL 或特定商品連結。前端使用廣告回應中的 `storeId` 建構導航 URL。

### 理由
- 消除一類管理員輸入錯誤（錯誤 URL、外部連結）。
- 前端已有以 `storeId` 為鍵的店家頁面路由。
- 與 FR-003 一致（「玩家被導向至連結店家的頁面，顯示所有上架商品」）。

### 已考慮的替代方案
| 選項 | 拒絕原因 |
|--------|-----------------|
| 儲存自由格式 `linkUrl` 輸入 | 違反 FR-004；管理員可能輸入錯誤 URL |
| 儲存商品層級連結 | 明確被 FR-004 禁止 |

---

## 7. 狀態生命週期與狀態機

### 決策

廣告狀態值：`DRAFT` → `PUBLISHED` → `UNPUBLISHED`。轉換規則：

| 來源 | 目標 | 觸發條件 |
|------|----|---------|
| DRAFT | PUBLISHED | 管理員明確發布，或排程器達到 `startTime` |
| PUBLISHED | UNPUBLISHED | 管理員明確下架，或排程器達到 `endTime` |
| UNPUBLISHED | PUBLISHED | 管理員重新發布（僅手動） |
| DRAFT | UNPUBLISHED | 不允許（無實質語義） |

新建廣告預設為 `DRAFT`，除非建立請求中包含 `status = PUBLISHED`。

### 理由
- 三狀態模型符合現有程式碼庫的值（"PUBLISHED"、"UNPUBLISHED"、"DRAFT"）。
- DRAFT 允許管理員在正式發布前準備廣告。
- 明確的單向排程轉換可防止排程器狀態震盪。

---

## 8. 公開輪播的 MyBatis 查詢方式

### 決策

在 `BannerMapper.xml` 中使用**自訂 SQL 片段**（而非 MBG 生成的 `selectByExample`）進行公開輪播查詢，因為需要對 `store` 資料表進行 JOIN，以及具備 null 安全比較的日期範圍篩選。

```sql
SELECT b.*
FROM banner b
INNER JOIN store s ON b.store_id = s.id AND s.status = 'ACTIVE'
WHERE b.status = 'PUBLISHED'
  AND (b.start_time IS NULL OR b.start_time <= NOW())
  AND (b.end_time IS NULL OR b.end_time >= NOW())
ORDER BY b.order_num ASC, b.created_at ASC
```

### 理由
- `selectByExample` 無法表達跨資料表的 JOIN。
- MBG criteria 無法在不使用自訂 SQL 的情況下表達 null 安全日期範圍。
- 命名自訂方法 `selectActiveBanners()` 清晰且可獨立測試。

---

## 已解決的未知問題摘要

| 未知問題 | 解決方案 |
|---------|-----------|
| 排程機制 | 在現有 `ScheduledTasks.java` 中每 60 秒執行 Spring `@Scheduled` |
| 公開 API 中的店家篩選 | 對 `store.status = 'ACTIVE'` 進行 SQL JOIN |
| 圖片上傳方式 | 透過現有 `/admin/upload` 預先上傳；儲存 URL 字串 |
| 店家刪除行為 | Service 層級聯至 `UNPUBLISHED` |
| 顯示順序相同處理 | 次要排序使用 `createdAt ASC` |
| 連結 URL 生成 | 伺服器端計算 `/stores/{storeId}`；無自由格式輸入 |
| 廣告狀態值 | `DRAFT` / `PUBLISHED` / `UNPUBLISHED`（現有列舉） |
| 輪播查詢方式 | Mapper XML 中含 JOIN 的自訂命名 SQL 方法 |
