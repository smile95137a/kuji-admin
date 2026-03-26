# 快速入門：付款與點數系統 (Payment & Points System)

**功能**：`006-payment-points`  
**分支**：`006-payment-points`  
**最後更新**：2026-03-22

---

## 前置需求

| 需求 | 版本 | 檢查指令 |
|-------------|---------|---------------|
| Java | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| MySQL | 8.3 | `mysql --version` |
| AWS CLI | 2.x（S3 選用） | `aws --version` |

---

## 1. 分支設定

```bash
git checkout -b 006-payment-points
# 或切換至已存在的分支：
git checkout 006-payment-points
```

---

## 2. 資料庫設定

### 2a. 執行結構描述遷移

```sql
-- Connect to your local MySQL:
mysql -u root -p kuji_db

-- New table: recharge_order
SOURCE src/main/resources/db/migration/V006__create_recharge_order.sql;

-- Extend wallet_transaction if columns are missing:
SOURCE src/main/resources/db/migration/V006b__extend_wallet_transaction.sql;

-- Verify users table has CHECK constraint:
SHOW CREATE TABLE users\G
-- Look for: CONSTRAINT chk_no_negative_balances CHECK (gold_coins >= 0 AND bonus_coins >= 0)
-- If missing, run:
ALTER TABLE users
  ADD CONSTRAINT chk_no_negative_balances
  CHECK (gold_coins >= 0 AND bonus_coins >= 0);
```

### 2b. 植入測試儲值套餐

```sql
INSERT INTO recharge_plan (id, name, gold_amount, bonus_amount, price_twd, is_active, sort_order)
VALUES
  (UUID(), '體驗包 100',  100,   0, 100.00, 1, 1),
  (UUID(), '入門包 500',  500,  50, 500.00, 1, 2),
  (UUID(), '大禮包 1000', 1000, 150, 1000.00, 1, 3),
  (UUID(), '超值包 2000', 2000, 400, 2000.00, 1, 4);
```

### 2c. 預先檢查：現有資料無負餘額

```sql
SELECT COUNT(*) AS neg_count FROM users WHERE gold_coins < 0 OR bonus_coins < 0;
-- Must return 0 before adding CHECK constraint
```

---

## 3. 本地應用程式配置

在 `src/main/resources/application-local.yml` 中新增/驗證以下屬性（**請勿**提交機密資訊）：

```yaml
payment:
  gateway:
    provider: stub          # 'stub' for local dev; 'tappay'|'ecpay'|'newebpay' in prod
    stub:
      always-success: true  # stub returns success for all payments in dev mode
    tappay:
      partner-key: ${TAPPAY_PARTNER_KEY}
      merchant-id: ${TAPPAY_MERCHANT_ID}
      sandbox: true
    callback-base-url: http://localhost:8080  # update for EC2 deployment

wallet:
  recharge-order:
    ttl-minutes: 30         # PENDING order expiry TTL
  optimistic-lock:
    max-retries: 3          # Retry attempts on version conflict
```

透過環境變數設定機密資訊（絕對不要放在 `application.yml` 中）：

```bash
export TAPPAY_PARTNER_KEY=your_key_here
export TAPPAY_MERCHANT_ID=your_merchant_id
```

---

## 4. 建構與執行

```bash
# Clean build
mvn clean package -DskipTests

# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Application starts on:
# http://localhost:8080
```

---

## 5. 冒煙測試（手動）

### 5a. 取得錢包餘額

```bash
# Login first to get JWT
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}' \
  | jq '.data.token'

# Set token
TOKEN="<jwt-from-above>"

# Get wallet
curl -s http://localhost:8080/api/wallet \
  -H "Authorization: Bearer $TOKEN" | jq .
```

預期回應：
```json
{ "code": 200, "data": { "goldBalance": 0, "bonusBalance": 0, "totalRecharged": 0 } }
```

### 5b. 列出儲值套餐

```bash
curl -s http://localhost:8080/api/recharge-plans | jq .
```

預期結果：包含步驟 2b 植入的 4 個套餐陣列。

### 5c. 發起儲值（Stub 模式）

```bash
# Get a plan ID first
PLAN_ID=$(curl -s http://localhost:8080/api/recharge-plans | jq -r '.data[0].id')

# Initiate recharge
curl -s -X POST http://localhost:8080/api/wallet/recharge \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"planId\": \"$PLAN_ID\"}" | jq .
```

預期結果：回傳 `rechargeOrderId` + `payUrl`。Stub 模式下，`payUrl` 為本地重新導向。

### 5d. 模擬回呼（Stub 模式）

Stub 模式下，測試端點模擬金流回呼：

```bash
ORDER_ID="<rechargeOrderId-from-above>"

curl -s -X POST "http://localhost:8080/api/wallet/recharge/callback/stub?orderId=$ORDER_ID&success=true" | jq .
```

然後驗證錢包是否已入帳：

```bash
curl -s http://localhost:8080/api/wallet \
  -H "Authorization: Bearer $TOKEN" | jq '.data.goldBalance'
# Should now equal the plan's goldAmount (e.g., 100)
```

### 5e. 檢視交易歷史

```bash
curl -s "http://localhost:8080/api/wallet/transactions?size=5" \
  -H "Authorization: Bearer $TOKEN" | jq '.data.content[0]'
```

預期結果：一筆 `RECHARGE` 類型的交易，`goldDelta` 為正值且 `goldAfter` 正確。

### 5f. 獎品回收

