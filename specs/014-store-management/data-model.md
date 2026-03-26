# 資料模型：店家管理 (Store Management)

**Feature**: `014-store-management`  
**Phase**: 1 — 設計  
**Date**: 2026-03-22  

---

## 實體

### 1. Store（店家）

> **資料表**: `store`  
> **主鍵**: `id` VARCHAR(36) — UUID v4  

| 欄位 | Java 欄位 | 型別 | 可為空 | 說明 |
|--------|------------|------|----------|-------------|
| `id` | `id` | VARCHAR(36) | NOT NULL PK | UUID v4 |
| `owner_id` | `ownerId` | VARCHAR(36) | NOT NULL FK → `admin_user.id` | 一對一負責人帳號 |
| `store_name` | `storeName` | VARCHAR(100) | NOT NULL | 顯示名稱（非唯一） |
| `short_description` | `shortDescription` | VARCHAR(255) | NULL | 用於列表卡片檢視 |
| `long_description` | `longDescription` | LONGTEXT | NULL | 店家完整頁面描述（Mapper 中為 BLOB） |
| `logo_url` | `logoUrl` | VARCHAR(500) | NULL | S3 URL；404 時前端顯示預設圖片 |
| `cover_image_url` | `coverImageUrl` | VARCHAR(500) | NULL | 店家頁面封面圖片 |
| `email` | `email` | VARCHAR(100) | NULL | 聯絡 Email |
| `phone` | `phone` | VARCHAR(30) | NULL | 聯絡電話 |
| `address` | `address` | VARCHAR(255) | NULL | 實體地址 |
| `business_hours` | `businessHours` | VARCHAR(255) | NULL | 自由格式，例如「週一至週五 09:00–18:00」 |
| `facebook_url` | `facebookUrl` | VARCHAR(500) | NULL | 選填社群連結 |
| `instagram_url` | `instagramUrl` | VARCHAR(500) | NULL | 選填社群連結 |
| `line_id` | `lineId` | VARCHAR(100) | NULL | 選填 LINE ID |
| `status` | `status` | VARCHAR(20) | NOT NULL DEFAULT 'ENABLED' | `ENABLED` \| `DISABLED` |
| `remark` | `remark` | LONGTEXT | NULL | 內部管理員備注（不對玩家顯示） |
| `created_by` | `createdBy` | VARCHAR(36) | NULL | 操作管理員 ID |
| `created_at` | `createdAt` | DATETIME | NOT NULL | 新增時自動設定 |
| `updated_by` | `updatedBy` | VARCHAR(36) | NULL | 最後修改者 ID（FR-011） |
| `updated_at` | `updatedAt` | DATETIME | NOT NULL | 更新時自動設定 |

**索引**：
- `idx_store_status` on `(status)` — 用於公開列表查詢
- `idx_store_owner_id` on `(owner_id)` — 用於所有權驗證

---

### 2. AdminUser（店家負責人帳號）

> **資料表**: `admin_user`  
> **已存在** — 此處僅列出原子性店家建立時所用到的欄位。

| 欄位 | 本功能相關說明 |
|--------|----------------------|
| `id` | UUID — 在 `createStore` 期間生成 |
| `username` | 唯一登入名稱，來自 `CreateStoreReq.owner.username` |
| `password` | 初始密碼的 BCrypt 雜湊值 |
| `email` | 來自 `CreateStoreReq.owner.email` |
| `display_name` | 來自 `CreateStoreReq.owner.displayName` |
| `status` | 建立時設為 `ACTIVE` |
| `force_change_password` | 建立時設為 `true`（負責人首次登入須強制修改密碼） |
| `created_by` | 建立該店家的平台管理員 |

---

### 3. AdminUserRole（角色綁定）

> **資料表**: `admin_user_role`  
> **已存在** — 每次建立負責人時新增一筆資料列。

| 欄位 | 建立店家時的值 |
|--------|------------------------|
| `admin_user_id` | 新負責人的 UUID |
| `role_code` | `ROLE_STORE_OWNER` |

---

## 店家狀態機

