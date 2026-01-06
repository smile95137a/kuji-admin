# 🗑️ 刪除重複檔案指南

## 問題說明

目前 `controller/api/` 資料夾中有 **3 個重複檔案**：

1. ✅ **LotteryBrowseController.java** - 正確的，保留
2. ❌ **LotteryController.java** - 重複的，內容跟 LotteryBrowseController 一樣（class 名稱是 LotteryControllerS）
3. ❌ **FrontendLotteryController.java** - 重複的

## 解決方法（3 選 1）

### 方法 1：用檔案總管手動刪除（最簡單）

1. 打開檔案總管
2. 貼上這個路徑：
   ```
   c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api
   ```
3. 刪除這 2 個檔案：
   - `LotteryController.java`
   - `FrontendLotteryController.java`
4. 完成！

### 方法 2：用 Git Bash（如果有安裝）

```bash
cd /c/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin
rm src/main/java/com/group/admin/controller/api/LotteryController.java
rm src/main/java/com/group/admin/controller/api/FrontendLotteryController.java
```

### 方法 3：開新的 PowerShell 視窗

```powershell
# 複製貼上以下指令（一行一行執行）
Remove-Item "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\LotteryController.java" -Force

Remove-Item "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\FrontendLotteryController.java" -Force
```

## 刪除後確認

檢查 `api/` 資料夾應該只剩 **5 個檔案**：

```
✅ ApiAuthController.java
✅ LotteryBrowseController.java    ← 商品瀏覽
✅ LotteryDrawController.java      ← 抽獎功能
✅ OAuth2Controller.java
✅ UserController.java
```

## 最終驗證

刪除後執行：

```bash
mvn clean compile
```

應該會看到：

```
[INFO] BUILD SUCCESS
```

---

**為什麼會有重複的 LotteryController？**

重新命名時 Windows 檔案系統出問題了：
- 想把 `LotteryController.java` 改名為 `LotteryBrowseController.java`
- 結果檔案系統建立了新檔案但沒刪除舊檔案
- 而且還把 class 名稱改成 `LotteryControllerS`（多了個 S）

所以現在有兩個檔案內容幾乎一樣，只是 class 名稱不同 😅

**建議用方法 1（檔案總管）最快最保險！**