```bash
# Requires a prize in prize_box with is_recyclable=1 and status=AVAILABLE
PRIZE_BOX_ID="<existing-prize-box-id>"

curl -s -X POST http://localhost:8080/api/prize-box/recycle \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"prizeBoxId\": \"$PRIZE_BOX_ID\"}" | jq .
```

預期結果：`bonusAwarded` > 0，`newBonusBalance` 增加。

### 5g. 管理員錢包調整

```bash
# Admin JWT required
ADMIN_TOKEN="<admin-jwt>"
USER_ID="<player-uuid>"

curl -s -X POST http://localhost:8080/admin/wallet/adjust \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"$USER_ID\",
    \"currency\": \"BONUS\",
    \"delta\": 10,
    \"reason\": \"Test admin adjustment from quickstart guide\"
  }" | jq .
```

預期結果：回傳 `transactionId`，`bonusBalanceAfter` 增加 10。

---

## 6. 執行自動化測試

```bash
# Unit tests only (fast, no DB)
mvn test -pl . -Dtest="WalletServiceTest,RechargeServiceTest"

# Integration tests (requires running MySQL)
mvn test -pl . -Dspring.profiles.active=test -Dtest="WalletControllerTest"

# All tests
mvn test
```

### 主要測試案例驗證

| 測試 | 情境 | 預期結果 |
|------|---------|---------|
| `testDeductGoldFirst` | 200 金幣、100 紅利；消費 150 | 剩餘 50 金幣、100 紅利 |
| `testDeductGoldThenBonus` | 50 金幣、100 紅利；消費 100 | 剩餘 0 金幣、50 紅利 |
| `testInsufficientBalance` | 50 金幣、20 紅利；消費 100 | InsufficientBalanceException |
| `testConcurrentDeductions` | 2 個執行緒同時扣款 | 僅一個成功（或兩個都成功，如果總量足夠） |
| `testDuplicateCallback` | 同一個金流回呼發送兩次 | 錢包恰好入帳一次 |
| `testRecycleShippedPrize` | 獎品 status=SHIPPED | 409 Conflict |
| `testAdminAdjustNegativeBalance` | 扣款超過餘額 | 422 Unprocessable |
| `testAdminAdjustNoReason` | 缺少 reason 欄位 | 400 Bad Request |

---

## 7. 生產環境部署檢查清單

- [ ] 確認金流業者（TapPay / ECPay / NewebPay）並在 EC2 環境變數中設定生產憑證
- [ ] 在 `application-prod.yml` 中設定 `payment.gateway.provider=tappay`（或所選業者）
- [ ] 在生產環境設定檔中停用 Stub 端點（`/api/wallet/recharge/callback/stub`）
- [ ] 向業者登記 webhook URL：`https://your-domain.com/api/wallet/recharge/callback`
- [ ] 如業者要求則驗證閘道 IP 白名單
- [ ] 對生產環境 RDS 執行 `V006__create_recharge_order.sql`（先以 `EXPLAIN` 進行試運行）
- [ ] 在 `users` 表新增 CHECK 約束（先確認無負餘額資料列）
- [ ] 設定 `RechargeOrderExpiryJob` 的排程 cron（每 5 分鐘）
- [ ] 部署後監控 `recharge_order` 表，注意卡在 PENDING 狀態的訂單

---

## 8. 主要檔案參考

| 檔案 | 用途 |
|------|---------|
| `WalletService.java` | 核心餘額操作介面 |
| `WalletServiceImpl.java` | 含樂觀鎖的原子性扣款/入帳 |
| `RechargeService.java` | 付款發起 + 回呼處理 |
| `WalletController.java` | `GET /api/wallet`、`GET /api/wallet/transactions` |
| `RechargeController.java` | `POST /api/wallet/recharge` + 回呼 |
| `PrizeBoxController.java` | `POST /api/prize-box/recycle` |
| `AdminWalletController.java` | `POST /admin/wallet/adjust` |
| `AdminRechargePlanController.java` | 儲值套餐 CRUD |
| `WalletTransaction.java` | 稽核日誌實體 |
| `RechargeOrder.java` | 付款狀態機實體 |
| `V006__create_recharge_order.sql` | 遷移：新增 recharge_order 表 |
| `V006b__extend_wallet_transaction.sql` | 遷移：擴充 wallet_transaction |

---

## 9. 疑難排解

### 「回呼後餘額未更新」
- 檢查 `recharge_order.status` — 若已是 `SUCCESS`，重複回呼已被冪等處理（正確行為）
- 檢查 `wallet_transaction` 是否有 `RECHARGE` 記錄 — 若有，餘額已入帳
- 確認金流業者可以從其 IP 範圍訪問 webhook URL

### 「日誌中出現 OptimisticLockException」
- 在並發負載下屬於正常現象；服務最多重試 3 次
- 若重試耗盡，客戶端收到 409 — 玩家應重試操作
- 檢查是否有失控的並發請求（可能是客戶端重複提交）

### 「閘道總是回傳 502」
- 確認本地開發時 `payment.gateway.provider` 設定為 `stub`
- 確認 TapPay/ECPay 沙盒憑證有效
- 檢查防火牆/安全群組：EC2 對外 HTTPS 至業者 IP 範圍的出站流量必須開放

### 「gold_coins 的 CHECK 約束違規」
- 有 bug 導致負餘額（部署後不應發生）
- 檢查 `WalletServiceImpl.deductCoins` — 確保在扣款前呼叫 `hasEnoughGold()` 防護
- 檢查是否有繞過應用層直接修改 DB 的操作
