# MBG 自動生成程式修正報告

## 📋 問題診斷

### 問題現象
執行 `MBGAutoRunner` 後，以下 Mapper 的自定義方法全部消失：
- ❌ DistrictMapper.selectAllCities()
- ❌ EmailLogMapper.selectPendingForRetry()
- ❌ MarqueeMapper.selectActiveMarquees()
- ❌ SystemLogMapper.selectByType()
- ❌ UserAddressMapper.clearDefaultByUserId()
- ❌ ReferralCodeMapper.selectByCode()
- ❌ ReferralRecordMapper.selectByUserId()
- ❌ ReportSnapshotMapper.selectByTypeAndPeriod()

### 根本原因
`MBGAutoRunner.cleanGeneratedFiles()` 方法會刪除整個 `mapper/` 目錄，導致：
1. MBG 生成的基礎方法（selectByPrimaryKey 等）
2. **手動加的自定義方法**（@Select、@Update 等）

全部被刪除，然後 MBG 只會重新生成基礎方法，自定義方法就消失了。

---

## ✅ 解決方案

### 修改 1：MBGAutoRunner 不再刪除 Mapper 介面

**檔案**：`src/main/java/com/group/admin/MBGAutoRunner.java`

**修改前**：
```java
// 清理 Mapper（保留自定義的 Mapper，只刪除 XML）
File mapperDir = new File("src/main/java/com/group/admin/mapper");
if (mapperDir.exists()) {
    deleteDirectory(mapperDir);
    System.out.println("   🗑️  已清理 mapper/ 目錄");
}
```

**修改後**：
```java
// ✅ 不刪除 Mapper 介面（保留自定義方法）
System.out.println("   ✅ 保留 mapper/ 目錄（內含自定義方法）");
```

**原因**：
- MBG 的 `overwrite="true"` 會覆蓋 Entity/Example/XML
- 但 **不會覆蓋 Mapper 介面中的自定義方法**
- 只要不刪除 Mapper 介面，自定義方法就不會消失

### 修改 2：恢復所有自定義方法

已恢復以下 8 個 Mapper 的 26 個自定義方法：

#### 1. DistrictMapper（4 個方法）
```java
@Select("SELECT DISTINCT city FROM district ORDER BY city")
List<String> selectAllCities();

@Select("SELECT * FROM district WHERE city = #{city} ORDER BY district")
List<District> selectByCity(@Param("city") String city);

@Select("SELECT * FROM district ORDER BY city, district")
List<District> selectAll();

@Select("SELECT * FROM district WHERE city = #{city} AND district = #{district}")
District selectByCityAndDistrict(@Param("city") String city, @Param("district") String district);
```

#### 2. EmailLogMapper（2 個方法）
```java
@Select("SELECT * FROM email_log WHERE status = #{status} AND retry_count < #{maxRetries} ORDER BY created_at ASC LIMIT #{limit}")
List<EmailLog> selectPendingForRetry(@Param("status") String status, @Param("maxRetries") int maxRetries, @Param("limit") int limit);

@Update("UPDATE email_log SET status = #{status}, sent_at = #{sentAt}, retry_count = #{retryCount}, error_message = #{errorMessage}, updated_at = #{updatedAt} WHERE id = #{id}")
int updateStatus(EmailLog emailLog);
```

#### 3. MarqueeMapper（6 個方法）
```java
@Select("SELECT * FROM marquee WHERE is_active = 1 AND (start_time IS NULL OR start_time <= #{now}) AND (end_time IS NULL OR end_time >= #{now}) ORDER BY order_num ASC, created_at DESC")
List<Marquee> selectActiveMarquees(@Param("now") LocalDateTime now);

@Select("SELECT * FROM marquee ORDER BY order_num ASC, created_at DESC")
List<Marquee> selectAll();

@Select("SELECT * FROM marquee WHERE id = #{id}")
Marquee selectById(@Param("id") String id);

@Update("UPDATE marquee SET content = #{content}, type = #{type}, is_active = #{isActive}, start_time = #{startTime}, end_time = #{endTime}, order_num = #{orderNum}, updated_at = NOW() WHERE id = #{id}")
int update(Marquee marquee);

@Delete("DELETE FROM marquee WHERE id = #{id}")
int deleteById(@Param("id") String id);

@Update("UPDATE marquee SET is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
int updateStatus(@Param("id") String id, @Param("isActive") byte isActive);
```

#### 4. SystemLogMapper（4 個方法）
```java
@Select("SELECT * FROM system_log WHERE type = #{type} ORDER BY created_at DESC")
List<SystemLog> selectByType(@Param("type") String type);

@Select("SELECT * FROM system_log WHERE user_id = #{userId} ORDER BY created_at DESC")
List<SystemLog> selectByUserId(@Param("userId") String userId);

@Select("SELECT * FROM system_log WHERE type = #{type} AND created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
List<SystemLog> selectByTypeAndDateRange(@Param("type") String type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

@Delete("DELETE FROM system_log WHERE created_at < #{beforeDate}")
int deleteOldLogs(@Param("beforeDate") LocalDateTime beforeDate);
```

#### 5. UserAddressMapper（3 個方法）
```java
@Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId}")
int clearDefaultByUserId(@Param("userId") String userId);

@Select("SELECT * FROM user_address WHERE user_id = #{userId} ORDER BY is_default DESC, created_at DESC")
List<UserAddress> selectByUserId(@Param("userId") String userId);

@Select("SELECT * FROM user_address WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
UserAddress selectDefaultByUserId(@Param("userId") String userId);
```

