# 功能擴充實作總結

## 實作日期：2025-12-25

---

## 1. 新增依賴 (pom.xml)

```xml
<!-- Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

---

## 2. 資料庫遷移 (sql/V2_user_address_invoice_log.sql)

### 新增表格：
- `district` - 行政區資料（台灣縣市鄉鎮區）
- `system_log` - 系統操作日誌
- `email_log` - 郵件發送記錄
- `marquee` - 跑馬燈訊息
- `report_snapshot` - 報表快照

### User 表擴充欄位：
- `line_id` - LINE 登入 ID
- `recipient_name` - 收件人姓名
- `recipient_phone` - 收件人電話
- `city` - 縣市
- `district` - 鄉鎮市區
- `address_detail` - 詳細地址
- `invoice_type` - 發票類型
- `invoice_email` - 發票寄送信箱
- `carrier_code` - 載具條碼
- `tax_id` - 統一編號
- `company_name` - 公司名稱
- `referral_code` - 推薦碼
- `referred_store_id` - 推薦來源店家

---

## 3. 新增 Entity

| Entity | 說明 |
|--------|------|
| `District` | 行政區資料 |
| `SystemLog` | 系統日誌 |
| `EmailLog` | 郵件記錄 |
| `Marquee` | 跑馬燈 |
| `ReportSnapshot` | 報表快照 |

---

## 4. 新增 Mapper

| Mapper | 主要方法 |
|--------|----------|
| `DistrictMapper` | selectAll, selectAllCities, selectByCity |
| `SystemLogMapper` | insert, selectByType, selectByUserId, deleteOldLogs |
| `EmailLogMapper` | insert, updateStatus, selectPendingForRetry |
| `MarqueeMapper` | insert, update, delete, selectActiveMarquees |
| `ReportSnapshotMapper` | insert, selectByTypeAndPeriod |

---

## 5. 新增 Service

### EmailService / EmailServiceImpl
- `sendVerificationEmail()` - 發送驗證碼
- `sendPasswordResetEmail()` - 發送密碼重設
- `sendOrderNotification()` - 發送訂單通知
- `retryFailedEmails()` - 重試失敗的郵件

### SystemLogService / SystemLogServiceImpl
- `log()` - 記錄日誌
- `logLogin()` - 記錄登入
- `logAdminAction()` - 記錄管理操作
- `logError()` - 記錄錯誤
- `deleteOldLogs()` - 清除過期日誌

### MarqueeService / MarqueeServiceImpl
- `getActiveMarquees()` - 取得啟用中的跑馬燈
- `createMarquee()` - 新增跑馬燈
- `broadcastMarquee()` - WebSocket 廣播

### ReportService / ReportServiceImpl
- `getRevenueReport()` - 營業額報表
- `getReferralReport()` - 推薦碼報表
- `getLotteryResultReport()` - 開獎結果報表
- `getRechargeReport()` - 儲值報表
- `getBonusReport()` - 贈送點數報表

---

## 6. 新增 Controller

### 前台 API (api/)
| 路徑 | Controller | 說明 |
|------|------------|------|
| `/district/**` | DistrictController | 行政區資料（公開） |
| `/marquee/**` | MarqueeController | 跑馬燈訊息（公開） |

### 後台 API (admin/)
| 路徑 | Controller | 說明 |
|------|------------|------|
| `/admin/marquee/**` | AdminMarqueeController | 跑馬燈管理 |
| `/admin/report/**` | AdminReportController | 報表查詢 |
| `/admin/system-log/**` | AdminSystemLogController | 系統日誌查詢 |

---

## 7. 新增 DTO

### 報表回應 (dto/res/report/)
- `RevenueReportRes` - 營業額報表
- `ReferralReportRes` - 推薦碼報表
- `LotteryResultReportRes` - 開獎結果報表
- `RechargeReportRes` - 儲值報表
- `BonusReportRes` - 贈送點數報表

### 請求 (dto/req/)
- `MarqueeCreateReq` - 跑馬燈建立
- `MarqueeUpdateReq` - 跑馬燈更新

---

## 8. 新增配置

### AsyncConfig
- 非同步任務執行器配置
- 用於郵件發送、日誌記錄等

### WebSocketConfig
- WebSocket STOMP 配置
- 端點: `/ws` (SockJS), `/ws-native` (原生)
- 訂閱: `/topic/marquee`, `/topic/marquees`

### application.yml 郵件配置
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_USERNAME:}
    password: ${GMAIL_APP_PASSWORD:}
```

---

## 9. 定時任務

### ScheduledTasks
- 每 5 分鐘重試失敗的郵件
- 每天凌晨 3 點清除超過 90 天的日誌

---

## 10. 安全配置更新

新增公開端點：
- `/api/district/**` - 行政區資料
- `/api/marquee/**` - 跑馬燈
- `/api/ws/**` - WebSocket

---

## API 使用範例

### 行政區 API

```bash
# 取得所有縣市
GET /api/district/cities

# 取得指定縣市的鄉鎮市區
GET /api/district/districts/台北市

# 取得樹狀結構
GET /api/district/tree
```

### 跑馬燈 API

```bash
# 前台取得啟用中的跑馬燈
GET /api/marquee

# 後台新增跑馬燈
POST /api/admin/marquee
{
  "content": "🎉 新品上架！",
  "linkUrl": "/lottery/new",
  "linkType": "INTERNAL",
  "priority": 10,
  "bgColor": "#ff6600",
  "textColor": "#ffffff",
  "isActive": true
}
```

### WebSocket 訂閱

```javascript
// 訂閱跑馬燈更新
const socket = new SockJS('/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/marquees', (message) => {
    const marquees = JSON.parse(message.body);
    updateMarquee(marquees);
  });
});
```

### 報表 API

```bash
# 營業額報表
GET /api/admin/report/revenue?startDate=2025-01-01&endDate=2025-01-31

# 推薦碼報表
GET /api/admin/report/referral?startDate=2025-01-01&endDate=2025-01-31

# 開獎結果報表
GET /api/admin/report/lottery-result?startDate=2025-01-01&endDate=2025-01-31

# 儲值報表
GET /api/admin/report/recharge?startDate=2025-01-01&endDate=2025-01-31

# 贈送點數報表
GET /api/admin/report/bonus?startDate=2025-01-01&endDate=2025-01-31
```

---

## Gmail SMTP 設定指南

1. 開啟 Google 帳戶的「兩步驟驗證」
2. 前往「應用程式密碼」設定
3. 建立新的應用程式密碼
4. 設定環境變數：
   ```bash
   export GMAIL_USERNAME=your-email@gmail.com
   export GMAIL_APP_PASSWORD=your-app-password
   ```

---

## 待完成項目

1. ⬜ 更新 User Entity 加入新欄位
2. ⬜ 更新使用者註冊/更新 API 支援新欄位
3. ⬜ 建立 UserProfileUpdateReq DTO
4. ⬜ 執行 SQL 遷移腳本
5. ⬜ 整合登入日誌記錄到 AuthService
