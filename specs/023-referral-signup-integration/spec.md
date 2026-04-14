# 功能規格書：推薦碼於使用者註冊流程整合

**功能分支**：`023-referral-signup-integration`  
**建立日期**：2026-04-14  
**狀態**：進行中  
**優先級**：P1（核心流程）  
**關聯功能**：012-referral-code, 018-user-auth  

---

## 概述

本規格定義推薦碼在使用者註冊流程中的完整整合，分為**兩條路徑**：

1. **官網 Email 註冊**：推薦碼在註冊欄位中提供，**註冊時一次性綁定**，之後不可變更
2. **第三方登入（Google OAuth）**：
   - **新用戶**：可在首次登入後的個人設定中補上推薦碼（**一次性**）
   - **既存用戶**：不允許修改推薦碼

---

## 使用者故事

### US1 — 官網註冊時輸入推薦碼（優先級：P1）

**角色**：新用戶  
**目標**：在官網安全註冊表單中，輸入店家的推薦碼，完成一命一脈的推薦關係建立

**驗收條件**：

1. **有效推薦碼**
   - **當** 用戶在官網填入有效推薦碼並提交註冊
   - **則** 系統驗證碼有效性、店家活躍、組數未超限
   - **並** 建立 `referral_record`，記錄（userId, storeId, code, createdAt）
   - **並且** `user.referral_code = code`（不可變更標記）

2. **無效推薦碼**
   - **當** 用戶填入不存在 / 已停用 / 已過期 / 超限碼
   - **則** 註冊流程中斷，前端顯示清晰的失敗原因
   - **例外**：異詞碼可忽略（預設無推薦）

3. **空（但合法）推薦碼**
   - **當** 用戶留空推薦碼欄位或輸入錯誤後清空
   - **則** 正常完成註冊，`user.referral_code = null`

---

### US2 — Google OAuth 新用戶補上推薦碼（優先級：P1）

**角色**：新用戶（首次 OAuth 登入）  
**目標**：在自動建立帳號後，於「會員資訊」補上推薦碼提升轉化率

**驗收條件**：

1. **首次 OAuth 標記**
   - **當** 用戶首次用 Google 登入
   - **則** 系統判斷新帳號，回傳 `isNewUser: true`
   - **並** 前端引導至「新用戶導覽」彈窗

2. **補上推薦碼頁（可選但推薦）**
   - **當** 新 OAuth 用戶點「填入店家推薦碼」
   - **則** 顯示輸入框 + 驗證按鈕
   - **許可** 之後點「略過」跳過此步

3. **一次性綁定**
   - **當** 用戶輸入有效碼並確認
   - **則** 建立 `referral_record` 和 `user.referral_code`
   - **並且** 之後無法再修改

4. **已存在用戶用 OAuth**
   - **當** 用戶已有帳號再用 OAuth 登入（Email 匹配）
   - **則** 系統忽略 OAuth 新用戶流程，直接登入
   - **不允許** 修改既存 `referral_code`

---

### US3 — 推薦碼驗證 API（客端使用，優先級：P1）

**角色**：前端應用  
**目標**：註冊/補碼時實時驗證推薦碼有效性，提升 UX

**驗收條件**：

1. **公開端點**（無需登入）
   - **端點**：`POST /api/auth/validate-referral`
   - **用途**：註冊表單即時驗證

2. **需認證端點**（已登入用戶用）
   - **端點**：`POST /api/user/apply-referral`
   - **用途**：OAuth 新用戶補碼時驗證並綁定
   - **授權**：ROLE_USER 且 `user.referral_code IS NULL`

3. **驗證邏輯**
   - 碼存在 ✓ 且活躍 ✓ 且未超限 ✓ 且有效期內 ✓
   - 非自推薦 ✓

---

### US4 — 後台推薦統計及審計（優先級：P2）

**角色**：平台管理員  
**目標**：查看推薦轉化、管理推薦碼生命週期

