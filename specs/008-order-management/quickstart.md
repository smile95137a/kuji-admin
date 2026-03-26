# Quickstart: 訂單管理 (Order Management)

**功能分支**：`008-order-management`  
**日期**：2026-03-22

---

## 概覽

此功能為 KUJI 平台新增完整的訂單管理系統。玩家可從獎品盒（prize box）出貨獎品，店家負責人透過單向狀態機推進訂單。管理員可跨所有店家檢視及管理訂單。

**核心流程**：
1. 玩家從獎品盒建立訂單 → 原子性扣除獎品
2. 店家負責人推進狀態：待處理 → 備貨中 → 已出貨 → 已完成
3. 店家負責人或管理員取消（出貨前）→ 獎品退回獎品盒

---

## 前置條件

| 工具 | 版本 |
|------|------|
| Java | 21 |
| Maven | 3.9+ |
| MySQL | 8.3 |
| Spring Boot | 3.3.3 |

資料庫資料表 `order`、`order_item`、`order_status_log` 必須存在。若尚未執行遷移，請套用 `doc/sql/prize-box-wallet-order-ddl.sql` 中的 DDL。

---

## 資料庫設定

確認三張訂單相關資料表存在：

```sql
SHOW TABLES LIKE '%order%';
-- Expected: order, order_item, order_status_log
```

若資料表不存在，請執行：

```bash
mysql -h <host> -u <user> -p <database> < doc/sql/prize-box-wallet-order-ddl.sql
```

---

## 啟動應用程式

```bash
# From project root
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

# Start with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or build and run jar
mvn clean package -DskipTests
java -jar target/admin-*.jar --spring.profiles.active=local
```

伺服器預設啟動於 `8080` 埠。

---

## 快速 API 測試

### 1. 登入（取得 JWT token）

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"store_owner","password":"password123"}'
```

從回應中複製 `token`，並在 shell 中設定為 `TOKEN`：

```bash
export TOKEN="eyJhbGciOi..."
```

### 2. 建立出貨訂單（玩家）

```bash
curl -X POST http://localhost:8080/order/ship \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "prizeBoxIds": ["<prize-box-uuid>"],
    "shippingMethod": "HOME_DELIVERY",
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "recipientAddress": "台北市信義區信義路五段7號"
  }'
```

預期結果：`201 Created`，附帶 `orderIds` 陣列。

### 3. 列出訂單（店家負責人——Admin API）

```bash
curl -X POST http://localhost:8080/admin/orders/list \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

預期結果：`200 OK`，附帶限定於呼叫者店家的訂單陣列。

### 4. 推進訂單狀態

```bash
curl -X PUT http://localhost:8080/admin/orders/<order-id>/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"targetStatus":"PREPARING"}'
```

預期結果：`200 OK`，附帶更新後的訂單狀態。

### 5. 取消訂單

```bash
curl -X DELETE http://localhost:8080/admin/orders/<order-id> \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cancelReason":"顧客要求取消"}'
```

預期結果：`200 OK`。獎品項目退回獎品盒。

---

## 執行測試

```bash
# All tests
mvn test

# Specific service test
mvn test -Dtest=OrderServiceTest

# Specific controller test
mvn test -Dtest=AdminOrderControllerTest
```

---

## 重要檔案

| 檔案 | 用途 |
|------|------|
| `src/main/java/.../service/OrderService.java` | Service 介面 |
| `src/main/java/.../service/impl/OrderServiceImpl.java` | 業務邏輯：狀態機、獎品盒扣除 |
| `src/main/java/.../controller/admin/AdminOrderController.java` | 管理端 REST endpoints |
| `src/main/java/.../controller/api/OrderController.java` | 玩家端 REST endpoints |
| `src/main/java/.../enums/OrderStatusEnum.java` | 狀態代碼 + 中文標籤 |
| `src/main/resources/mapper/OrderMapper.xml` | 訂單的 MyBatis SQL |
| `specs/008-order-management/contracts/` | 完整 API 契約 |
| `specs/008-order-management/data-model.md` | 實體定義 + 狀態機 |

---

## 狀態機快速參考

```
PENDING → PREPARING → SHIPPED → COMPLETED
              ↓            ↑ (blocked)
           CANCELLED ──────┘ (only from PENDING or PREPARING)
```

| 轉換 | HTTP 呼叫 |
|------|----------|
| PENDING → PREPARING | `PUT /admin/orders/{id}/status` `{"targetStatus":"PREPARING"}` |
| PREPARING → SHIPPED | `PUT /admin/orders/{id}/status` `{"targetStatus":"SHIPPED","trackingNo":"..."}` |
| SHIPPED → COMPLETED | `PUT /admin/orders/{id}/status` `{"targetStatus":"COMPLETED"}` |
| 取消（任意→CANCELLED） | `DELETE /admin/orders/{id}` `{"cancelReason":"..."}` |

---

## 疑難排解

### `/admin/orders` 回傳 403
- 確認 JWT token 屬於具有 `STORE_OWNER`、`STORE_EDITOR` 或 `ADMIN` 角色的使用者
- 確認該使用者在 `store_user` 中介表中有記錄

### 更新狀態時回傳 409
- 在 DB 中確認目前訂單狀態：`SELECT status FROM \`order\` WHERE id = '<id>';`
- 狀態轉換每次只能前進一步，不能後退

### 獎品盒不在 `IN_BOX` 狀態
- 查詢：`SELECT id, status, order_id FROM prize_box WHERE id = '<box-id>';`
- 若為 `SHIPPED`：已在某筆訂單中，無法建立重複訂單
- 若為 `RECYCLED`：獎品已回收，無法出貨

### 店家負責人看不到訂單
- 確認使用者已連結至正確店家：`SELECT * FROM store_user WHERE admin_user_id = '<user-id>';`
- 確認訂單的 `store_id` 一致

---

## 環境變數（正式環境）

`application-prod.properties` 中的關鍵設定：

```properties
spring.datasource.url=jdbc:mysql://<RDS_HOST>:3306/<DB_NAME>
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASS>
jwt.secret=<JWT_SECRET>
jwt.expiration=86400000
```
