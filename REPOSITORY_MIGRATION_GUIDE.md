# Repository 架構遷移指南

## ✅ 已完成的工作

### 1. 建立 Repository 層
所有自定義查詢方法已從 Mapper 移到獨立的 Repository：

```
repository/
├── DistrictRepository.java          ← 行政區自定義查詢
├── MarqueeRepository.java            ← 跑馬燈自定義查詢
├── UserAddressRepository.java        ← 使用者地址自定義查詢
├── EmailLogRepository.java           ← 郵件日誌自定義查詢
├── SystemLogRepository.java          ← 系統日誌自定義查詢
├── ReportSnapshotRepository.java     ← 報表快照自定義查詢
├── ReferralCodeRepository.java       ← 推薦碼自定義查詢
└── ReferralRecordRepository.java     ← 推薦記錄自定義查詢
```

### 2. 優點
- ✅ **不會被 MBG 覆蓋**：Repository 在獨立目錄，MBG 不會動到
- ✅ **職責分離**：Mapper = 基本 CRUD，Repository = 自定義查詢
- ✅ **易於維護**：自定義方法集中管理
- ✅ **可以隨時執行 MBG**：不用擔心自定義方法被刪除

## 📋 待執行步驟

### 步驟 1：執行 SQL（新增缺少的欄位）

**檔案**：`add-missing-referral-columns.sql`

**執行方式**：
1. 開啟 DBeaver / MySQL Workbench / Navicat
2. 連接到 AWS RDS：`database-1.c43csoqulekw.ap-southeast-2.rds.amazonaws.com`
3. 選擇 database：`kuji`
4. 開啟 `add-missing-referral-columns.sql` 並執行

或執行：`execute-sql-on-rds.bat`（會自動開啟 Notepad）

### 步驟 2：執行 MyBatis Generator

**方式 1（推薦）**：
```bash
run-mbg.bat
```

**方式 2**：
```bash
mvn mybatis-generator:generate
```

這會重新生成：
- Entity（新增 store_id, description, user_id, used_code, referred_at 欄位）
- Mapper（只有基本 CRUD）
- Example
- XML

⚠️ **不用擔心自定義方法被刪除**，因為它們在 `repository/` 目錄！

### 步驟 3：更新 Service 層（自動化腳本）

需要更新以下 Service 的 import 和注入：

#### ✅ 已更新
- DistrictServiceImpl

#### ⏳ 待更新
- MarqueeServiceImpl
- EmailServiceImpl  
- SystemLogServiceImpl
- UserAddressServiceImpl
- ReportServiceImpl
- ReferralCodeServiceImpl

**更新內容**：
```java
// 舊的
private final XxxMapper xxxMapper;

// 新的  
private final XxxRepository xxxRepository;  // 基本 CRUD 用 Mapper
private final XxxMapper xxxMapper;           // 自定義查詢用 Repository
```

### 步驟 4：編譯驗證

```bash
mvn clean compile -DskipTests
```

## 🎯 未來開發流程

### 新增自定義查詢方法

**❌ 錯誤（舊做法）**：
```java
// UserMapper.java ← 會被 MBG 覆蓋！
@Select("...")
List<User> myCustomMethod();
```

**✅ 正確（新做法）**：
```java
// UserRepository.java ← 不會被 MBG 覆蓋！
@Mapper
public interface UserRepository {
    @Select("...")
    List<User> myCustomMethod();
}
```

### Service 層使用

```java
@Service
public class UserServiceImpl {
    private final UserMapper userMapper;           // 基本 CRUD
    private final UserRepository userRepository;   // 自定義查詢
    
    public void example() {
        userMapper.insert(user);                    // MBG 生成的
        userRepository.myCustomMethod();            // 自定義的
    }
}
```

## 📁 檔案清單

### 已建立
- ✅ `add-missing-referral-columns.sql` - SQL 補欄位腳本
- ✅ `execute-sql-on-rds.bat` - SQL 執行輔助腳本
- ✅ `run-mbg.bat` - MBG 執行腳本
- ✅ `repository/DistrictRepository.java` - 8 個 Repository
- ✅ `DATABASE_FIX_GUIDE.md` - 之前的指南（可刪除）
- ✅ `fix-referral-tables.sql` - 之前的腳本（不需要了）

### 待建立
- ⏳ Service 層更新腳本（可手動或自動）

## ⚠️ 注意事項

1. **不要再在 Mapper 寫自定義方法**
2. **執行 MBG 前不用再擔心**
3. **新的自定義方法都寫在 Repository**
4. **Mapper 只用於基本 CRUD**

## 🔄 遷移對照表

| 舊檔案 (Mapper) | 新檔案 (Repository) | 狀態 |
|----------------|-------------------|------|
| DistrictMapper | DistrictRepository | ✅ 已建立 |
| MarqueeMapper | MarqueeRepository | ✅ 已建立 |
| UserAddressMapper | UserAddressRepository | ✅ 已建立 |
| EmailLogMapper | EmailLogRepository | ✅ 已建立 |
| SystemLogMapper | SystemLogRepository | ✅ 已建立 |
| ReportSnapshotMapper | ReportSnapshotRepository | ✅ 已建立 |
| ReferralCodeMapper | ReferralCodeRepository | ✅ 已建立 |
| ReferralRecordMapper | ReferralRecordRepository | ✅ 已建立 |
