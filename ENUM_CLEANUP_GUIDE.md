# 🧹 Enum 重複清理指南

## 問題說明

目前專案中存在 **重複的 Enum 類型**，這會造成：
- ❌ 前端不知道該使用哪種 Enum
- ❌ 後端維護時會覺得多餘
- ❌ 增加編譯和維護成本

## 發現的重複 Enum

| 正在使用（保留） | 未使用（刪除） | 用途 |
|---|---|---|
| ✅ `LotteryStatusEnum.java` | ❌ `LotteryStatus.java` | 商品狀態 |
| ✅ `LotteryCategoryEnum.java` | ❌ `LotteryCategory.java` | 商品分類 |
| ✅ `LotterySubCategoryEnum.java` | ❌ `LotterySubCategory.java` | 商品子分類 |

## 使用情況統計

### LotteryStatusEnum（正在使用）
- ✅ `LotteryServiceImpl.java` - 18 處引用
- ✅ `LotteryPrizeServiceImpl.java` - 8 處引用
- **總計：26 處使用**

### LotteryCategoryEnum（正在使用）
- ✅ `LotteryServiceImpl.java` - 3 處引用
- **總計：3 處使用**

### LotterySubCategoryEnum（正在使用）
- 📋 預留給未來使用（自定義扭蛋子分類）

### 未使用的 Enum
- ❌ `LotteryStatus.java` - **0 處引用**
- ❌ `LotteryCategory.java` - **0 處引用**
- ❌ `LotterySubCategory.java` - **0 處引用**

## 清理步驟

### 方法 1：手動刪除（推薦）

1. 開啟檔案總管，導航到：
   ```
   c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\enums
   ```

2. 刪除以下 3 個檔案：
   - ❌ `LotteryStatus.java`
   - ❌ `LotteryCategory.java`
   - ❌ `LotterySubCategory.java`

3. 確認保留以下檔案：
   - ✅ `LotteryStatusEnum.java`
   - ✅ `LotteryCategoryEnum.java`
   - ✅ `LotterySubCategoryEnum.java`

### 方法 2：使用清理腳本

**步驟 1：關閉所有正在執行的命令提示字元視窗**

**步驟 2：開啟新的命令提示字元，執行：**
```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
clean-duplicate-enums.bat
```

或使用 PowerShell：
```powershell
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
powershell -ExecutionPolicy Bypass -File clean-duplicate-enums.ps1
```

### 方法 3：使用 VS Code

1. 在 VS Code 左側檔案樹中找到這 3 個檔案
2. 右鍵點擊 → 刪除
3. 確認刪除

## 驗證清理結果

清理完成後，執行以下命令驗證：

```bash
# 編譯專案（確保沒有編譯錯誤）
mvn clean compile

# 如果沒有錯誤，表示清理成功！
```

## 統一後的 Enum 規範

### ✅ 保留的 Enum 格式

**1. LotteryStatusEnum**
```java
public enum LotteryStatusEnum {
    DRAFT("DRAFT", "草稿"),
    OFF_SHELF("OFF_SHELF", "已下架"),
    ON_SHELF("ON_SHELF", "已上架"),
    IN_PROGRESS("IN_PROGRESS", "抽獎中"),
    ENDED("ENDED", "已結束"),
    FORCED_OFF("FORCED_OFF", "強制下架");
    
    private final String code;  // 資料庫存的值
    private final String name;  // 中文名稱
}
```

**2. LotteryCategoryEnum**
```java
public enum LotteryCategoryEnum {
    OFFICIAL_ICHIBAN("OFFICIAL_ICHIBAN", "官方一番賞"),
    GACHA("GACHA", "扭蛋"),
    TRADING_CARD("TRADING_CARD", "卡牌"),
    CUSTOM_GACHA("CUSTOM_GACHA", "自製賞");
    
    private final String code;  // 資料庫存的值
    private final String name;  // 中文名稱
}
```

**3. LotterySubCategoryEnum**
```java
public enum LotterySubCategoryEnum {
    LOTTERY_MODE("LOTTERY_MODE", "抽籤型"),
    SCRATCH_MODE("SCRATCH_MODE", "刮刮樂型");
    
    private final String code;  // 資料庫存的值
    private final String name;  // 中文名稱
}
```

## 前端使用範例

清理後，前端只需要知道一種 Enum 格式：

```javascript
// ✅ 正確：使用 xxxEnum
const response = await axios.get('/api/enums/lottery-status');
// 返回：
// [
//   { code: "DRAFT", name: "草稿" },
//   { code: "OFF_SHELF", name: "已下架" },
//   ...
// ]

// ❌ 錯誤：不會再有其他格式
```

## 為什麼保留 xxxEnum 而非 xxx？

1. **簡單實用**：
   - `code` + `name` 結構簡單明瞭
   - 前端可以直接使用 `code` 和 `name`

2. **正在使用中**：
   - `LotteryServiceImpl` 已經使用 `xxxEnum`
   - 不需要大規模重構

3. **符合慣例**：
   - 大部分 Java 專案使用 `xxxEnum` 命名
   - 易於識別是列舉類型

## 清理後的好處

✅ 前端明確知道使用哪種 Enum  
✅ 後端維護更簡單  
✅ 減少編譯時間  
✅ 降低錯誤使用的機率  
✅ 統一的命名規範  

---

## 注意事項

⚠️ **清理前請確保應用程式沒有在運行**  
⚠️ **清理後請執行 `mvn clean compile` 驗證**  
⚠️ **如果有 Git，建議先 commit 當前狀態**  

---

**更新時間**：2026-01-07  
**清理腳本**：`clean-duplicate-enums.bat` 或 `clean-duplicate-enums.ps1`
