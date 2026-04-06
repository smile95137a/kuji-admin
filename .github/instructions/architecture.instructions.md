---
description: "Use when working on project architecture, MyBatis Generator (MBG), Example queries, or setting up new database tables. Covers DDL-first workflow, MBG configuration, and Mapper conventions."
applyTo: ["**/entity/**", "**/example/**", "**/mapper/**", "**/generator/**", "**generatorConfig.xml"]
---

# 專案架構與開發規範

## 一、技術棧
- Spring Boot 3.3.3 + Java 21 + MyBatis 3.0.5
- MySQL 8.x + Maven + Spring Security + JWT (jjwt 0.9.1)
- Lombok 1.18.32 + Spring AOP + OAuth2 Client

## 二、專案結構
```
admin/src/main/java/com/group/admin/
├── AdminApplication.java          # 啟動類
├── aop/                            # AOP 切面
├── config/                         # 配置類
├── controller/                     # Controller 層
├── entity/                         # 實體類（MBG 生成）
├── example/                        # Example 查詢類（MBG 生成）
├── mapper/                         # Mapper 介面（MBG 生成）
├── service/                        # Service 業務邏輯層
├── filter/                         # JWT 過濾器
├── handler/                        # 全域例外處理器
├── result/                         # 統一回應格式
├── util/                           # 工具類
└── generator/                      # MBG 生成器工具
```

## 三、MyBatis Generator (MBG)

### 配置位置
- 配置檔: `src/main/resources/mapper/generatorConfig.xml`
- 生成器: `src/main/java/com/group/admin/generator/FullSchemaExampleGenerator.java`

### 生成檔案
- Entity → `entity/*.java`
- Mapper → `mapper/*.java` + `resources/mapper/*.xml`
- Example → `example/*Example.java`

### 開發流程（必須遵循）
1. **先撰寫 DDL** → 在 MySQL 執行
2. **執行 MBG 生成器** 自動產生 Entity/Mapper/Example
3. **優先使用 Example 查詢**，避免手寫 SQL
4. 只有 JOIN、子查詢、聚合函數才手寫 SQL

### ⚠️ 禁止操作
- 不要繞過 MBG 手動建立 Entity
- 不要修改 MBG 生成的基礎方法
- 不要在沒有 DDL 的情況下憑空想像實體類結構

## 四、Example 使用範例

```java
// 單條件查詢
AdminUserExample example = new AdminUserExample();
example.createCriteria().andEmailEqualTo("admin@example.com");

// 多條件 AND
example.createCriteria()
    .andStatusEqualTo("ACTIVE")
    .andRoleEqualTo("STORE_OWNER");

// OR 查詢
example.createCriteria().andRoleEqualTo("ADMIN");
example.or().andRoleEqualTo("STORE_OWNER");
```

## 五、Mapper 方法規範

### MBG 自動生成
```java
int deleteByPrimaryKey(Long id);
int insert(AdminUser row);
AdminUser selectByPrimaryKey(Long id);
List<AdminUser> selectAll();
int updateByPrimaryKey(AdminUser row);
```

### 自定義命名
- 查詢: `selectXxxByYyy` / `findXxxByYyy`
- 新增: `insertXxx` / `createXxx`
- 更新: `updateXxxByYyy`
- 刪除: `deleteXxxByYyy`

## 六、全域回應封裝 (AOP)
- `GlobalResponseAspect` 自動將 Controller 回傳值包裝成 `ApiResponse`
- Controller 直接返回物件或 `ResponseEntity<T>` 即可
- 不需手動建立 `ApiResponse`

## 七、常用命令
```bash
mvn clean package -DskipTests    # 建構打包
mvn spring-boot:run              # 開發模式
mvn mybatis-generator:generate   # MBG 生成
```
