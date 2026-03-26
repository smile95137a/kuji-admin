# 抽獎 API 回應為空 - 診斷與修復報告

## 🔍 問題描述

**使用者報告**：
```
POST /api/lottery/draw/{lotteryId}/draw
{count: 1, ticket: ["2d4c90e2-855c-4758-bab2-2500a34a344d"]}

回應：
{
    "success": true,
    "meta": { ... }
}

❌ data 欄位為空！
```

**預期行為**：
```json
{
    "success": true,
    "data": [
        {
            "ticketNumber": 13,
            "prizeLevel": "A",
            "prizeName": "炭治郎公仔",
            ...
        }
    ],
    "meta": { ... }
}
```

---

## 🔧 根本原因分析

### 1. **AOP 包裝邏輯正常**
- ✅ `GlobalResponseAspect.java` 的包裝邏輯正確
- ✅ 當 `ResponseEntity.ok(results)` 返回時，AOP 會自動包裝成 `ApiResponse.success(results)`
- ✅ 因此 `results` 應該出現在 `data` 欄位中

### 2. **Controller 邏輯正常**
- ✅ `LotteryDrawController.draw()` 返回 `ResponseEntity.ok(results)`
- ✅ 批次驗證都正確（長度、重複、UUID 格式）
- ✅ 呼叫 `ticketService.drawByTicketId()`

### 3. **核心問題：可能原因**

**猜測 1**：票券 UUID 不存在或已被抽走
- `drawByTicketId()` 會返回錯誤消息
- 但由於陣列中有元素，不應該導致 `data` 為空

**猜測 2**：票券查詢失敗
- 如果 `LotteryTicketMapper.selectByPrimaryKey(ticketId)` 返回 null
- 則 `drawByTicketId()` 返回錯誤結果
- 但仍會加入陣列中

**猜測 3**：AOP 問題（最可能）
- 如果返回的 `results` 陣列為空
- AOP 包裝時可能出現問題

---

## ✅ 修復方案

### Step 1：添加詳細日誌（已完成）

**修改**：`LotteryDrawController.java`

新增以下日誌：
```java
log.info("🎰 抽獎請求: lotteryId={}, userId={}, count={}, ticket列表長度={}", 
        lotteryId, userId, request.getCount(), 
        request.getTickets() != null ? request.getTickets().size() : 0);

log.info("✅ 驗證通過，開始執行批次抽獎: 票券={}", tickets);

for (String ticketId : tickets) {
    log.info("🎯 處理票券: {}", ticketId);
    DrawResult r = ticketService.drawByTicketId(lotteryId, userId, ticketId);
    results.add(r);
    log.info("📊 抽獎結果: success={}, message={}", r.success(), r.message());
}

log.info("✅ 批次抽獎完成，共 {} 張，成功 {} 張", 
        results.size(), 
        results.stream().filter(r -> r.success()).count());
```

### Step 2：重新編譯和部署

```bash
# 1. 本地編譯
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests -Pprod

# 預期結果
# BUILD SUCCESS
# JAR 生成：target/admin-1.0.0.jar

# 2. 上傳到 EC2
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/

# 3. 在 EC2 上重啟服務
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129
pkill -f "admin-1.0.0.jar"
nohup java -jar -Dspring.profiles.active=prod /home/ec2-user/admin-1.0.0.jar > /home/ec2-user/logs/app.log 2>&1 &
```

### Step 3：測試與驗證

**在EC2上測試**：
```bash
# 1. 查看日誌
tail -50 /home/ec2-user/logs/app.log | grep -E "🎰|✅|📊"

# 2. 測試 API
curl -X POST http://18.179.187.129:8080/api/lottery/draw/13f30242-fd8e-4a11-974f-78bd32e27cba/draw \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"count": 1, "ticket": ["2d4c90e2-855c-4758-bab2-2500a34a344d"]}'
```

---

## 📊 預期的診斷日誌

如果問題解決，您應該在日誌中看到：

```
[INFO] 🎰 抽獎請求: lotteryId=13f30242-fd8e-4a11-974f-78bd32e27cba, userId=xxx, count=1, ticket列表長度=1
[INFO] ✅ 驗證通過，開始執行批次抽獎: 票券=[2d4c90e2-855c-4758-bab2-2500a34a344d]
[INFO] 🎯 處理票券: 2d4c90e2-855c-4758-bab2-2500a34a344d
[INFO] 📊 抽獎結果: success=true, message=抽獎成功！恭喜獲得 炭治郎公仔
[INFO] ✅ 批次抽獎完成，共 1 張，成功 1 張
```

---

## 🔍 如果問題仍未解決

### 可能的後續診斷

1. **檢查 `drawByTicketId()` 是否正確執行**
   - 在 `LotteryTicketServiceImpl.drawByTicketId()` 開頭加入日誌
   - 檢查是否有異常拋出

2. **檢查票券是否真的存在**
   ```bash
   mysql -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com \
     -u admin -pWUfan0667. kuji \
     -e "SELECT * FROM lottery_ticket WHERE id='2d4c90e2-855c-4758-bab2-2500a34a344d';"
   ```

3. **檢查 AOP 是否正確包裝**
   - 在 `GlobalResponseAspect.java` 的 `wrapResponse()` 方法中添加日誌
   - 確認 `results` 陣列不為空

4. **測試隨機抽獎模式（不指定票券）**
   ```bash
   curl -X POST http://18.179.187.129:8080/api/lottery/draw/{lotteryId}/draw \
     -H "Authorization: Bearer TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"count": 1}'
   ```

---

## 📝 編譯狀態

**✅ 編譯成功**（2026-02-07 04:22:02）

```
Total time:  30.634 s
BUILD SUCCESS
```

**生成的 JAR**：
```
target/admin-1.0.0.jar (已準備就緒)
```

---

## 📋 檢查清單

- [x] 修改 `LotteryDrawController.draw()` 添加詳細日誌
- [x] 編譯成功，無錯誤
- [x] JAR 檔案生成
- [ ] 上傳到 EC2
- [ ] 重啟服務
- [ ] 測試 API
- [ ] 驗證日誌輸出
- [ ] 確認 `data` 欄位不為空

---

**後續步驟**：請上傳 JAR 到 EC2 並重啟服務，然後再次測試抽獎 API。
