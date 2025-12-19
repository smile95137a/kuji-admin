# kuji-admin 專案架構與開發規範

## 文件用途
本文件描述 kuji-admin 專案的基本架構、技術棧、MyBatis Generator 使用規範以及開發流程，供 AI 編碼代理理解專案架構並遵循開發規範。

---

## 一、專案基本資訊

### 1.1 技術棧
- **Spring Boot**: 3.3.3
- **Java**: 21 (JDK 21)
- **資料庫**: MySQL 8.x
- **ORM 框架**: MyBatis 3.0.5
- **建構工具**: Maven
- **安全框架**: Spring Security
- **JWT**: jjwt 0.9.1
- **其他**: Lombok 1.18.32, Spring AOP, OAuth2 Client

### 1.2 專案結構
```
admin/
├── src/main/java/com/group/admin/
│   ├── AdminApplication.java          # 啟動類
│   ├── aop/                            # AOP 切面（如 GlobalResponseAspect）
│   ├── config/                         # 配置類（SecurityConfig, CorsConfig）
│   ├── controller/                     # Controller 層
│   ├── entity/                         # 實體類（由 MBG 生成）
│   ├── example/                        # Example 查詢類（由 MBG 生成）
│   ├── mapper/                         # Mapper 介面（由 MBG 生成）
│   ├── service/                        # Service 業務邏輯層
│   ├── filter/                         # 過濾器（JWT 驗證等）
│   ├── handler/                        # 全域例外處理器
│   ├── result/                         # 統一回應格式（ApiResponse）
│   ├── util/                           # 工具類（JwtUtil 等）
│   └── generator/                      # MBG 生成器工具
├── src/main/resources/
│   ├── application.yml                 # 主配置檔
│   ├── application-dev.yml             # 開發環境配置
│   ├── application-prod.yml            # 生產環境配置
│   └── mapper/                         # MyBatis XML Mapper（由 MBG 生成）
│       └── generatorConfig.xml         # MBG 配置檔
└── pom.xml                             # Maven 依賴配置
```

---

## 二、MyBatis Generator (MBG) 架構

### 2.1 核心概念
本專案使用 **MyBatis Generator (MBG)** 自動從資料庫生成以下檔案：
- **Entity**: Java 實體類（對應資料表）
- **Mapper Interface**: Mapper 介面（定義 CRUD 方法）
- **Mapper XML**: MyBatis XML 映射檔（SQL 語句）
- **Example**: 查詢條件構造器（選填，本專案有使用）

### 2.2 MBG 配置位置
- **配置檔**: `src/main/resources/mapper/generatorConfig.xml`
- **生成器工具**: `src/main/java/com/group/admin/generator/FullSchemaExampleGenerator.java`

### 2.3 MBG 配置範例
```xml
<!-- generatorConfig.xml 片段 -->
<table tableName="admin_user" 
       enableCountByExample="false" 
       enableUpdateByExample="false"
       enableDeleteByExample="false" 
       enableSelectByExample="false" 
       selectByExampleQueryId="false">
    <generatedKey column="id" sqlStatement="JDBC" identity="true"/>
</table>
```

### 2.4 已生成的檔案範例
- **Entity**: `com/group/admin/entity/AdminUser.java`
- **Mapper**: `com/group/admin/mapper/AdminUserMapper.java`
- **Mapper XML**: `src/main/resources/mapper/AdminUserMapper.xml`
- **Example**: `com/group/admin/example/AdminUserExample.java`

---

## 三、開發流程規範

### 3.1 新增功能的標準流程

#### ❌ 錯誤做法（不要這樣做）
1. 直接寫 Entity、Mapper、Mapper XML
2. 手動撰寫大量 SQL 語句
3. 忽略 Example 類別的存在

#### ✅ 正確做法（必須遵循）
1. **先撰寫 DDL**（資料表定義 SQL）
2. **在資料庫執行 DDL** 建立或修改資料表
3. **執行 MBG 生成器** 自動產生 Entity、Mapper、Example
4. **優先使用 Example 進行查詢**，避免手寫 SQL
5. **只有在逼不得已時** 才在 Mapper XML 中手寫自定義 SQL

### 3.2 DDL 優先原則

#### 為什麼要先寫 DDL？
- 確保資料表結構明確定義
- MBG 能自動生成正確的實體類與欄位
- 避免手動維護實體類與資料表不同步問題

#### DDL 撰寫範例
```sql
-- 範例: 建立 audit_log 表
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL COMMENT '操作者 ID',
    action VARCHAR(50) NOT NULL COMMENT '操作類型',
    description TEXT COMMENT '操作描述',
    ip_address VARCHAR(45),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (operator_id) REFERENCES admin_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作記錄表';
```

