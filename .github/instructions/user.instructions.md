---
description: "Use when working on user system, member registration, OAuth login, point system (Gold/Bonus), or frontend-backend role separation."
applyTo: ["**/User*", "**/user*", "**/Wallet*", "**/wallet*"]
---

# 會員系統需求

## 系統角色

### 一般會員（前台玩家）
- 功能：抽獎、儲值、查看紀錄
- 登入方式：Email + 密碼、Google OAuth
- 資料表：`user`

### 店家（後台管理者）
- **Admin**（最高權限）：管理所有店家帳號、角色與菜單權限
- **Store Owner**（店家主帳號）：管理店家資訊、商品、抽獎、訂單
- **Store Editor**（店家小編）：僅能操作 Owner 允許的功能
- 登入方式：Email + 密碼
- 資料表：`admin_user`

## 功能需求

### 會員資料
- **前台**: `user` 表 — email、nickname、password、avatar、gold_coins、bonus_coins
- **後台**: `admin_user` 表 — email、password、role

### 帳號建立流程
- **前台**: 玩家自助註冊（Email 或 Google OAuth）
- **後台**: Admin 新增店家帳號 → 首次登入強制改密碼

### 登入與安全
- **後台**: Email/密碼 + Access Token + Refresh Token
- **前台**: Email/密碼 或 Google OAuth + Refresh Token

### 點數系統（前台專用）
- **Gold（儲值金）** + **Bonus（紅利金）**
- 使用 `balance` + `point_logs` 雙軌制

## 需求總結

| 項目 | 前台 | 後台 |
|------|------|------|
| 資料表 | `user` | `admin_user` |
| 自助註冊 | ✔ | ✖ |
| 登入方式 | Email / Google | Email |
| Refresh Token | ✔ | ✔ |
| 點數 | Gold / Bonus | 無 |
| 權限 | 無 | RBAC（role + menu） |
