# 功能測試指南

## 環境準備

### 1. 執行 SQL 遷移
```bash
# 連接到 MySQL 資料庫
mysql -u root -p kuji_db < sql/V2_user_address_invoice_log.sql
```

### 2. 設定郵件（可選）
```bash
# Windows
set GMAIL_USERNAME=your-email@gmail.com
set GMAIL_APP_PASSWORD=your-app-password

# Linux/Mac
export GMAIL_USERNAME=your-email@gmail.com
export GMAIL_APP_PASSWORD=your-app-password
```

### 3. 啟動後端
```bash
mvn spring-boot:run
```

---

## API 測試

### 行政區 API（公開，不需要認證）

```bash
# 取得所有縣市
curl http://localhost:8080/api/district/cities

# 取得台北市的行政區
curl http://localhost:8080/api/district/districts/臺北市

# 取得所有行政區（樹狀結構）
curl http://localhost:8080/api/district/tree

# 取得所有行政區資料
curl http://localhost:8080/api/district/all

# 取得單一行政區
curl "http://localhost:8080/api/district?city=臺北市&district=大安區"
```

### 跑馬燈 API（公開）

```bash
# 取得啟用中的跑馬燈
curl http://localhost:8080/api/marquee
```

### 後台跑馬燈管理（需要 Admin Token）

```bash
# 取得 Admin Token
TOKEN=$(curl -s -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@kuji.com","password":"admin123"}' | jq -r '.data.accessToken')

# 取得所有跑馬燈
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/marquee

# 新增跑馬燈
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  http://localhost:8080/api/admin/marquee \
  -d '{
    "content": "🎉 歡迎光臨 KUJI 一番賞！",
    "linkUrl": "/lottery",
    "linkType": "INTERNAL",
    "priority": 10,
    "bgColor": "#ff6600",
    "textColor": "#ffffff",
    "isActive": true
  }'

# 更新跑馬燈狀態
curl -X PATCH -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/marquee/{id}/status?status=INACTIVE"

# 刪除跑馬燈
curl -X DELETE -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/marquee/{id}
```

### 報表 API（需要 Admin Token）

```bash
# 營業額報表
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/report/revenue?startDate=2025-01-01&endDate=2025-01-31"

# 推薦碼報表
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/report/referral?startDate=2025-01-01&endDate=2025-01-31"

# 開獎結果報表
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/report/lottery-result?startDate=2025-01-01&endDate=2025-01-31"

# 儲值報表
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/report/recharge?startDate=2025-01-01&endDate=2025-01-31"

# 贈送點數報表
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/report/bonus?startDate=2025-01-01&endDate=2025-01-31"
```

### 系統日誌 API（僅 Admin）

```bash
# 按類型查詢日誌
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/system-log/type/AUTH?limit=50"

# 按使用者查詢日誌
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/system-log/user/{userId}?limit=50"

# 按時間範圍查詢
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/system-log/date-range?start=2025-01-01T00:00:00&end=2025-01-31T23:59:59&limit=100"

# 清除過期日誌
curl -X DELETE -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/system-log/cleanup?days=90"
```

---

## WebSocket 測試

### 使用 JavaScript 連接

```javascript
// 引入 SockJS 和 STOMP
// <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
// <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // 訂閱跑馬燈列表更新
    stompClient.subscribe('/topic/marquees', function(message) {
        const marquees = JSON.parse(message.body);
        console.log('收到跑馬燈更新:', marquees);
        // 更新 UI
    });
    
    // 訂閱單條跑馬燈
    stompClient.subscribe('/topic/marquee', function(message) {
        const marquee = JSON.parse(message.body);
        console.log('收到新跑馬燈:', marquee);
        // 顯示新跑馬燈
    });
});
```

### 手動觸發廣播

```bash
# 後台手動廣播所有跑馬燈
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/marquee/broadcast
```

---

## 前端整合建議

### 行政區選擇器

```javascript
// React 範例
const [cities, setCities] = useState([]);
const [districts, setDistricts] = useState([]);
const [selectedCity, setSelectedCity] = useState('');
const [selectedDistrict, setSelectedDistrict] = useState('');

// 載入縣市
useEffect(() => {
  fetch('/api/district/cities')
    .then(res => res.json())
    .then(data => setCities(data.data));
}, []);

// 當選擇縣市時載入行政區
useEffect(() => {
  if (selectedCity) {
    fetch(`/api/district/districts/${selectedCity}`)
      .then(res => res.json())
      .then(data => setDistricts(data.data));
  }
}, [selectedCity]);
```

### 跑馬燈組件

```javascript
// React 範例
const [marquees, setMarquees] = useState([]);

// 初始載入
useEffect(() => {
  fetch('/api/marquee')
    .then(res => res.json())
    .then(data => setMarquees(data.data));
}, []);

// WebSocket 即時更新
useEffect(() => {
  const socket = new SockJS('/api/ws');
  const stompClient = Stomp.over(socket);
  
  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/marquees', (message) => {
      setMarquees(JSON.parse(message.body));
    });
  });
  
  return () => {
    if (stompClient.connected) {
      stompClient.disconnect();
    }
  };
}, []);
```

---

## 報表資料格式

### 營業額報表 (RevenueReportRes)

```json
{
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "totalRevenue": 1500000,
  "totalOrders": 500,
  "totalDraws": 2500,
  "avgOrderAmount": 3000,
  "growthRate": 15.5,
  "dailyDetails": [
    {
      "date": "2025-01-01",
      "revenue": 50000,
      "orders": 20,
      "draws": 100
    }
  ],
  "storeDetails": [
    {
      "storeId": "store-001",
      "storeName": "台北旗艦店",
      "revenue": 500000,
      "orders": 150,
      "percentage": 33.33
    }
  ]
}
```

### 推薦碼報表 (ReferralReportRes)

```json
{
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "totalReferrals": 100,
  "totalBonusAmount": 50000,
  "conversionRate": 25.5,
  "growthRate": 10.2,
  "rankings": [
    {
      "referralCode": "ABC123",
      "userName": "小明",
      "storeName": "台北店",
      "referralCount": 50,
      "totalBonus": 25000,
      "rank": 1
    }
  ]
}
```

---

## 常見問題

### Q: 行政區 API 返回空
A: 確認已執行 SQL 遷移，district 表中有資料

### Q: WebSocket 連接失敗
A: 確認後端已啟動，檢查 CORS 配置

### Q: 郵件發送失敗
A: 確認已設定 GMAIL_USERNAME 和 GMAIL_APP_PASSWORD 環境變數

### Q: 報表查詢很慢
A: 建議添加適當的資料庫索引，或限制查詢時間範圍