```
         ┌────────────────────────────────────────────────────────┐
         │                     STORE STATUS                        │
         └────────────────────────────────────────────────────────┘

                              createStore()
                                   │
                                   ▼
                           ┌──────────────┐
                           │   ENABLED    │◄──── enableStore()
                           └──────┬───────┘       (admin only)
                                  │
                           disableStore()
                           (admin only)
                                  │
                                  ▼
                           ┌──────────────┐
                           │   DISABLED   │
                           └──────────────┘

     On disableStore():
       ├── store.status       → DISABLED
       ├── lottery.status     → OFF_SHELF     （批次 UPDATE，該店家所有商品）
       └── news_banner.status → DISABLED      （批次 UPDATE，該店家所有橫幅）

     On enableStore():
       └── store.status       → ENABLED       （僅此項 — 商品／橫幅維持原狀）
```

**不變式**：
- `DISABLED` 的店家對 `/api/stores` 及 `/api/stores/{id}` 不可見。
- `DISABLED` 店家的商品對所有公開商品查詢不可見。
- 重新啟用**不會**恢復商品或橫幅（FR-005）。
- 不支援刪除店家 — 僅停用（規格假設）。

---

## 關聯關係

```
AdminUser (1) ────────────────── (1) Store
  owner_id FK on Store.owner_id
  一間店家一位負責人；建立後負責人綁定不可變更（v1.0）

Store (1) ─────────────────── (N) Lottery（商品）
  Lottery.store_id FK
  連鎖：停用店家 → 批次將所有抽獎商品設為 OFF_SHELF

Store (1) ─────────────────── (N) NewsBanner
  NewsBanner.store_id FK
  連鎖：停用店家 → 批次將所有橫幅設為 DISABLED

Store (1) ─────────────────── (N) Order
  Order.store_id FK
  停用時無連鎖 — 現有訂單繼續處理（FR-012）
```

---

## DTO 定義

### 請求 DTO

#### `CreateStoreReq`
```java
public class CreateStoreReq {
    // Store fields
    @NotBlank String storeName;
    String shortDescription;
    String longDescription;
    String logoUrl;
    String coverImageUrl;
    String email;
    String phone;
    String address;
    String businessHours;
    String facebookUrl;
    String instagramUrl;
    String lineId;
    String remark;

    // Owner account (created atomically)
    @NotNull OwnerAccountReq owner;

    @Data
    public static class OwnerAccountReq {
        @NotBlank String username;
        @NotBlank String password;       // initial password (force-change on login)
        @NotBlank String displayName;
        String email;
        String phone;
    }
}
```

#### `UpdateStoreReq`
```java
public class UpdateStoreReq {
    String storeName;
    String shortDescription;
    String longDescription;
    String logoUrl;
    String coverImageUrl;
    String email;
    String phone;
    String address;
    String businessHours;
    String facebookUrl;
    String instagramUrl;
    String lineId;
    String remark;
    // NOTE: ownerId intentionally absent — owner binding is immutable for STORE_OWNER role
    // ADMIN role may pass ownerId in a separate future endpoint (v2)
}
```

#### `UpdateStoreStatusReq`
```java
public class UpdateStoreStatusReq {
    @NotBlank
    @Pattern(regexp = "ENABLED|DISABLED")
    String status;
}
```

---

### 回應 DTO

#### `StoreRes`（後台完整檢視）
包含所有 Store 欄位 + 負責人顯示名稱。

#### `StoreListItemRes`（公開卡片 — `/api/stores`）
```java
public class StoreListItemRes {
    String id;
    String storeName;
    String shortDescription;
    String logoUrl;
}
```

#### `StoreDetailRes`（公開店家頁面 — `/api/stores/{id}`）
```java
public class StoreDetailRes {
    String id;
    String storeName;
    String shortDescription;
    String longDescription;
    String logoUrl;
    String coverImageUrl;
    String email;
    String phone;
    String address;
    String businessHours;
    String facebookUrl;
    String instagramUrl;
    String lineId;
    List<LotteryListItemRes> products;  // ON_SHELF only
}
```

---

## 驗證規則

| 欄位 | 規則 |
|-------|------|
| `storeName` | 建立時必填；最多 100 字元 |
| `shortDescription` | 選填；最多 255 字元 |
| `logoUrl` | 選填；若有提供須為有效 URL 格式 |
| `owner.username` | 必填；在 `admin_user` 中唯一；4–50 個英數字或底線 |
| `owner.password` | 必填；最少 8 字元 |
| `status` | 必須為 `ENABLED` 或 `DISABLED` |
| 店家建立 | 整個操作為原子性；任何失敗均回滾 |
| 負責人變更 | `ownerId` 對 `STORE_OWNER` 呼叫者忽略（伺服器端強制執行） |
| 跨店家存取 | `STORE_OWNER` 只能編輯自己的店家 |
