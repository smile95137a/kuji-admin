# kuji-admin 專案 Prompt 檔案總覽

## 📋 Prompt 檔案清單

### 1. 架構與開發規範（必讀）
- **`architecture-guide.prompt.md`** - 專案架構、MBG 使用規範、開發流程

### 2. 功能需求 Prompt
- **`game-management.prompt.md`** - 抽獎遊戲管理功能需求
- **`permissions-rbac.prompt.md`** - 權限管理與商品管理功能需求  
- **`store-management.prompt.md`** - 店家資料管理功能需求
- **`store-account-management.prompt.md`** - 店家後台帳號管理功能需求
- **`user-member-system.prompt.md`** - 前台與後台會員系統功能需求

---

## 🎯 使用指南

### AI 編碼代理使用順序
1. **第一步**: 閱讀 `architecture-guide.prompt.md` 了解專案架構
2. **第二步**: 根據需求閱讀對應的功能 prompt
3. **第三步**: 遵循 DDL → MBG → Example 的開發流程

### 關鍵開發原則
1. ✅ **DDL 優先**: 先撰寫資料表定義 SQL
2. ✅ **MBG 生成**: 執行 MyBatis Generator 自動產生程式碼
3. ✅ **Example 優先**: 能用 Example 就不手寫 SQL
4. ✅ **事務管理**: 資料變更操作使用 `@Transactional`
5. ❌ **禁止**: 手動建立 Entity、繞過 MBG

---

## 📂 MyBatis Generator (MBG) 快速參考

### MBG 配置位置
- 配置檔: `src/main/resources/mapper/generatorConfig.xml`
- 生成器: `src/main/java/com/group/admin/generator/FullSchemaExampleGenerator.java`

### MBG 生成檔案
- **Entity**: `com/group/admin/entity/*.java`
- **Mapper**: `com/group/admin/mapper/*.java`  
- **Mapper XML**: `src/main/resources/mapper/*.xml`
- **Example**: `com/group/admin/example/*Example.java`

### Example 使用範例
```java
// 單條件查詢
AdminUserExample example = new AdminUserExample();
example.createCriteria().andEmailEqualTo("admin@example.com");
List<AdminUser> users = adminUserMapper.selectByExample(example);

// 多條件 AND
example.createCriteria()
    .andStatusEqualTo("ACTIVE")
    .andRoleEqualTo("ADMIN");

// 多條件 OR
example.createCriteria().andRoleEqualTo("ADMIN");
example.or().andRoleEqualTo("STORE_OWNER");
```

---

## 🔄 標準開發流程

### 新增功能完整流程
```
1. 撰寫 DDL (CREATE TABLE ...)
   ↓
2. 在 MySQL 執行 DDL
   ↓
3. 在 generatorConfig.xml 新增 <table> 配置
   ↓
4. 執行 FullSchemaExampleGenerator.java
   ↓
5. 檢查生成的 Entity、Mapper、Example
   ↓
6. 使用 Example 撰寫 Service 邏輯
   ↓
7. (必要時) 在 Mapper XML 新增自定義 SQL
```

---

## ⚠️ 常見錯誤

### ❌ 錯誤做法
- 直接手寫 Entity 類別
- 跳過 DDL 直接寫程式碼
- 大量手寫 SQL 而不用 Example
- 修改 MBG 生成的基礎方法                                                  

### ✅ 正確做法
- DDL → MBG → Example 流程
- 優先使用 Example 查詢
- 只在複雜查詢（JOIN、子查詢）時手寫 SQL
- 自定義 SQL 寫在 Mapper XML 底部

---

## 📌 快速檢查清單

開始開發前請確認:
- [ ] 已閱讀 `architecture-guide.prompt.md`
- [ ] 已撰寫 DDL 並在資料庫執行
- [ ] 已更新 `generatorConfig.xml`
- [ ] 已執行 MBG 生成器
- [ ] 已檢查生成的檔案無誤
- [ ] 了解何時使用 Example、何時手寫 SQL

---

**最後更新**: 2025-12-13  
**專案**: kuji-admin  
**版本**: Spring Boot 3.3.3 + MyBatis 3.0.5 + Java 21
