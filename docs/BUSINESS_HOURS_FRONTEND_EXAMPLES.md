Business hours JSON examples for frontend integration

1) CreateStore (完整範例)

POST /api/admin/store

JSON payload (重點欄位)：

{
  "storeName": "KUJI 官方商店",
  "shortDescription": "專營一番賞、扭蛋精品",
  "logoUrl": "https://cdn.example.com/logo.png",
  "email": "store@example.com",
  "phone": "0912345678",
  "address": "台北市中正區xxx",
  "businessHoursStructured": {
    "schedules": [
      {"day": "MON", "open": "10:00", "close": "22:00", "closed": false},
      {"day": "TUE", "open": "10:00", "close": "22:00", "closed": false},
      {"day": "WED", "open": "10:00", "close": "22:00", "closed": false},
      {"day": "THU", "open": "10:00", "close": "22:00", "closed": false},
      {"day": "FRI", "open": "10:00", "close": "22:00", "closed": false},
      {"day": "SAT", "open": "11:00", "close": "18:00", "closed": false},
      {"day": "SUN", "closed": true}
    ],
    "exceptions": [
      {"date": "2026-12-25", "closed": true},
      {"date": "2026-10-10", "open": "11:00", "close": "17:00", "closed": false}
    ],
    "tz": "Asia/Taipei"
  },
  "owner": {
    "username": "owner@store.com",
    "password": "P@ssw0rd!",
    "displayName": "王小明",
    "phone": "0912345678"
  }
}

說明：
- `day` 使用 MON/TUE/WED/THU/FRI/SAT/SUN（後端 enum 解析）
- `open` / `close` 格式為 `HH:mm`（24 小時制），驗證由後端 `@Pattern` 負責
- `closed` 為布林，若為 true 則忽略 open/close
- `exceptions` 可用於國定假日或臨時調整，`date` 格式 `YYYY-MM-DD`
- `tz`（選填）用以標註時區

2) UpdateStore 範例（僅更新營業時間）

PATCH /api/admin/store/{storeId}

{
  "businessHoursStructured": {
    "schedules": [
      {"day": "MON", "open": "10:00", "close": "22:00"},
      {"day": "TUE", "open": "10:00", "close": "22:00"},
      {"day": "SUN", "closed": true}
    ],
    "tz": "Asia/Taipei"
  }
}

3) 前端顯示建議
- 優先顯示 `businessHoursStructured`：使用每週 `schedules` 拼成可讀字串（例如「週一至週五 10:00–22:00；週六 11:00–18:00；週日 店休」）
- 若 API 回傳 `businessHours`（文字）而非結構化欄位，直接顯示原始文字（兼容模式）

4) 驗證規則摘要（後端）
- `day`: 必須為 "MON|TUE|WED|THU|FRI|SAT|SUN"
- `open`/`close`: regex ^([01]\d|2[0-3]):[0-5]\d$
- `date`（例外）: regex ^\d{4}-\d{2}-\d{2}$

5) 注意事項
- 若前端要提供表單編輯，建議使用逐日編輯 UI（每日一行：開/關、開/關時間）並一併提供例外日期管理。
- 當使用者輸入自由文字（舊表單），需由前端提示改用結構化格式或轉換到結構化（可建立簡單 parser，但可能不完美）。
