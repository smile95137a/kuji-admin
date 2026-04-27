
📋 後台前端 API 文件

 Base URL: http://18.179.187.129/api
 認證: Authorization: Bearer {ADMIN_TOKEN}
 所有回應包在 { success, data, meta } 外層

----------------------------------------------------------------------------------------------------------------

🔑 一、角色列表（請改用 API，不要讓使用者手動輸入）

GET /admin/roles

 權限：ADMIN

Response

 [
   {
     "id": "uuid",
     "name": "系統管理員",
     "code": "ROLE_ADMIN",
     "description": "最高權限",
     "createdAt": "2026-01-01T00:00:00"
   },
   {
     "id": "uuid",
     "name": "店家管理員",
     "code": "ROLE_STORE_OWNER",
     "description": "店家擁有者"
   },
   {
     "id": "uuid",
     "name": "店家小編",
     "code": "ROLE_STORE_EDITOR",
     "description": "店家編輯人員"
   }
 ]

 ⚠️ 所有需要填角色的地方（建立帳號、指派角色），請先 call 此 API 取清單做 dropdown，不要讓使用者手動打字

----------------------------------------------------------------------------------------------------------------

👥 二、帳號管理（建立店家負責人 — 已修復 Bug）

POST /admin/users/store-owner

 權限：ADMIN

Request Body

 {
   "email": "owner@example.com",
   "displayName": "測試負責人",
   "phone": "0912345678",
   "storeName": "測試店家",
   "remark": "備註（選填）"
 }

Response — 完整 AdminUser 物件，含初始密碼（系統自動產生）

----------------------------------------------------------------------------------------------------------------

POST /admin/users/store-editor

 權限：ADMIN

Request Body

 {
   "email": "editor@example.com",
   "displayName": "測試小編",
   "phone": "0912345678",
   "storeId": "店家UUID",
   "remark": "備註（選填）"
 }

----------------------------------------------------------------------------------------------------------------

🆕 二-A、後台管理者帳號列表查詢（新增需求）

POST /admin/users/list

 權限：ADMIN
 說明：前端後台帳號管理頁已實作，需後端支援此 API

Request Body

 {
   "condition": {
     "keyword": "string（模糊比對 email / displayName / nickname）",
     "status": "ACTIVE | INACTIVE（空字串或 null = 全部）",
     "storeId": "string（空字串或 null = 不限店家）"
   },
   "sortBy": "createdAt",
   "sortOrder": "DESC"
 }

Response

 {
   "success": true,
   "data": [
     {
       "id": "uuid",
       "email": "owner@example.com",
       "displayName": "測試負責人",
       "status": "ACTIVE",
       "roles": ["ROLE_STORE_OWNER"],
       "storeId": "uuid",
       "storeName": "測試店家",
       "createdAt": "2026-01-01T00:00:00",
       "lastLoginAt": "2026-04-25T10:30:00"
     }
   ]
 }

 ⚠️ 前端已呼叫此端點，後端尚未實作時查詢功能無法正常運作

----------------------------------------------------------------------------------------------------------------

📊 三、報表 API（5 種）

 所有報表：
 
 
  - storeId 不用傳，後端自動帶入（StoreOwner 只看自己店）
  - startDate / endDate 格式："2026-01-01" (YYYY-MM-DD)
  - 全部是 POST 請求，body 結構相同

3-1. 營業額報表 POST /admin/report/revenue

Request

 {
   "condition": {
     "startDate": "2026-01-01",
     "endDate": "2026-04-30"
   }
 }

Response

 {
   "startDate": "2026-01-01",
   "endDate": "2026-04-30",
   "totalRevenue": 125000.00,
   "totalOrders": 350,
   "totalDraws": 4200,
   "avgOrderAmount": 357.14,
   "growthRate": 12.5,
   "dailyDetails": [
     { "date": "2026-01-01", "revenue": 3500.00, "orders": 10, "draws": 120 }
   ],
   "storeDetails": [
     {
       "storeId": "uuid",
       "storeName": "測試店家",
       "revenue": 80000.00,
       "orders": 200,
       "percentage": 64.0
     }
   ]
 }

----------------------------------------------------------------------------------------------------------------

3-2. 開獎結果報表 POST /admin/report/lottery-result

Request

 {
   "condition": {
     "startDate": "2026-01-01",
     "endDate": "2026-04-30"
   }
 }

