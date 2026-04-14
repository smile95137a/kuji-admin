# 資料模型：推薦碼於使用者註冊流程整合

---

## user 表新增欄位

```sql
ALTER TABLE user ADD COLUMN referral_code VARCHAR(50) 
  DEFAULT NULL UNIQUE COMMENT '推薦碼（一次性，不可變更）';

ALTER TABLE user ADD COLUMN referred_store_id VARCHAR(36) 
  DEFAULT NULL COMMENT '推薦來源店家 ID';

ALTER TABLE user ADD COLUMN referral_bound_at TIMESTAMP 
  DEFAULT NULL COMMENT '推薦碼綁定時間';

ALTER TABLE user ADD COLUMN is_oauth_new_user TINYINT(1) DEFAULT 0 
  COMMENT '標記：是否為 OAuth 新用戶首次登入';

-- 外鍵約束
ALTER TABLE user ADD CONSTRAINT fk_user_referred_store
  FOREIGN KEY (referred_store_id) REFERENCES store(id);
```

### 欄位說明

| 欄位名 | 型別 | 約束 | 說明 |
|-------|------|------|------|
| `referral_code` | VARCHAR(50) | UNIQUE, NULL | 推薦碼（註冊時綁定，一次性） |
| `referred_store_id` | VARCHAR(36) | FK, NULL | 推薦來源店家 |
| `referral_bound_at` | TIMESTAMP | NULL | 推薦碼綁定時間（建立 referral_record 時同步） |
| `is_oauth_new_user` | TINYINT(1) | NOT NULL, DEFAULT 0 | OAuth 新用戶標記，用於決定是否進補碼導覽 |

---

## referral_record 表新增欄位

```sql
ALTER TABLE referral_record ADD COLUMN signup_method ENUM('EMAIL', 'OAUTH') 
  DEFAULT 'EMAIL' COMMENT 'EMAIL=官網註冊時綁定, OAUTH=登入後補碼';
```

### signup_method 取值

- **EMAIL**：使用者於官網註冊時提供推薦碼
- **OAUTH**：使用者於第三方登入後補上推薦碼

---

## Entity 業務邏輯

### User 實體

```java
@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    private String id;
    
    private String email;
    private String password;
    private String nickname;
    private String provider;  // EMAIL, GOOGLE, etc.
    
    // 新欄位
    private String referralCode;        // 推薦碼（一次性）
    private String referredStoreId;     // 推薦來源店家
    private LocalDateTime referralBoundAt;  // 綁定時間
    private Integer isOauthNewUser;     // OAuth 新用戶標記（0/1）
    
    // 金幣 / 紅利（已存在）
    private Long goldCoins;
    private Long bonusCoins;
    
    // 其他
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## ReferralRecord 業務邏輯

```java
@Entity
@Table(name = "referral_record")
@Data
public class ReferralRecord {
    @Id
    private String id;
    
    private String userId;             // 新用戶 ID
    private String referralCode;       // 使用的推薦碼（冗餘）
    private String storeId;            // 推薦來源店家
    private String signupMethod;       // EMAIL 或 OAUTH
    
    // referrer 資訊（若需要）
    private String referrerUserId;     // 推薦人 ID（若推薦碼為用戶碼）
    private String referrerStoreId;    // 推薦店家 ID
    
    private LocalDateTime createdAt;
}
```

---

## 查詢範例

### 查詢用戶是否已綁定推薦碼

```sql
SELECT referral_code, referred_store_id, referral_bound_at
FROM user
WHERE id = ? AND referral_code IS NOT NULL;
```

### 查詢特定推薦碼下的所有新用戶

```sql
SELECT rr.user_id, rr.signup_method, rr.created_at
FROM referral_record rr
WHERE rr.referral_code = ?
ORDER BY rr.created_at DESC;
```

### 查詢 OAuth 新用戶

```sql
SELECT id, email, is_oauth_new_user, referral_code
FROM user
WHERE provider = 'GOOGLE' AND is_oauth_new_user = 1;
```

### 推薦統計（按店家分組）

```sql
SELECT 
    s.id,
    s.store_name,
    COUNT(DISTINCT rr.user_id) AS total_referred_users,
    SUM(CASE WHEN rr.signup_method = 'EMAIL' THEN 1 ELSE 0 END) AS email_signup_count,
    SUM(CASE WHEN rr.signup_method = 'OAUTH' THEN 1 ELSE 0 END) AS oauth_signup_count
FROM store s
LEFT JOIN referral_code rc ON rc.store_id = s.id
LEFT JOIN referral_record rr ON rr.referral_code = rc.code
WHERE s.status = 'ACTIVE'
GROUP BY s.id, s.store_name
ORDER BY total_referred_users DESC;
```

---

## 資料完整性保護

### 觸發器（可選，增強安全）

```sql
-- 防止 referral_code 被修改（INSERT 後不允許 UPDATE）
DELIMITER //
CREATE TRIGGER trg_user_referral_code_immutable
BEFORE UPDATE ON user
FOR EACH ROW
BEGIN
    IF OLD.referral_code IS NOT NULL AND NEW.referral_code != OLD.referral_code THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '推薦碼不允許修改';
    END IF;
END//
DELIMITER ;
```

---

## 約束總結

| 約束類型 | 欄位 | 規則 |
|---------|------|------|
| UNIQUE | `referral_code` | 確保一碼一人，防止重複綁定 |
| NOT NULL | `id` (PK) | 主鍵 |
| FOREIGN KEY | `referred_store_id` → `store.id` | 確保推薦店家存在 |
| CHECK (應用層) | `referral_bound_at` | 只在 `referral_code` NOT NULL 時有值 |
| CHECK (應用層) | `is_oauth_new_user` | 只在 OAuth 用戶首次登入時設為 1 |

