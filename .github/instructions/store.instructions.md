---
description: "Use when working on store management, store CRUD, store activation/deactivation, or store-product relationships. Covers store data, banner linking, and report integration."
applyTo: ["**/Store*", "**/store*"]
---

# 店家管理模組規範

## 一、模組目的

- 記錄每個店家的基本資料
- 控制店家的啟用/停用狀態
- 前台可展示店家資訊，點擊店家後查看該店家所有商品
- 讓平台能對接 Banner、商品、報表等模組
- 所有店家資料均由 Admin 建立與管理

### 設計理念
- 店家與店家小編**無法自行操作權限或停用帳號**
- Banner 與店家資料連動，便於前端展示與廣告計費
- 每筆操作需留存操作者與時間紀錄

## 二、Store 資料表欄位

| 欄位 | 說明 | 必填 | 資料類型建議 |
|------|------|------|-------------|
| id | 店家 ID | ✔ | BIGINT PRIMARY KEY AUTO_INCREMENT |
| owner_id | 店家主帳號 ID | ✔ | BIGINT (FK → admin_user.id) |
| store_name | 店家公開顯示名稱 | ✔ | VARCHAR(100) |
| short_description | 短描述（列表用） | ✔ | VARCHAR(255) |
| long_description | 詳細介紹、品牌故事 | ✘ | TEXT |
| logo_url | 店家 Logo（小圖） | ✔ | VARCHAR(255) |
| cover_image_url | 封面圖片（大圖） | ✘ | VARCHAR(255) |
| email | 聯絡 Email | ✔ | VARCHAR(100) |
| phone | 聯絡電話 | ✔ | VARCHAR(20) |
| address | 地址 | ✔ | VARCHAR(255) |
| business_hours | 營業時間 | ✔ | VARCHAR(100) |
| facebook_url | Facebook 連結 | ✘ | VARCHAR(255) |
| instagram_url | Instagram 連結 | ✘ | VARCHAR(255) |
| status | 上架狀態 | ✔ | VARCHAR(20) (ACTIVE/INACTIVE) |
| remark | 後台備註 | ✘ | TEXT |
| created_at | 建立時間 | ✔ | DATETIME |
| updated_at | 更新時間 | ✔ | DATETIME |
| updated_by | 最後修改者 ID | ✘ | BIGINT |

## 三、前台顯示邏輯

### 使用方式
- 前台「店家列表」顯示所有啟用中店家（名稱、Logo、介紹）
- 點擊店家 → 前往「店家專屬頁」

### 店家專屬頁內容
- 店家大圖（封面圖，若無則系統預設）
- 店家簡介（短 + 長）
- 營業時間、社群連結
- 該店家所有上架中商品

### 查詢範例
```java
// 查詢所有啟用的店家（前台列表）
StoreExample example = new StoreExample();
example.createCriteria().andStatusEqualTo("ACTIVE");
List<Store> activeStores = storeMapper.selectByExample(example);
```

## 四、模組關聯

### 與商品（Lottery）關聯
- 每個商品必須隸屬於唯一一個店家，不能跨店家共用
- 訂單、抽獎、報表皆依據商品所屬店家計算

### 與 Banner 關聯
- Banner 一定綁定一個店家，點擊 Banner 即跳店家頁

### 與抽獎/報表關聯
- 每一筆遊玩紀錄屬於該店家的營業額
- 日結/月結報表依店家分開統計
- 店家停用後不可再被抽獎、不可搜尋到商品

## 五、管理流程

### 5.1 新增店家
1. Admin 點擊「新增店家」，填寫必填欄位
2. 系統建立 `store` 紀錄（status=ACTIVE）
3. 同時建立「店家 Owner 帳號」（參考 `store-account-management.prompt.md`）
4. 店家與帳號建立需在同一事務中完成（`@Transactional`）

### 5.2 編輯店家
- **可修改**: Logo、描述、地址、營業時間、社群連結
- **不可修改**: owner_id（綁定後不可變更）
- Admin 可編輯任何店家，StoreOwner 僅可編輯自己的店家
- 修改後立即同步到前台顯示

### 5.3 停用店家（重要連動）
當 Admin 停用店家時，系統需在同一事務中執行：
1. 店家狀態 → INACTIVE
2. 該店家所有商品 → OFF_SHELF
3. 前台隱藏該店家與其商品
4. 禁止新抽獎（進行中可繼續）
5. 相關 Banner 一併停用（可選）

## 六、權限矩陣

| 角色 | 新增店家 | 編輯店家 | 停用店家 | 查看所有店家 |
|------|---------|---------|---------|-------------|
| **Admin** | ✔ | ✔ | ✔ | ✔ |
| **StoreOwner** | ✖ | ✔ (僅自己) | ✖ | ✖ (僅自己) |
| **StoreEditor** | ✖ | ✖ | ✖ | ✖ |
| **前台使用者** | ✖ | ✖ | ✖ | ✔ (僅啟用的店家) |
