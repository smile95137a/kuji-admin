-- 039-migrate-image-urls-to-stardo.sql
-- 用途：
-- 1. 將資料庫中舊的 S3 圖片網址
--    https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/...
--    批次改為
--    https://stardo.tw/images/...
-- 2. 僅替換舊前綴，不會動到其他第三方或手動填寫的圖片網址

SET @old_base := 'https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/';
SET @new_base := 'https://stardo.tw/images/';

START TRANSACTION;

-- 預覽：各表命中筆數
SELECT 'banner.image_url' AS target, COUNT(*) AS matched_count
FROM banner
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'lottery.image_url', COUNT(*)
FROM lottery
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'lottery_prize.image_url', COUNT(*)
FROM lottery_prize
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'news.image_url', COUNT(*)
FROM news
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'lottery_theme.image_url', COUNT(*)
FROM lottery_theme
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'store.logo_url', COUNT(*)
FROM store
WHERE logo_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'store.cover_image_url', COUNT(*)
FROM store
WHERE cover_image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'order_item.lottery_image_url', COUNT(*)
FROM order_item
WHERE lottery_image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'order_item.prize_image_url', COUNT(*)
FROM order_item
WHERE prize_image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'user.avatar', COUNT(*)
FROM `user`
WHERE avatar LIKE CONCAT(@old_base, '%');

-- 正式更新
UPDATE banner
SET image_url = REPLACE(image_url, @old_base, @new_base)
WHERE image_url LIKE CONCAT(@old_base, '%');

UPDATE lottery
SET image_url = REPLACE(image_url, @old_base, @new_base)
WHERE image_url LIKE CONCAT(@old_base, '%');

UPDATE lottery_prize
SET image_url = REPLACE(image_url, @old_base, @new_base)
WHERE image_url LIKE CONCAT(@old_base, '%');

UPDATE news
SET image_url = REPLACE(image_url, @old_base, @new_base)
WHERE image_url LIKE CONCAT(@old_base, '%');

UPDATE lottery_theme
SET image_url = REPLACE(image_url, @old_base, @new_base)
WHERE image_url LIKE CONCAT(@old_base, '%');

UPDATE store
SET logo_url = REPLACE(logo_url, @old_base, @new_base)
WHERE logo_url LIKE CONCAT(@old_base, '%');

UPDATE store
SET cover_image_url = REPLACE(cover_image_url, @old_base, @new_base)
WHERE cover_image_url LIKE CONCAT(@old_base, '%');

UPDATE order_item
SET lottery_image_url = REPLACE(lottery_image_url, @old_base, @new_base)
WHERE lottery_image_url LIKE CONCAT(@old_base, '%');

UPDATE order_item
SET prize_image_url = REPLACE(prize_image_url, @old_base, @new_base)
WHERE prize_image_url LIKE CONCAT(@old_base, '%');

UPDATE `user`
SET avatar = REPLACE(avatar, @old_base, @new_base)
WHERE avatar LIKE CONCAT(@old_base, '%');

-- 更新後再次檢查是否還有舊網址殘留
SELECT 'banner.image_url' AS target, COUNT(*) AS remaining_old_count
FROM banner
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'lottery.image_url', COUNT(*)
FROM lottery
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'lottery_prize.image_url', COUNT(*)
FROM lottery_prize
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'news.image_url', COUNT(*)
FROM news
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'lottery_theme.image_url', COUNT(*)
FROM lottery_theme
WHERE image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'store.logo_url', COUNT(*)
FROM store
WHERE logo_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'store.cover_image_url', COUNT(*)
FROM store
WHERE cover_image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'order_item.lottery_image_url', COUNT(*)
FROM order_item
WHERE lottery_image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'order_item.prize_image_url', COUNT(*)
FROM order_item
WHERE prize_image_url LIKE CONCAT(@old_base, '%')
UNION ALL
SELECT 'user.avatar', COUNT(*)
FROM `user`
WHERE avatar LIKE CONCAT(@old_base, '%');

COMMIT;

-- 如果你想先人工確認再提交，可以把上面的 COMMIT 改成：
-- ROLLBACK;