**驗收條件**：

1. 推薦碼管理頁
   - 建立、編輯、停用推薦碼
   - 查看各碼的使用統計（含新 OAuth 用戶來源）

2. 推薦統計報表
   - 按店家分組統計（官網註冊 vs OAuth 註冊佔比）
   - 時間軸與轉化漏斗

---

## 資料模型變更

### user 表新增 / 修改欄位

```sql
ALTER TABLE user ADD COLUMN IF NOT EXISTS referral_code VARCHAR(50) 
  DEFAULT NULL COMMENT '推薦碼（一次性，正向不可變更）';

ALTER TABLE user ADD COLUMN IF NOT EXISTS referred_store_id VARCHAR(36) 
  DEFAULT NULL COMMENT '推薦來源店家 ID（外鍵 store.id）';

ALTER TABLE user ADD COLUMN IF NOT EXISTS 
  referral_bound_at TIMESTAMP DEFAULT NULL 
  COMMENT '推薦碼綁定時間（建立 referral_record 時同步）';

ALTER TABLE user ADD COLUMN IF NOT EXISTS 
  is_oauth_new_user TINYINT(1) DEFAULT 0 
  COMMENT '標記：是否為 OAuth 新用戶首次登入（用於導引補碼流程）';
```

### referral_record 表（已存在，補充記錄）

```sql
-- referral_record 新增欄位區分來源
ALTER TABLE referral_record ADD COLUMN IF NOT EXISTS 
  signup_method ENUM('EMAIL', 'OAUTH') DEFAULT 'EMAIL' 
  COMMENT 'EMAIL=官網註冊時綁定, OAUTH=登入後補碼';
```

---

## API 端點總覽

| 方法 | 路徑 | 認證 | 用途 |
|------|------|------|------|
| POST | `/api/auth/validate-referral` | ✗ 公開 | 註冊/補碼前驗證 |
| POST | `/api/auth/register` | ✗ 公開 | 官網註冊（新增 `referralCode` 欄位） |
| POST | `/api/user/apply-referral` | ✓ USER | 登入後補上推薦碼（OAuth 新用戶） |
| GET | `/api/user/me` | ✓ USER | 查詢個人資訊（含 `referralCode`） |

---

## 流程圖

```
┌─────────────────────────────────────────────────────────────┐
│                     新用戶進入系統                              │
└─────────────────────────────────────────────────────────────┘
                           │
                ┌──────────┴──────────┐
                │                   │
         官網註冊              Google OAuth
                │                   │
                ▼                   ▼
      【官網註冊表單】         【Google 授權】
           │                       │
           ├─ Email               ├─ 系統自動建帳
           ├─ 密碼              ├─ 標記 is_oauth_new_user = 1
           └─ 推薦碼 ◄────────────  └─ 回傳 {token, isNewUser: true}
                │                       │
                ▼                       ▼
         驗證推薦碼                   【新用戶導覽彈窗】
         (POST validate-referral)      │
                │                 ┌────┴─────┐
           ✓ 有效              │          │
           ✗ 無效 ◄────────    填碼      略過
                │              │          │
                ▼              ▼          ▼
           完成註冊     驗證 & 綁定    直接進主畫面
           ├─ 建立 user        ├─ POST /api/user/apply-referral
           ├─ 建立 referral_   ├─ 建立 referral_record
           │  record          └─ user.referral_code = code
           └─ user.referral_
              code = code
```

---

## 安全性考量

1. **一次性不可變**：使用 DB 觸發器或應用層檢查，確保 `referral_code` 不可變更
2. **無限制嘗試防止**：驗證端點限速（Rate limit）
3. **自推薦防止**：比對推薦碼擁有者與新用戶資訊
4. **合規審計**：記錄所有推薦碼綁定事件

---

## 後續相關功能

- **推薦獎勵系統**（紅利發放，見 018-coin-system）
- **推薦統計報表**（見 022-report-analytics）
