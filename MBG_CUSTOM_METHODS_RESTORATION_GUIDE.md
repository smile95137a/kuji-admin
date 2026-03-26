# 🔧 MBG 自定義方法修復指南

## 問題描述

執行 `MBGAutoRunner` 後，MyBatis Generator 會重新生成 Entity、Example、Mapper.xml，但以下自定義方法會遺失：

| Mapper | 遺失的自定義方法 |
|--------|----------------|
| `ConsumptionRecordMapper` | `selectByUserId(String userId)`, `selectAll()` |
| `ContactInquiryMapper` | `selectAll()`, `updateStatus(ContactInquiry inquiry)` |
| `OrderMapper` | `selectByUserId(String userId)` |

## 解決方案：在 Mapper XML 添加自定義查詢

### 1. ConsumptionRecordMapper.xml

**檔案位置**：`src/main/resources/mapper/ConsumptionRecordMapper.xml`

**在 `</mapper>` 標籤前添加**：

```xml
  <!-- 自定義查詢：根據用戶 ID 查詢 -->
  <select id="selectByUserId" parameterType="java.lang.String" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from consumption_record
    where user_id = #{userId,jdbcType=VARCHAR}
    order by created_at DESC
  </select>

  <!-- 自定義查詢：查詢所有記錄 -->
  <select id="selectAll" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from consumption_record
    order by created_at DESC
  </select>
```

### 2. ContactInquiryMapper.xml

**檔案位置**：`src/main/resources/mapper/ContactInquiryMapper.xml`

**在 `</mapper>` 標籤前添加**：

```xml
  <!-- 自定義查詢：查詢所有記錄 -->
  <select id="selectAll" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from contact_inquiry
    order by created_at DESC
  </select>

  <!-- 自定義更新：更新狀態 -->
  <update id="updateStatus" parameterType="com.group.admin.entity.ContactInquiry">
    update contact_inquiry
    set status = #{status,jdbcType=VARCHAR},
        remark = #{remark,jdbcType=VARCHAR},
        processed_by = #{processedBy,jdbcType=VARCHAR},
        processed_at = #{processedAt,jdbcType=TIMESTAMP},
        updated_at = #{updatedAt,jdbcType=TIMESTAMP}
    where id = #{id,jdbcType=VARCHAR}
  </update>
```

### 3. OrderMapper.xml

**檔案位置**：`src/main/resources/mapper/OrderMapper.xml`

**在 `</mapper>` 標籤前添加**：

```xml
  <!-- 自定義查詢：根據用戶 ID 查詢 -->
  <select id="selectByUserId" parameterType="java.lang.String" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from `order`
    where user_id = #{userId,jdbcType=VARCHAR}
    order by created_at DESC
  </select>
```

---

## 在 Mapper 介面添加方法簽名

### 1. ConsumptionRecordMapper.java

**檔案位置**：`src/main/java/com/group/admin/mapper/ConsumptionRecordMapper.java`

**在介面中添加**：

```java
import java.util.List;

public interface ConsumptionRecordMapper {
    // ... MBG 生成的方法 ...
    
    // 自定義方法
    List<ConsumptionRecord> selectByUserId(String userId);
    
    List<ConsumptionRecord> selectAll();
}
```

### 2. ContactInquiryMapper.java

**檔案位置**：`src/main/java/com/group/admin/mapper/ContactInquiryMapper.java`

**在介面中添加**：

```java
import java.util.List;

public interface ContactInquiryMapper {
    // ... MBG 生成的方法 ...
    
    // 自定義方法
    List<ContactInquiry> selectAll();
    
    int updateStatus(ContactInquiry inquiry);
}
```

### 3. OrderMapper.java

**檔案位置**：`src/main/java/com/group/admin/mapper/OrderMapper.java`

**在介面中添加**：

```java
import java.util.List;

public interface OrderMapper {
    // ... MBG 生成的方法 ...
    
    // 自定義方法
    List<Order> selectByUserId(String userId);
}
```

---

## 快速修復腳本

執行以下命令自動添加自定義方法：

