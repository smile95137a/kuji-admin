# 10 - 系統參數管理

> **路由前綴**：`/admin/system-config`  
> **允許角色**：ADMIN

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/admin/system-config` | 查詢所有參數（可依 group 篩選） |
| POST | `/admin/system-config` | 新增參數 |
| PUT | `/admin/system-config/{id}` | 更新參數 |
| DELETE | `/admin/system-config/{id}` | 刪除參數 |

---

## 資料結構

```typescript
interface SystemConfigRes {
  id: string;
  configKey: string;      // 唯一識別碼（如 "DRAW_PROTECTION_MINUTES"）
  configValue: string;    // 字串值（前端依 type 轉換）
  configType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON';
  group: string;          // 分組（如 "GAME", "NOTIFY", "PAYMENT"）
  description: string;    // 參數說明
  isEditable: boolean;    // 是否可在後台編輯（false 為唯讀/程式碼控制）
  updatedAt: string;
}
```

---

## 查詢系統參數

```
GET /api/admin/system-config?group=GAME
Authorization: Bearer {token}（需 ADMIN）
```

| 參數 | 說明 |
|------|------|
| `group` | 選填，不傳則返回全部 |

---

## 新增參數

```
POST /api/admin/system-config
Authorization: Bearer {token}（需 ADMIN）
```

```typescript
interface SystemConfigCreateReq {
  configKey: string;      // 必填，英文大寫+底線
  configValue: string;    // 必填
  configType: string;     // 必填
  group: string;          // 必填
  description?: string;
}
```

---

## 更新參數

```
PUT /api/admin/system-config/{id}
Authorization: Bearer {token}（需 ADMIN）
```

```typescript
interface SystemConfigUpdateReq {
  configValue: string;    // 必填
  description?: string;
}
```

⚠️ `configKey` 和 `configType` 建立後不可修改

---

## 常用參數說明

| Group | Key | 說明 | 預設值 |
|-------|-----|------|--------|
| GAME | `DRAW_PROTECTION_MINUTES` | 抽獎保護時間（分鐘） | `5` |
| GAME | `MAX_DRAWS_PER_SESSION` | 每場最大抽取數 | `null`（無限制） |
| GAME | `SCRATCH_DESIGNATION_MINUTES` | 刮刮樂玩家指定大獎倒數（分鐘） | `10` |
| PAYMENT | `MIN_RECHARGE_AMOUNT` | 最低儲值金額（台幣） | `100` |
| NOTIFY | `MARQUEE_DURATION_SECONDS` | 跑馬燈一輪播放秒數 | `30` |

---

## 前端 UI 建議

- 依 `group` 顯示 Tab 分頁
- 根據 `configType` 使用對應輸入框：
  - `STRING` → text input
  - `NUMBER` → number input
  - `BOOLEAN` → toggle switch
  - `JSON` → code editor（如 Monaco）
- `isEditable = false` 的參數顯示為唯讀（灰色）
- 修改後顯示確認提示（系統參數異動影響全系統）
