# 🛠️ Mapper 方法缺失修復指南

## 問題描述

執行 MyBatis Generator 後，以下 Mapper 缺少自定義方法：
1. `ConsumptionRecordMapper` 缺少 `selectByUserId()` 和 `selectAll()`
2. `ContactInquiryMapper` 缺少 `selectAll()` 和 `updateStatus()`
3. `OrderMapper` → **Order entity 整個不見了！**

## ✅ 已修復項目

### 1. ConsumptionRecordMapper
- ✅ 已在 Mapper 介面添加自定義方法
- ✅ 已在 XML 添加對應 SQL

### 2. ContactInquiryMapper
- ✅ 已在 Mapper 介面添加自定義方法
- ✅ 已在 XML 添加對應 SQL

### 3. OrderMapper
- ⚠️ **無法修復，因為 Order entity 不存在！**

---

## 🔧 修復步驟

### Step 1：重新生成 Order entity

你的 `generatorConfig.xml` 已經有 `order` 表的配置：

```xml
<table tableName="`order`" delimitIdentifiers="true" domainObjectName="Order" 
       enableCountByExample="true" enableUpdateByExample="true" 
       enableDeleteByExample="true" enableSelectByExample="true" 
       selectByExampleQueryId="true" modelType="flat">
```

**執行 MyBatis Generator：**

```bash
mvn mybatis-generator:generate
```

這會生成：
- `entity/Order.java`
- `example/OrderExample.java`  
- `mapper/OrderMapper.java`（會被覆蓋）
- `mapper/OrderMapper.xml`（會被覆蓋）

### Step 2：為 OrderMapper 添加 selectByUserId() 方法

生成後，在 `OrderMapper.java` 最後添加：

```java
// ==================== 自定義方法 ====================

/**
 * 根據用戶 ID 查詢訂單
 */
List<Order> selectByUserId(@Param("userId") String userId);
```

### Step 3：為 OrderMapper.xml 添加對應 SQL

在 `OrderMapper.xml` 最後（`</mapper>` 之前）添加：

```xml
<!-- ==================== 自定義查詢 ==================== -->

<select id="selectByUserId" resultMap="BaseResultMap" parameterType="java.lang.String">
  select id, user_id, order_number, store_id, status, total_items,
         shipping_method, recipient_name, recipient_phone, recipient_address,
         store_code, store_name, store_address, tracking_no, remark,
         created_at, updated_at
  from "order"
  where user_id = #{userId,jdbcType=VARCHAR}
  order by created_at DESC
</select>
```

### Step 4：確認其他相關 Mapper

如果 `PrizeBoxMapper` 或其他 Mapper 也缺少方法，參考同樣的模式添加。

---

## ⚠️ 注意事項

1. **先備份現有的自定義 Mapper XML**  
   MyBatis Generator 會覆蓋 XML 檔案，如果你有自定義查詢會被刪除。

2. **不要手動修改 MBG 生成的 Example 類別**  
   Example 類別是完全由 MBG 管理的。

3. **Order 表名必須用反引號**  
   因為 `order` 是 SQL 保留字，所以配置裡用 `` `order` ``。

4. **檢查 import**  
   生成後確認所有 import 都正確：
   ```java
   import com.group.admin.entity.Order;
   import com.group.admin.example.OrderExample;
   ```

---

## 🧪 驗證步驟

執行以下命令確認編譯成功：

```bash
mvn clean compile -DskipTests
```

應該看到：
```
[INFO] BUILD SUCCESS
```

---

## 📋 快速檢查清單

- [ ] 執行 `mvn mybatis-generator:generate`
- [ ] 確認 `Order.java` 和 `OrderExample.java` 存在
- [ ] 為 `OrderMapper.java` 添加 `selectByUserId()` 方法
- [ ] 為 `OrderMapper.xml` 添加對應 SQL
- [ ] 執行 `mvn clean compile -DskipTests` 驗證
- [ ] 檢查 `OrderServiceImpl.java` 的 import 是否正確

---

## 🔍 如果還有問題

### 錯誤：找不到 Order 類別

```
The import com.group.admin.entity.Order cannot be resolved
```

**解決方法：**
1. 確認 `src/main/java/com/group/admin/entity/Order.java` 存在
2. 在 IDE 重新整理專案（F5 或 Refresh）
3. Clean & Rebuild：`mvn clean compile`

### 錯誤：找不到 selectByUserId 方法

```
The method selectByUserId(String) is undefined for the type OrderMapper
```

**解決方法：**
1. 確認 `OrderMapper.java` 有這個方法宣告
2. 確認 `OrderMapper.xml` 有對應的 `<select id="selectByUserId">`

---

## 📝 完成後的檔案結構

```
entity/
├── Order.java                    ✅ MBG 生成
├── ConsumptionRecord.java        ✅ MBG 生成
└── ContactInquiry.java           ✅ MBG 生成

example/
├── OrderExample.java             ✅ MBG 生成
├── ConsumptionRecordExample.java ✅ MBG 生成
└── ContactInquiryExample.java    ✅ MBG 生成

mapper/
├── OrderMapper.java              ✅ MBG 生成 + 自定義方法
├── ConsumptionRecordMapper.java  ✅ MBG 生成 + 自定義方法
└── ContactInquiryMapper.java     ✅ MBG 生成 + 自定義方法

mapper/ (XML)
├── OrderMapper.xml               ✅ MBG 生成 + 自定義 SQL
├── ConsumptionRecordMapper.xml   ✅ 已完成
└── ContactInquiryMapper.xml      ✅ 已完成
```
