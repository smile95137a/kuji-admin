# 11 - 物流方式管理

> **路由前綴**：`/admin/shipping-methods`  
> **允許角色**：`ROLE_ADMIN`（僅限最高管理員，StoreOwner / StoreEditor 無此權限）  
> 管理前台可用的運送方式（宅配、超商取貨等），新增/停用後即時影響前台出貨選項。

---

## API 列表

| 方法 | 路徑 | 說明 | 角色 |
|------|------|------|------|
| GET | `/admin/shipping-methods` | 查詢所有運送方式（含停用） | ADMIN |
| POST | `/admin/shipping-methods` | 新增運送方式 | ADMIN |
| PUT | `/admin/shipping-methods/{id}` | 修改運送方式 | ADMIN |
| PUT | `/admin/shipping-methods/{id}/status` | 啟用 / 停用 | ADMIN |

---

## 資料結構

```typescript
interface ShippingMethodRes {
  id: string;
  name: string;           // 顯示名稱（如「7-11 超商取貨」）
  code: string;           // 系統代碼（如 SEVEN_ELEVEN、HOME_DELIVERY）
  provider: string;       // 物流商名稱（如「統一速達」、「黑貓宅急便」）
  fee: number;            // 運費（單位：元）
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

interface ShippingMethodCreateReq {
  name: string;           // 必填，最長 100 字
  code: string;           // 必填，唯一，最長 50 字（英文大寫 + 底線）
  provider?: string;      // 物流商名稱，最長 100 字
  fee: number;            // 必填，最小 0（免運則填 0）
}

interface ShippingMethodUpdateReq {
  name?: string;
  code?: string;
  provider?: string;
  fee?: number;
  status?: 'ACTIVE' | 'INACTIVE';
}
```

---

## 查詢所有運送方式

```
GET /api/admin/shipping-methods
Authorization: Bearer {token}（需 ADMIN）
```

回應：`ShippingMethodRes[]`，依 `name` 升冪排序（後端處理，前端不需另外排序）。

---

## 新增運送方式

```
POST /api/admin/shipping-methods
Authorization: Bearer {token}（需 ADMIN）
```

```json
{
  "name": "7-11 超商取貨",
  "code": "SEVEN_ELEVEN",
  "provider": "統一速達",
  "fee": 60
}
```

- `code` 不可重複，若重複後端回傳 `400 + message: "運送方式代碼已存在"`
- 新增後 `status` 預設為 `ACTIVE`

---

## 修改運送方式

```
PUT /api/admin/shipping-methods/{id}
Authorization: Bearer {token}（需 ADMIN）
```

所有欄位皆為選填；只傳要修改的欄位。

---

## 啟用 / 停用

```
PUT /api/admin/shipping-methods/{id}/status
Authorization: Bearer {token}（需 ADMIN）
```

```json
{ "status": "INACTIVE" }
```

> ⚠️ **停用規則**：停用後前台立即不顯示此選項；但**歷史訂單不受影響**，已選用此方式的訂單資料保留完整。

---

## 前端 UI 規格

### 頁面路由
`/admin/shipping-methods`  
側邊欄位置：系統設定 > 物流方式管理（或獨立一級選單，依選單設定而定）

---

### 列表頁

| 欄位 | 說明 |
|------|------|
| 名稱 | `name` |
| 代碼 | `code`（灰色小字） |
| 物流商 | `provider` |
| 運費 | `fee` 元（0 顯示「免運」） |
| 狀態 | `ACTIVE` → 綠色 Badge「啟用中」；`INACTIVE` → 灰色 Badge「已停用」 |
| 操作 | 「編輯」按鈕、「啟用/停用」Toggle |

- 列表依 `name` 字母升冪排序（前端根據 API 回傳直接渲染，不需前端重排）
- 右上角「+ 新增運送方式」按鈕
- 停用的項目仍顯示於列表（可重新啟用），僅用淡色或 Badge 區分

---

### 新增 / 編輯表單（Modal 彈窗）

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| 顯示名稱 | 文字輸入 | ✅ | 最長 100 字 |
| 系統代碼 | 文字輸入 | ✅ | 英文大寫 + 底線，編輯時唯讀（不可修改） |
| 物流商名稱 | 文字輸入 | ❌ | 最長 100 字 |
| 運費（元） | 數字輸入 | ✅ | 最小 0，0 代表免運 |

**系統代碼** 在新增時可自由填寫，儲存後不可修改（避免影響歷史訂單關聯）。  
建議代碼格式範例：

| 物流方式 | 建議代碼 |
|----------|----------|
| 7-11 超商取貨 | `SEVEN_ELEVEN` |
| 全家超商取貨 | `FAMILY_MART` |
| 黑貓宅急便 | `T_CAT` |
| 宅配通 | `HOME_DELIVERY` |
| 萊爾富 | `HI_LIFE` |

---

### 啟用 / 停用確認

- 點擊停用 Toggle 時，彈出確認對話框：  
  「停用後，前台玩家將無法選擇此運送方式。歷史訂單不受影響，確定停用？」
- 點擊啟用無需確認，直接生效

---

### 狀態提示

| 操作 | 成功提示 | 失敗提示 |
|------|----------|----------|
| 新增 | `運送方式「{name}」已新增` | `代碼已存在，請更換` |
| 修改 | `已更新` | `儲存失敗，請重試` |
| 停用 | `「{name}」已停用` | - |
| 啟用 | `「{name}」已啟用` | - |