#### 執行 DDL 後的步驟
1. 在 MySQL 中執行 DDL
2. 確認資料表建立成功
3. 執行 `FullSchemaExampleGenerator.java` 生成程式碼
4. 檢查生成的 Entity、Mapper、Example 是否正確

---

## 四、Example 使用規範

### 4.1 為什麼優先使用 Example？

#### 優點
- **無需手寫 SQL**: 減少 SQL 語法錯誤
- **類型安全**: 編譯期檢查，避免欄位名錯誤
- **易於維護**: 資料表結構變更時，重新生成即可
- **程式碼可讀性高**: 鏈式呼叫，語意清晰

#### 缺點
- 複雜查詢（JOIN、子查詢）需手寫 SQL
- 效能要求極高時可能需優化 SQL

### 4.2 Example 使用範例

#### 單條件查詢
```java
// 查詢指定 email 的用戶
AdminUserExample example = new AdminUserExample();
example.createCriteria().andEmailEqualTo("admin@example.com");
List<AdminUser> users = adminUserMapper.selectByExample(example);
```

#### 多條件 AND 查詢
```java
// 查詢狀態為 ACTIVE 且角色為 STORE_OWNER 的用戶
AdminUserExample example = new AdminUserExample();
example.createCriteria()
    .andStatusEqualTo("ACTIVE")
    .andRoleEqualTo("STORE_OWNER");
List<AdminUser> users = adminUserMapper.selectByExample(example);
```

#### 多條件 OR 查詢
```java
// 查詢角色為 ADMIN 或 STORE_OWNER 的用戶
AdminUserExample example = new AdminUserExample();
example.createCriteria().andRoleEqualTo("ADMIN");
example.or().andRoleEqualTo("STORE_OWNER");
List<AdminUser> users = adminUserMapper.selectByExample(example);
```

### 4.3 何時需要手寫 SQL？

#### 必須手寫 SQL 的情況
- **關聯查詢（JOIN）**: Example 不支援多表關聯
- **子查詢**: Example 無法處理子查詢
- **聚合函數**: SUM、AVG、GROUP BY 等統計查詢
- **複雜條件**: 需要使用函數或特殊運算子（如 `LIKE`, `IN`, `BETWEEN`）

#### 手寫 SQL 範例
```xml
<!-- 在 Mapper XML 中新增自定義查詢 -->
<select id="selectActiveUsersWithStore" resultType="AdminUser">
    SELECT au.* 
    FROM admin_user au
    INNER JOIN store s ON au.id = s.owner_id
    WHERE au.status = 'ACTIVE'
    AND s.status = 'ACTIVE'
</select>
```

對應 Mapper 介面:
```java
@Mapper
public interface AdminUserMapper {
    // MBG 生成的方法
    List<AdminUser> selectByExample(AdminUserExample example);
    
    // 手寫的自定義方法
    List<AdminUser> selectActiveUsersWithStore();
}
```

---

## 五、Mapper 方法規範

### 5.1 MBG 自動生成的方法
```java
public interface AdminUserMapper {
    int deleteByPrimaryKey(Long id);           // 根據主鍵刪除
    int insert(AdminUser row);                 // 插入完整記錄
    AdminUser selectByPrimaryKey(Long id);     // 根據主鍵查詢
    List<AdminUser> selectAll();               // 查詢全部
    int updateByPrimaryKey(AdminUser row);     // 根據主鍵更新
}
```

### 5.2 自定義方法命名規範
- **查詢**: `selectXxxByYyy` 或 `findXxxByYyy`
- **新增**: `insertXxx` 或 `createXxx`
- **更新**: `updateXxxByYyy`
- **刪除**: `deleteXxxByYyy`
- **統計**: `countXxxByYyy`

範例:
```java
User selectByEmail(@Param("email") String email);
int updateBalance(@Param("id") Long id, @Param("goldDelta") Long goldDelta);
List<AdminUser> selectByStoreId(@Param("storeId") Long storeId);
```

---

## 六、全域回應封裝 (AOP)

### 6.1 GlobalResponseAspect 機制
- 位置: `com/group/admin/aop/GlobalResponseAspect.java`
- 功能: 自動將 Controller 回傳值包裝成 `ApiResponse` 格式
- 影響範圍: 所有 `@RestController` 標註的 Controller

### 6.2 ApiResponse 格式
```java
public class ApiResponse<T> {
    private int code;        // 狀態碼（200 成功，其他失敗）
    private String message;  // 訊息
    private T data;          // 資料
    private long timestamp;  // 時間戳
    private long executionTime; // 執行時間（ms）
}
```

### 6.3 Controller 開發注意事項
- Controller 方法可直接返回物件或 `ResponseEntity<T>`
- AOP 會自動包裝成 `ApiResponse`
- 不需手動建立 `ApiResponse` 物件
- 異常處理由 `GlobalExceptionHandler` 統一處理

