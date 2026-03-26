-- 診斷腳本：檢查商品查詢問題

-- 1. 檢查 lottery 表是否有資料
SELECT COUNT(*) AS total_lotteries FROM lottery;

-- 2. 檢查 owner@teststore.com 的資訊
SELECT 
    au.id AS admin_user_id,
    au.username,
    au.email,
    au.status AS user_status,
    r.code AS role_code
FROM admin_user au
LEFT JOIN admin_user_role aur ON au.id = aur.admin_user_id
LEFT JOIN role r ON aur.role_id = r.id
WHERE au.email = 'owner@teststore.com';

-- 3. 檢查該使用者的店家關聯
SELECT 
    su.id AS store_user_id,
    su.admin_user_id,
    su.store_id,
    su.role_type,
    s.store_name,
    s.status AS store_status
FROM store_user su
LEFT JOIN store s ON su.store_id = s.id
WHERE su.admin_user_id = '424a9835-a0b8-4257-9a3e-be51b1d5fc43';

-- 4. 檢查該店家的商品
SELECT 
    l.id,
    l.store_id,
    l.title,
    l.category,
    l.price_per_draw,
    l.status,
    l.created_at
FROM lottery l
WHERE l.store_id IN (
    SELECT store_id 
    FROM store_user 
    WHERE admin_user_id = '424a9835-a0b8-4257-9a3e-be51b1d5fc43'
);

-- 5. 檢查所有商品（不過濾店家）
SELECT 
    l.id,
    l.store_id,
    l.title,
    l.status,
    s.store_name
FROM lottery l
LEFT JOIN store s ON l.store_id = s.id
ORDER BY l.created_at DESC
LIMIT 10;