```bash
# 1. 檢查 Mapper XML 是否存在
cd src/main/resources/mapper

# 2. 手動編輯每個 XML，或使用以下 PowerShell 腳本
```

**PowerShell 自動化腳本**（`add-custom-methods.ps1`）：

```powershell
# 添加 ConsumptionRecordMapper 自定義方法
$file = "src/main/resources/mapper/ConsumptionRecordMapper.xml"
$customMethods = @"

  <!-- 自定義查詢：根據用戶 ID 查詢 -->
  <select id="selectByUserId" parameterType="java.lang.String" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from consumption_record
    where user_id = #{userId,jdbcType=VARCHAR}
    order by created_at DESC
  </select>

  <!-- 自定義查詢：查詢所有記錄 -->
  <select id="selectAll" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from consumption_record
    order by created_at DESC
  </select>
"@

$content = Get-Content $file -Raw
$content = $content -replace '</mapper>', "$customMethods`n</mapper>"
Set-Content $file $content

Write-Host "✅ ConsumptionRecordMapper.xml 自定義方法已添加"

# 添加 ContactInquiryMapper 自定義方法
$file = "src/main/resources/mapper/ContactInquiryMapper.xml"
$customMethods = @"

  <!-- 自定義查詢：查詢所有記錄 -->
  <select id="selectAll" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from contact_inquiry
    order by created_at DESC
  </select>

  <!-- 自定義更新：更新狀態 -->
  <update id="updateStatus" parameterType="com.group.admin.entity.ContactInquiry">
    update contact_inquiry
    set status = #{status,jdbcType=VARCHAR},
        remark = #{remark,jdbcType=VARCHAR},
        processed_by = #{processedBy,jdbcType=VARCHAR},
        processed_at = #{processedAt,jdbcType=TIMESTAMP},
        updated_at = #{updatedAt,jdbcType=TIMESTAMP}
    where id = #{id,jdbcType=VARCHAR}
  </update>
"@

$content = Get-Content $file -Raw
$content = $content -replace '</mapper>', "$customMethods`n</mapper>"
Set-Content $file $content

Write-Host "✅ ContactInquiryMapper.xml 自定義方法已添加"

# 添加 OrderMapper 自定義方法
$file = "src/main/resources/mapper/OrderMapper.xml"
$customMethods = @"

  <!-- 自定義查詢：根據用戶 ID 查詢 -->
  <select id="selectByUserId" parameterType="java.lang.String" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from ``order``
    where user_id = #{userId,jdbcType=VARCHAR}
    order by created_at DESC
  </select>
"@

$content = Get-Content $file -Raw
$content = $content -replace '</mapper>', "$customMethods`n</mapper>"
Set-Content $file $content

Write-Host "✅ OrderMapper.xml 自定義方法已添加"

Write-Host "`n🎉 所有自定義方法已添加完成！"
Write-Host "📋 下一步：編譯專案驗證"
Write-Host "   mvn clean compile -DskipTests"
```

---

## 驗證步驟

1. 確認 XML 文件格式正確
2. 執行編譯：`mvn clean compile -DskipTests`
3. 檢查編譯錯誤
4. 啟動應用程式測試

---

## 預防措施

### 方案 A：將自定義方法寫入模板

修改 `MBGAutoRunner.java`，在生成 XML 時自動添加自定義方法（較複雜）。

### 方案 B：使用獨立的 Mapper 擴展

創建 `ConsumptionRecordMapperExt.java`，繼承 MBG 生成的 Mapper（推薦）：

```java
@Mapper
public interface ConsumptionRecordMapperExt extends ConsumptionRecordMapper {
    List<ConsumptionRecord> selectByUserId(String userId);
    List<ConsumptionRecord> selectAll();
}
```

然後創建 `ConsumptionRecordMapperExt.xml` 存放自定義查詢。

---

## 總結

- ✅ MBG 生成基礎 CRUD
- ✅ 自定義方法手動添加到 XML + Mapper 介面
- ✅ 每次執行 MBGAutoRunner 後需要重新添加自定義方法
- 💡 建議：使用 Ext 繼承模式分離自定義邏輯
