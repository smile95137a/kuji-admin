# 🎯 當前狀態與下一步行動計劃

## ✅ 已完成工作

### 1. 推薦碼 403 問題修正
- **問題**：`GET /admin/referral-codes/validate/{code}` 缺少 `@PreAuthorize`
- **修正**：已添加 `@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")`
- **文件**：`AdminReferralCodeController.java`（Line 162）

### 2. 獎品系統設計完成
- **資料庫設計**：`init-prize-box-order-wallet.sql`
  - 6 張表：wallet, wallet_transaction, draw_result, order, order_item, lottery_prize_pool
  - 2 個視圖：v_prize_box, v_order_overview
- **實體類別**：已建立 6 個 Entity
  - Wallet.java ✅
  - DrawResult.java ✅
  - LotteryPrizePool.java ✅
  - WalletTransaction.java（待建立）
  - Order.java（待建立）
  - OrderItem.java（待建立）

### 3. 完整實作指南
- **文件**：`PRIZE_BOX_ORDER_WALLET_IMPLEMENTATION.md`
- **內容**：完整的資料庫設計、API 設計、業務流程、實作順序

---

## 🚨 立即行動（優先順序）

### Priority 1: 測試推薦碼 403 問題修正 ⏰ 現在

#### 方法 A：使用自動部署腳本（推薦）
```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
quick-fix-and-deploy.bat
```

#### 方法 B：手動步驟
```cmd
# 1. 編譯
mvn clean package -DskipTests

# 2. 上傳
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem target\admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/

# 3. 重啟
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129
cd /home/ec2-user
pkill -f admin-1.0.0.jar
nohup java -jar admin-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &
exit

# 4. 測試（等待 10 秒後）
# 先登入取得 Token
curl -X POST http://18.179.187.129:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@kuji.com","password":"admin123"}'

# 複製返回的 token，然後測試推薦碼 API
curl -X GET "http://18.179.187.129:8080/api/admin/referral-codes/validate/TEST001" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

**預期結果**：
- 🟢 200 OK + `{"success":true,"data":true/false,...}`
- ❌ 不應該是 403 Forbidden

---

### Priority 2: 執行資料庫遷移 ⏰ 推薦碼測試完成後

#### 方法 A：透過 MySQL Workbench（推薦）
1. 連接到 RDS：`database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com:3306`
2. 開啟 `init-prize-box-order-wallet.sql`
3. 執行腳本
4. 驗證：`SELECT COUNT(*) FROM wallet;`

#### 方法 B：透過 EC2 SSH
```bash
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129

# 上傳 SQL 檔
exit  # 回到 Windows

scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem init-prize-box-order-wallet.sql ec2-user@18.179.187.129:/home/ec2-user/

# 再次 SSH 登入
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129

# 執行 SQL（需要 RDS 連線資訊）
mysql -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com \
      -u admin \
      -p \
      kuji < init-prize-box-order-wallet.sql

# 驗證
mysql -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com -u admin -p kuji -e "SHOW TABLES;"
```

---

### Priority 3: 建立 Mapper 介面 ⏰ 資料庫遷移完成後

需要建立 6 個 Mapper：

#### 3.1 WalletMapper.java
```java
package com.group.admin.mapper;

import com.group.admin.entity.Wallet;
import org.apache.ibatis.annotations.*;

@Mapper
public interface WalletMapper {
    @Select("SELECT * FROM wallet WHERE user_id = #{userId}")
    Wallet selectByUserId(String userId);
    
    @Insert("INSERT INTO wallet (id, user_id, gold, bonus) VALUES (#{id}, #{userId}, #{gold}, #{bonus})")
    int insert(Wallet wallet);
    
    @Update("UPDATE wallet SET gold = #{gold}, bonus = #{bonus}, updated_at = NOW() WHERE id = #{id}")
    int updateById(Wallet wallet);
    
    @Update("UPDATE wallet SET gold = gold + #{amount} WHERE id = #{id} AND (gold + #{amount}) >= 0")
    int updateGold(@Param("id") String id, @Param("amount") Long amount);
    
    @Update("UPDATE wallet SET bonus = bonus + #{amount} WHERE id = #{id} AND (bonus + #{amount}) >= 0")
    int updateBonus(@Param("id") String id, @Param("amount") Long amount);
}
```

#### 3.2 WalletTransactionMapper.java
```java
@Mapper
public interface WalletTransactionMapper {
    @Insert("INSERT INTO wallet_transaction (id, wallet_id, user_id, type, amount, currency_type, balance_after, reference_id, reference_type, description) " +
            "VALUES (#{id}, #{walletId}, #{userId}, #{type}, #{amount}, #{currencyType}, #{balanceAfter}, #{referenceId}, #{referenceType}, #{description})")
    int insert(WalletTransaction transaction);
    
    @Select("SELECT * FROM wallet_transaction WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<WalletTransaction> selectByUserId(@Param("userId") String userId, @Param("limit") int limit, @Param("offset") int offset);
}
```

#### 3.3 DrawResultMapper.java
```java
@Mapper
public interface DrawResultMapper {
    @Insert("INSERT INTO draw_result (id, user_id, lottery_id, store_id, prize_id, prize_name, prize_image_url, prize_level, status) " +
            "VALUES (#{id}, #{userId}, #{lotteryId}, #{storeId}, #{prizeId}, #{prizeName}, #{prizeImageUrl}, #{prizeLevel}, #{status})")
    int insert(DrawResult drawResult);
    
    @Select("SELECT * FROM draw_result WHERE user_id = #{userId} AND status = 'IN_PRIZE_BOX' ORDER BY drawn_at DESC")
    List<DrawResult> selectPrizeBoxByUserId(String userId);
    