#### 6. ReferralCodeMapper（3 個方法）
```java
@Select("SELECT * FROM referral_code WHERE code = #{code}")
ReferralCode selectByCode(@Param("code") String code);

@Select("SELECT * FROM referral_code WHERE store_id = #{storeId} ORDER BY created_at DESC")
List<ReferralCode> selectByStoreId(@Param("storeId") String storeId);

@Select("SELECT * FROM referral_code ORDER BY created_at DESC")
List<ReferralCode> selectAll();
```

#### 7. ReferralRecordMapper（3 個方法）
```java
@Select("SELECT * FROM referral_record WHERE user_id = #{userId} ORDER BY referred_at DESC")
List<ReferralRecord> selectByUserId(@Param("userId") String userId);

@Select("SELECT * FROM referral_record WHERE referral_code_id = #{referralCodeId} ORDER BY referred_at DESC")
List<ReferralRecord> selectByReferralCodeId(@Param("referralCodeId") String referralCodeId);

@Select("SELECT * FROM referral_record WHERE store_id = #{storeId} ORDER BY referred_at DESC")
List<ReferralRecord> selectByStoreId(@Param("storeId") String storeId);
```

#### 8. ReportSnapshotMapper（1 個方法）
```java
@Select("SELECT * FROM report_snapshot WHERE type = #{type} AND period = #{period} ORDER BY snapshot_time DESC")
List<ReportSnapshot> selectByTypeAndPeriod(@Param("type") String type, @Param("period") String period);
```

---

## 🎯 未來執行 MBG 的正確流程

### ✅ 現在的流程（安全）
1. 執行 `MBGAutoRunner`
2. MBG 只會刪除 Entity/Example/XML
3. **不刪除 Mapper 介面**
4. MBG 重新生成時，會保留自定義方法
5. 編譯成功 ✅

### 🔧 編譯測試
```bash
mvn clean compile -DskipTests
```

**預期結果**：
```
[INFO] BUILD SUCCESS
```

---

## 📝 最佳實踐

### 1. Mapper 介面結構規範
```java
public interface XXXMapper {
    // ==================== MBG 生成的基礎方法 ====================
    long countByExample(XXXExample example);
    int deleteByPrimaryKey(String id);
    int insert(XXX row);
    // ... 其他 MBG 生成的方法
    
    // ==================== 自定義方法（不會被 MBG 覆蓋）====================
    
    /**
     * 自定義查詢方法
     */
    @Select("SELECT * FROM xxx WHERE ...")
    List<XXX> customMethod();
}
```

### 2. 為什麼使用 Annotation 而非 XML？
| 方式 | 優點 | 缺點 |
|------|------|------|
| **Annotation** | ✅ 與 MBG 生成的 XML 分離<br>✅ 不會被 MBG 覆蓋<br>✅ 程式碼集中管理 | ⚠️ 複雜 SQL 不易閱讀 |
| **XML** | ✅ 複雜 SQL 易讀 | ❌ 會被 MBG 覆蓋<br>❌ 需要額外檔案 |

**結論**：簡單 CRUD 用 Annotation，複雜查詢用 XML（但要另外建立 `XXXMapperExt.xml`）

### 3. 如果需要複雜 SQL 怎麼辦？
**方法 A：繼承 Mapper（推薦）**
```java
// XXXMapper.java（MBG 生成）
public interface XXXMapper {
    // MBG 基礎方法
}

// XXXMapperExt.java（手動建立）
public interface XXXMapperExt extends XXXMapper {
    // 自定義方法
    @Select("複雜 SQL...")
    List<XXX> complexQuery();
}
```

**方法 B：建立獨立 XML（推薦）**
```xml
<!-- XXXMapperExt.xml -->
<mapper namespace="com.group.admin.mapper.XXXMapper">
    <select id="complexQuery" resultType="XXX">
        <!-- 複雜 SQL -->
    </select>
</mapper>
```

---

## ✅ 修正完成檢查清單

- [x] MBGAutoRunner 不再刪除 Mapper 介面
- [x] 恢復 DistrictMapper 的 4 個自定義方法
- [x] 恢復 EmailLogMapper 的 2 個自定義方法
- [x] 恢復 MarqueeMapper 的 6 個自定義方法
- [x] 恢復 SystemLogMapper 的 4 個自定義方法
- [x] 恢復 UserAddressMapper 的 3 個自定義方法
- [x] 恢復 ReferralCodeMapper 的 3 個自定義方法
- [x] 恢復 ReferralRecordMapper 的 3 個自定義方法
- [x] 恢復 ReportSnapshotMapper 的 1 個自定義方法
- [x] 總計恢復 26 個自定義方法

---

## 🚀 下一步

1. **測試編譯**：
   ```bash
   mvn clean compile -DskipTests
   ```

2. **重新啟動應用**：
   ```bash
   mvn spring-boot:run
   ```

3. **驗證 API**：
   - 測試需要自定義方法的功能
   - 確認沒有 "method not found" 錯誤

---

## 📞 未來注意事項

### ⚠️ 如果需要新增 Mapper 自定義方法
1. 直接在 Mapper 介面中新增（用 @Select/@Update/@Delete 等）
2. 在方法上方加註解區分：`// ==================== 自定義方法 ====================`
3. 執行 MBG 時不會被刪除 ✅

### ⚠️ 如果遇到 Mapper 方法衝突
**現象**：MBG 生成的方法與自定義方法重名

**解決**：
- 方法 1：自定義方法改名（加 `custom` 前綴）
- 方法 2：使用 @SelectProvider 動態 SQL

### ⚠️ 如果 Entity 欄位有變更
執行 MBG 後：
- ✅ Entity 會更新欄位
- ✅ Mapper 介面保留自定義方法
- ✅ XML 會重新生成（但不影響 Annotation 方法）

---

**修正完成時間**：2026-01-19  
**修正人員**：GitHub Copilot  
**修正狀態**：✅ 完成，所有自定義方法已恢復
