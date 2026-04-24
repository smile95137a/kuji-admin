-- ============================================================
-- Fix: 修復被舊版 loginWithGoogle() 錯誤合併的帳號
-- 問題原因：舊版程式碼在 EMAIL 帳號嘗試 Google OAuth 時，
--           會靜默將 provider 改為 GOOGLE，導致無法用密碼登入
-- 修復時間：2026-04-24
-- ============================================================

-- 1. 修復 user2@test.com（provider 被誤改為 GOOGLE）
UPDATE user
SET 
    provider      = 'EMAIL',
    provider_id   = NULL,
    email_verified = 1,         -- 老帳號直接標記為已驗證
    updated_at    = NOW()
WHERE 
    email    = 'user2@test.com'
    AND provider = 'GOOGLE';

-- 確認結果
SELECT id, email, provider, provider_id, email_verified, status
FROM user
WHERE email = 'user2@test.com';

-- ============================================================
-- 2. 通用修復：找出所有「有密碼但 provider=GOOGLE」的帳號
--    這些帳號應該是被舊版程式誤合併的
-- ============================================================
-- 先查看有哪些帳號受影響
SELECT id, email, provider, email_verified,
       password IS NOT NULL AS has_password,
       created_at
FROM user
WHERE provider = 'GOOGLE'
  AND password IS NOT NULL;

-- 如果上面查詢有結果，執行以下修復（請先確認名單再執行）
-- UPDATE user
-- SET provider = 'EMAIL', provider_id = NULL, email_verified = 1, updated_at = NOW()
-- WHERE provider = 'GOOGLE' AND password IS NOT NULL;

-- ============================================================
-- 3. 確認 email_verified=0 且無驗證 token 的 EMAIL 帳號
--    這些帳號無法登入（需要管理員手動驗證或重送驗證信）
-- ============================================================
SELECT id, email, email_verified, email_verification_token, created_at
FROM user
WHERE provider = 'EMAIL'
  AND (email_verified IS NULL OR email_verified = 0)
ORDER BY created_at DESC
LIMIT 20;