    @Update("UPDATE draw_result SET status = #{status}, order_id = #{orderId} WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status, @Param("orderId") String orderId);
}
```

#### 3.4 OrderMapper.java
```java
@Mapper
public interface OrderMapper {
    @Insert("INSERT INTO `order` (...) VALUES (...)")
    int insert(Order order);
    
    @Select("SELECT * FROM `order` WHERE store_id = #{storeId} ORDER BY created_at DESC")
    List<Order> selectByStoreId(String storeId);
    
    @Update("UPDATE `order` SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status);
}
```

#### 3.5 OrderItemMapper.java
```java
@Mapper
public interface OrderItemMapper {
    @Insert("INSERT INTO order_item (...) VALUES (...)")
    int insert(OrderItem item);
    
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(String orderId);
}
```

#### 3.6 LotteryPrizePoolMapper.java
```java
@Mapper
public interface LotteryPrizePoolMapper {
    @Insert("INSERT INTO lottery_prize_pool (...) VALUES (...)")
    int insert(LotteryPrizePool pool);
    
    @Select("SELECT * FROM lottery_prize_pool WHERE lottery_id = #{lotteryId} AND is_active = 1")
    List<LotteryPrizePool> selectByLotteryId(String lotteryId);
    
    @Update("UPDATE lottery_prize_pool SET remaining_quantity = remaining_quantity - 1 WHERE id = #{id} AND remaining_quantity > 0")
    int decreaseQuantity(String id);
}
```

---

### Priority 4: 建立 Service 層 ⏰ Mapper 建立完成後

#### 4.1 WalletService
- `getOrCreateWallet(String userId)`
- `recharge(String userId, Long amount, String currencyType)`
- `consume(String userId, Long amount, String referenceId)`
- `grantBonus(String userId, Long amount, String reason)`

#### 4.2 DrawService
- `executeDraw(String userId, String lotteryId, int count)`
- 加權隨機抽獎演算法
- 扣除庫存（樂觀鎖）
- 扣除點數

#### 4.3 PrizeBoxService
- `getPrizeBox(String userId)`
- `recyclePrizes(String userId, List<String> drawResultIds)`

#### 4.4 OrderService
- `createOrdersFromPrizeBox(String userId, OrderCreateReq req)`
- 依店家分組
- 建立訂單與明細
- 更新 draw_result 狀態

---

## 📊 實作進度追蹤

### Phase 1: 基礎設施 ⏳ 進行中
- [x] SQL 腳本設計
- [x] Entity 類別（3/6）
- [ ] Mapper 介面（0/6）
- [ ] 資料庫遷移執行

### Phase 2: 錢包系統 ⏸️ 待開始
- [ ] WalletService
- [ ] WalletController
- [ ] 測試

### Phase 3: 抽獎系統 ⏸️ 待開始
- [ ] LotteryPrizePoolService
- [ ] DrawService
- [ ] 測試

### Phase 4: 賞品盒 ⏸️ 待開始
- [ ] PrizeBoxService
- [ ] PrizeBoxController
- [ ] 測試

### Phase 5: 訂單系統 ⏸️ 待開始
- [ ] OrderService
- [ ] OrderController（前後台）
- [ ] 測試

### Phase 6: 整合測試 ⏸️ 待開始
- [ ] 完整流程測試
- [ ] 權限測試
- [ ] 並發測試

---

## 🎯 今天的目標（選一個開始）

### 選項 A：修復推薦碼後立即實作獎品系統（推薦）
1. ✅ 執行 `quick-fix-and-deploy.bat`
2. ✅ 測試推薦碼 API（確認 403 已修復）
3. ✅ 執行 SQL 遷移
4. 🔄 建立所有 Mapper
5. 🔄 實作 WalletService + Controller
6. 🔄 測試錢包 API

### 選項 B：先完成推薦碼測試再決定
1. ✅ 執行 `quick-fix-and-deploy.bat`
2. ✅ 測試推薦碼 API
3. ⏸️ 如果有其他問題，先處理
4. ⏸️ 沒問題後再開始獎品系統

---

## 📝 注意事項

### ⚠️ 推薦碼 API 可能的 403 原因
1. **Filter 路徑匹配問題**
   - 檢查 `AdminJwtAuthenticationFilter` 是否正確處理 `/admin/**`
   - 檢查 `SecurityConfig` 的 Order

2. **Role 名稱大小寫**
   - 資料庫存 `ROLE_ADMIN`
   - `@PreAuthorize("hasRole('ADMIN')")` 會自動加 `ROLE_` 前綴

3. **Token 中缺少 roles**
   - 檢查 JWT 是否包含 `roles: ["ROLE_ADMIN"]`
   - 檢查 `UserPrincipal` 是否正確設定

### 💡 如果 403 問題持續
提供以下資訊：
1. JWT Token 內容（用 jwt.io 解碼）
2. Filter 日誌（`🔍` 和 `🎭` emoji）
3. 完整錯誤訊息

---

## 🚀 開始實作命令

```cmd
# Windows 本地

# 1. 測試推薦碼修正
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
quick-fix-and-deploy.bat

# 2. 執行 SQL 遷移（MySQL Workbench）
# 開啟 init-prize-box-order-wallet.sql 並執行

# 3. 建立所有 Mapper（我會協助）
# 告訴我：「開始建立 Mapper」

# 4. 建立所有 Service（我會協助）
# 告訴我：「開始建立 Service」

# 5. 建立所有 Controller（我會協助）
# 告訴我：「開始建立 Controller」
```

---

**👉 請告訴我您想要：**
1. 先測試推薦碼 403 修正？
2. 直接開始獎品系統實作？
3. 還有其他問題需要處理？

準備好了就說一聲，我們開始！🚀
