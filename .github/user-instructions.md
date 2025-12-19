### **會員系統需求概要**

#### **系統角色**
1. **一般會員（前台玩家）**
    - 功能：抽獎、儲值、查看紀錄。
    - 登入方式：Email + 密碼、Google OAuth。
    - 資料表：`user`。

2. **店家（後台管理者）**
    - 角色：
      - **Admin**（最高權限）
         - 管理所有店家帳號、角色與菜單權限。
         - 新增/停用店家帳號。
      - **Store Owner**（店家主帳號）
         - 管理店家資訊、商品、抽獎、訂單。
         - 權限依角色與菜單設定。
      - **Store Editor**（店家小編）
         - 僅能操作 Owner 設定允許的功能（如商品管理、部分訂單權限）。
         - 無法查看財務或高權限頁面。
    - 登入方式：Email + 密碼。
    - 資料表：`admin_user`。

3. **平台管理者**
    - 最高權限，負責全局管理。

---

#### **功能需求**

1. **會員系統**
    - **前台會員**
      - 資料表：`user`。
      - 欄位：
         - `email`、`nickname`、`password`、`avatar`。
         - `gold_coins`（儲值金）、`bonus_coins`（紅利金）。
    - **後台會員**
      - 資料表：`admin_user`。
      - 欄位：
         - `email`、`password`、`role` 等。

2. **權限管理（RBAC）**
    - **Role**
      - 每個 `admin_user` 可綁定多個角色。
      - 預設角色：`Admin`、`StoreOwner`、`StoreEditor`。
    - **Menu**
      - 定義後台左側選單，支援階層與排序。
      - 店家與小編的菜單由 `role_menu` 決定。

3. **帳號建立流程**
    - **後台**
      - Admin 新增店家帳號（`StoreOwner`）。
      - 店家首次登入需強制改密碼。
      - 店家可新增小編帳號（`StoreEditor`）。
    - **前台**
      - 玩家可自助註冊（Email 或 Google OAuth）。

4. **登入與安全**
    - **後台**
      - Email / 密碼。
      - 使用 Access Token + Refresh Token。
    - **前台**
      - Email / 密碼、Google OAuth。
      - 支援 Refresh Token。

5. **點數系統（前台專用）**
    - **Gold（儲值金）**
    - **Bonus（紅利金）**
    - 使用 `balance` + `point_logs` 雙軌制。

---

#### **需求總結**

| 項目            | 前台                     | 後台                          |
|-----------------|-------------------------|------------------------------|
| 資料表          | `user`                  | `admin_user`                 |
| 使用者類型      | 玩家                     | Admin / StoreOwner / StoreEditor |
| 自助註冊        | ✔ Yes                  | ✖ No                        |
| 登入方式        | Email / Google          | Email                       |
| Refresh Token   | ✔ Yes                  | ✔ Yes                       |
| 點數            | Gold / Bonus           | 無                           |
| 權限            | 無                      | RBAC（role + menu）         |

---

此整理版本可作為會員系統的基礎，日後可根據需求進行補充與擴展。
