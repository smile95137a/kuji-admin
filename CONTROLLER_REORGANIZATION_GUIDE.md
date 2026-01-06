# Controller 重組完成指南

## 當前狀況

由於 Windows CMD 的互動式提示問題，檔案移動遇到困難。
請你手動完成以下步驟：

## 步驟 1: 手動刪除重複檔案

```bash
del "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\LotteryController.java"
del "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\FrontendLotteryController.java"
del "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\LotteryDrawController.java"
```

## 步驟 2: 手動移動檔案

在 Windows 檔案總管中：

**移動到 `controller/api/` 目錄：**
- `controller/LotteryController.java` → `controller/api/`
- `controller/UserController.java` → `controller/api/`
- `controller/ApiAuthController.java` → `controller/api/`
- `controller/OAuth2Controller.java` → `controller/api/`
- `controller/LotteryDrawController.java` → `controller/api/`

## 步驟 3: 修改檔案內容

完成檔案移動後，告訴我，我會幫你批量修改所有檔案的：
1. package 聲明
2. @RequestMapping 路徑
3. 加上註解說明

## 最終目錄結構

```
controller/
├── admin/                              # 後台 API
│   ├── AdminAuthController.java        # /api/admin/auth
│   ├── AdminLotteryController.java     # /api/admin/lottery
│   ├── AdminUserController.java        # /api/admin/users
│   ├── LotteryPrizeController.java     # /api/admin/lotteries (獎品管理)
│   ├── MenuController.java             # /api/admin/menus
│   ├── PermissionController.java       # /api/admin/permissions
│   └── RoleController.java             # /api/admin/roles
│
├── api/                                # 前台 API
│   ├── ApiAuthController.java          # /api/auth
│   ├── LotteryController.java          # /api/lottery
│   ├── LotteryDrawController.java      # /api/lottery (抽獎功能)
│   ├── OAuth2Controller.java           # /api/auth/oauth2
│   └── UserController.java             # /api/user
│
└── TestController.java                 # 測試用（可刪除）
```

## 需要修改的檔案清單

### controller/api/ 目錄（前台）

#### 1. ApiAuthController.java
- package: `com.group.admin.controller.api`
- @RequestMapping: `/auth` (改掉 `/api/auth`)

#### 2. LotteryController.java
- package: `com.group.admin.controller.api`
- @RequestMapping: `/lottery` (改掉 `/api/lottery`)

#### 3. LotteryDrawController.java  
- package: `com.group.admin.controller.api`
- @RequestMapping: 檢查是否已正確

#### 4. OAuth2Controller.java
- package: `com.group.admin.controller.api`
- @RequestMapping: `/auth/oauth2` (改掉 `/api/auth/oauth2`)

#### 5. UserController.java
- package: `com.group.admin.controller.api`
- @RequestMapping: `/user` (保持不變)

### controller/admin/ 目錄（後台）

#### 1. AdminAuthController.java
- ✅ package: `com.group.admin.controller.admin` (已完成)
- @RequestMapping: 檢查是否為 `/admin/auth`

#### 2. AdminLotteryController.java
- ✅ package: `com.group.admin.controller.admin` (已完成)
- ✅ @RequestMapping: `/admin/lottery` (已完成)

#### 3. AdminUserController.java
- ✅ package: `com.group.admin.controller.admin` (已完成)
- @RequestMapping: 檢查是否為 `/admin/users`

#### 4. LotteryPrizeController.java
- ✅ package: `com.group.admin.controller.admin` (已完成)
- @RequestMapping: `/admin/lottery` 或 `/admin/prizes`

#### 5. MenuController.java
- ✅ package: `com.group.admin.controller.admin` (已完成)
- ✅ @RequestMapping: `/admin/menus` (已完成)

#### 6. PermissionController.java
- ✅ package: `com.group.admin.controller.admin` (已完成)
- ✅ @RequestMapping: `/admin/permissions` (已完成)

#### 7. RoleController.java
- ✅ package: `com.group.admin.controller.admin` (已完成)
- ✅ @RequestMapping: `/admin/roles` (已完成)

## 完成檔案移動後

請告訴我「檔案已經移動完成」，我會立即幫你批量修改所有檔案的 package 和 @RequestMapping。