Response

 {
   "totalDraws": 4200,
   "totalPrizes": 420,
   "bigPrizes": 42,
   "totalAmount": 210000.00,
   "prizeStats": [
     {
       "prizeLevel": "A獎",
       "totalCount": 10,
       "wonCount": 8,
       "remainCount": 2,
       "wonPercentage": 80.0
     }
   ],
   "lotteryStats": [
     {
       "lotteryId": "uuid",
       "lotteryTitle": "鬼滅一番賞",
       "storeName": "測試店家",
       "totalSlots": 100,
       "soldSlots": 75,
       "remainSlots": 25,
       "soldPercentage": 75.0,
       "revenue": 37500.00
     }
   ]
 }

----------------------------------------------------------------------------------------------------------------

3-3. 儲值報表 POST /admin/report/recharge

Response

 {
   "totalAmount": 500000.00,
   "totalCount": 1250,
   "avgAmount": 400.00,
   "growthRate": 8.3,
   "dailyDetails": [
     { "date": "2026-01-01", "amount": 12000.00, "count": 30, "newUsers": 5 }
   ],
   "planStats": [
     {
       "planId": "uuid",
       "planName": "NT$500 方案",
       "planPrice": 500.00,
       "bonusPoints": 50.00,
       "purchaseCount": 300,
       "totalAmount": 150000.00,
       "percentage": 30.0
     }
   ]
 }

----------------------------------------------------------------------------------------------------------------

3-4. 贈送紅利報表 POST /admin/report/bonus

Response

 {
   "totalBonusPoints": 15000.00,
   "totalCount": 450,
   "benefitUsers": 320,
   "growthRate": 5.2,
   "dailyDetails": [
     { "date": "2026-01-01", "points": 500.00, "count": 15 }
   ],
   "typeStats": [
     {
       "bonusType": "REFERRAL",
       "typeName": "推薦好友",
       "totalPoints": 8000.00,
       "count": 200,
       "percentage": 53.3
     }
   ]
 }

 bonusType 可能值：REFERRAL / PROMOTION / ADJUSTMENT / REGISTRATION

----------------------------------------------------------------------------------------------------------------

3-5. 推薦碼報表 POST /admin/report/referral

（條件同上，response 結構包含推薦人/被推薦人統計）

----------------------------------------------------------------------------------------------------------------

📋 四、系統日誌

 權限：ADMIN 限定

依類型查詢

 GET /admin/system-log/type/{logType}?limit=100

┌────────────────┬──────────────┐
│ logType 常用值 │ 說明         │
├────────────────┼──────────────┤
│ LOGIN          │ 登入日誌     │
├────────────────┼──────────────┤
│ DRAW           │ 抽獎日誌     │
├────────────────┼──────────────┤
│ PAYMENT        │ 支付日誌     │
├────────────────┼──────────────┤
│ ADMIN          │ 後台操作日誌 │
└────────────────┴──────────────┘

Response — Array of SystemLog：

 [
   {
     "id": "uuid",
     "logType": "LOGIN",
     "action": "LOGIN_SUCCESS",
     "userId": "uuid",
     "userType": "admin",
     "requestIp": "1.2.3.4",
     "requestUrl": "/api/admin/auth/login",
     "requestMethod": "POST",
     "responseStatus": 200,
     "durationMs": 145,
     "createdAt": "2026-04-25T10:30:00",
     "errorMessage": null
   }
 ]

依使用者查詢

 GET /admin/system-log/user/{userId}?limit=100

依日期範圍查詢

 GET /admin/system-log/date-range?start=2026-04-01T00:00:00&end=2026-04-25T23:59:59&limit=200

清除舊日誌（謹慎使用）

 DELETE /admin/system-log/cleanup?days=90

----------------------------------------------------------------------------------------------------------------

🆕 五、前台會員搜尋（MemberPicker 全域元件需求）

POST /admin/frontend-users/list

 說明：前端「會員選擇器」元件使用，需支援 keyword 模糊搜尋
 此 API 已存在，但請確認 condition.keyword 欄位有做以下三欄的 OR 模糊比對：
   - nickname（暱稱）
   - email
   - phone（手機號碼）

Request Body

 {
   "condition": {
     "keyword": "string（OR 比對：nickname / email / phone）",
     "status": "ACTIVE | INACTIVE（選填）",
     "provider": "LOCAL | LINE | GOOGLE（選填）"
   },
   "sortBy": "createdAt",
   "sortOrder": "DESC"
 }

