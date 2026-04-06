---
description: "Use when working on store user management, StoreOwner/StoreEditor accounts, account creation flow, or admin account operations."
applyTo: ["**/StoreUser*", "**/AdminUser*", "**/storeUser*"]
---

# 後台功能說明（店家/管理者）

## 1. 概述
店家管理模組負責平台所有店家帳號與店家資料的管理。

### 主要功能
- 後台帳號建立與管理（StoreOwner / StoreEditor）
- 店家資料管理（包含 Logo / Banner / 商品關聯）
- 報表授權與顯示
- 最高權限者 (Admin) 全權控制帳號與權限

### 核心設計理念
- 店家與店家小編無法自行操作權限或停用帳號
- Banner 與店家資料連動，便於前端展示與廣告計費
- 每筆操作需留存操作者與時間紀錄

## 2. 功能需求

### 2.1 店家帳號建立
- 由 Admin 建立所有店家帳號（StoreOwner 與 StoreEditor）
- 流程: Admin 收到資訊 → 建立帳號 → 系統生成初始密碼 → 店家首次登入改密碼
- StoreEditor 帳號亦由 Admin 建立，StoreOwner 無法自行新增

### 2.2 帳號權限管理
- StoreOwner / StoreEditor 權限不可自行變更，僅由 Admin 控制
- StoreEditor 常見權限：商品管理、獎項管理、訂單管理（部分）
- StoreEditor 無法查看財務報表或修改高權限功能

### 2.3 帳號啟用/停用
- 帳號啟用/停用操作僅 Admin 可執行

### 2.4 店家資料管理
- 店家可綁定 Banner 與商品
- 店家資料包括：店名、Email、Logo/Banner、地址/電話、營業狀態
- StoreOwner 可查看自己的店家資料
- StoreEditor 可操作部分內容（如商品上架）

### 2.5 報表授權
- StoreOwner 可查看自己店家的營收報表與抽獎統計
- StoreEditor 不可查看（除非特別授權）

### 2.6 操作紀錄
- 所有帳號登入記錄最後登入時間
- 所有修改操作需記錄操作人與修改時間