---

## 七、JWT 與安全機制

### 7.1 JwtUtil 工具類
- 位置: `com/group/admin/util/JwtUtil.java`
- 功能: 產生與驗證 JWT Token
- 配置: `application.yml` 中的 `jwt.secret`

### 7.2 安全配置
- **SecurityConfig**: `com/group/admin/config/SecurityConfig.java`
- **JWT Filter**: `com/group/admin/filter/` 目錄下的過濾器
- **注意**: 部分 Filter 為註解示範，需檢查是否啟用

---

## 八、開發命令與除錯

### 8.1 常用 Maven 命令
```bash
# 建構與打包（不跑測試）
mvn -DskipTests package

# 開發模式執行（dev profile）
mvn -Pdev spring-boot:run

# 執行測試
mvn test

# 清理並重新建構
mvn clean install
```

### 8.2 執行 MBG 生成器
```bash
# 方法 1: 執行 Java 類別
# 在 IDE 中直接執行 FullSchemaExampleGenerator.java

# 方法 2: 使用 Maven Plugin（若有配置）
mvn mybatis-generator:generate
```

### 8.3 開發環境設定
- 預設 Profile: `dev`
- 資料庫連線: `jdbc:mysql://localhost:3306/kuji`
- 日誌等級: `com.group` 設為 DEBUG

---

## 九、重要規範總結

### 9.1 ✅ 必須遵守
1. **DDL 優先**: 先寫 DDL → 執行 → 生成程式碼
2. **Example 優先**: 能用 Example 就不要手寫 SQL
3. **不要手動修改 MBG 生成的檔案**: Entity、Mapper、Mapper XML（基礎部分）
4. **事務管理**: 涉及資料變更的操作使用 `@Transactional`
5. **異常處理**: 拋出異常讓 `GlobalExceptionHandler` 統一處理

### 9.2 ⚠️ 特別注意
- `GlobalResponseAspect` 會自動封裝回應，Controller 不需重複包裝
- MyBatis 的 namespace 與 resultMap 名稱需與 Mapper 介面對齊
- 修改資料表後必須重新執行 MBG 生成器
- 自定義 SQL 需寫在 Mapper XML 的自定義區塊，避免被覆蓋

### 9.3 ❌ 禁止操作
- 不要繞過 MBG 手動建立 Entity
- 不要在 Service 中直接寫 SQL 字串
- 不要修改 MBG 生成的 `insert`、`update`、`delete` 等基礎方法
- 不要在沒有 DDL 的情況下憑空想像實體類結構

---

## 十、範例工作流程

### 範例: 新增 `audit_log` 功能

#### 步驟 1: 撰寫 DDL
```sql
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### 步驟 2: 在 MySQL 執行 DDL
```bash
mysql -u root -p kuji < audit_log.sql
```

#### 步驟 3: 在 `generatorConfig.xml` 新增表配置
```xml
<table tableName="audit_log" 
       enableCountByExample="false" 
       enableUpdateByExample="false"
       enableDeleteByExample="false" 
       enableSelectByExample="false">
    <generatedKey column="id" sqlStatement="JDBC" identity="true"/>
</table>
```

#### 步驟 4: 執行 MBG 生成器
```bash
# 在 IDE 中執行 FullSchemaExampleGenerator.java
```

#### 步驟 5: 使用 Example 查詢
```java
@Service
public class AuditLogService {
    @Autowired
    private AuditLogMapper auditLogMapper;
    
    public List<AuditLog> getLogsByOperator(Long operatorId) {
        AuditLogExample example = new AuditLogExample();
        example.createCriteria().andOperatorIdEqualTo(operatorId);
        return auditLogMapper.selectByExample(example);
    }
}
```

---

## 十一、疑難排解

### Q1: MBG 生成後編譯失敗？
**A**: 檢查 DDL 中的欄位類型是否正確，重新生成並清理快取 (`mvn clean`)

### Q2: Example 查詢沒有結果？
**A**: 檢查條件是否正確，確認資料庫中有符合條件的資料

### Q3: 自定義 SQL 被 MBG 覆蓋？
**A**: 自定義 SQL 應寫在 Mapper XML 的底部，並加上註解標記，避免被覆蓋

### Q4: JWT 驗證失敗？
**A**: 檢查 `jwt.secret` 配置，確認 Token 簽名正確

---

## 十二、參考資料

- MyBatis Generator 官方文件: https://mybatis.org/generator/
- Spring Boot 官方文件: https://spring.io/projects/spring-boot
- 專案 Copilot 指引: `.github/copilot-instructions.md`

---

**最後更新**: 2025-12-13