Response

 {
   "success": true,
   "data": [
     {
       "id": "uuid",
       "nickname": "測試玩家",
       "email": "player@example.com",
       "phone": "0912345678",
       "status": "ACTIVE",
       "provider": "LOCAL"
     }
   ]
 }

 ⚠️ 前端 MemberPicker 元件（錢包管理、獎品箱查詢等頁面）依賴此端點的 keyword 欄位

----------------------------------------------------------------------------------------------------------------

🆕 六、訂單查詢條件更新（userKeyword 欄位）

POST /admin/orders/list

 說明：前端訂單管理頁的「玩家」查詢欄位已從 userId（需輸入系統 ID）改為 userKeyword（輸入暱稱/Email/手機），
       需後端在 condition 中支援 userKeyword 欄位

Request Body

 {
   "condition": {
     "orderNo": "string（模糊）",
     "userKeyword": "string（OR 比對玩家 nickname / email / phone）",
     "shippingStatus": "PENDING | PREPARING | SHIPPED | COMPLETED | CANCELLED",
     "shippingMethod": "HOME_DELIVERY | CONVENIENCE_STORE",
     "recipientName": "string（模糊）",
     "recipientPhone": "string（模糊）",
     "createdAtStart": "2026-01-01T00:00:00",
     "createdAtEnd":   "2026-04-30T23:59:59"
   },
   "sortBy": "createdAt",
   "sortOrder": "DESC"
 }

Response

 {
   "success": true,
   "data": [
     {
       "id": "uuid",
       "orderNo": "ORD-20260425-001",
       "userId": "uuid",
       "userNickname": "測試玩家",
       "userEmail": "player@example.com",
       "storeId": "uuid",
       "storeName": "測試店家",
       "shippingStatus": "PENDING",
       "shippingStatusName": "待處理",
       "shippingMethod": "HOME_DELIVERY",
       "shippingMethodName": "宅配",
       "recipientName": "王大明",
       "recipientPhone": "0912345678",
       "totalAmount": 500.00,
       "createdAt": "2026-04-25T10:00:00"
     }
   ]
 }

 ⚠️ 若後端尚未支援 userKeyword，請新增此欄位到 OrderCondition 並做以下 JOIN 查詢：
    LEFT JOIN frontend_user fu ON orders.user_id = fu.id
    WHERE (fu.nickname LIKE '%keyword%' OR fu.email LIKE '%keyword%' OR fu.phone LIKE '%keyword%')

----------------------------------------------------------------------------------------------------------------

🆕 七、Banner 查詢條件（storeId 改由下拉選單傳入）

POST /admin/banners/list（或現有的 Banner 查詢 API）

 說明：前端 Banner 查詢條件的「店家」欄位已改為下拉選單（從 GET /admin/stores/options 取得清單），
       條件傳入方式不變，仍為 storeId（UUID），確認後端接受此欄位即可

Request Body

 {
   "condition": {
     "status": "PUBLISHED | UNPUBLISHED",
     "title": "string（模糊）",
     "storeId": "uuid（由下拉選單選取，非手動輸入）",
     "createdAtStart": "2026-01-01T00:00:00",
     "createdAtEnd":   "2026-04-30T23:59:59"
   }
 }

 ⚠️ 前端已依賴 GET /admin/stores/options 取得店家下拉清單，請確認此端點回傳格式：
    [ { "id": "uuid", "storeName": "測試店家", "status": "ACTIVE" } ]
    或
    [ { "value": "uuid", "label": "測試店家" } ]


----------------------------------------------------------------------------------------------------------------

🆕 八、商品多連抽設定（allowMultiDraw / multiDrawOptions）

POST /admin/lotteries （建立）
PUT  /admin/lotteries/{id} （更新）

 說明：前端商品新增/編輯表單已加入「允許多連抽」與「多連抽選項」欄位，
       request body 中的 lottery 物件需支援以下欄位：

Request Body（lottery 物件內）

 {
   "allowMultiDraw": true,
   "multiDrawOptions": [10, 50, 100]
 }

欄位說明

 allowMultiDraw：boolean，是否允許多連抽（true = 開啟）
 multiDrawOptions：number[]，可選的連抽次數（例如 [10, 50, 100]）
   - 僅在 allowMultiDraw = true 時有意義
   - 前端傳入已排序的正整數陣列

GET /admin/lotteries/{id} Response（需回傳）

 {
   "allowMultiDraw": true,
   "multiDrawOptions": [10, 50, 100]
 }

 ⚠️ 若 allowMultiDraw = false 或欄位不存在，前端預設不顯示多連抽選項
