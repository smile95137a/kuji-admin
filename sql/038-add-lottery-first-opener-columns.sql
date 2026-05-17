ALTER TABLE `lottery`
    ADD COLUMN IF NOT EXISTS `first_opener_user_id` VARCHAR(36) NULL COMMENT '商品唯一開套者 user id',
    ADD COLUMN IF NOT EXISTS `first_opener_session_id` VARCHAR(36) NULL COMMENT '商品唯一開套者的首輪 session id';

UPDATE `lottery` l
SET `first_opener_user_id` = COALESCE(
        `first_opener_user_id`,
        (
            SELECT s.`opener_user_id`
            FROM `lottery_session` s
            WHERE s.`lottery_id` = l.`id`
            ORDER BY s.`created_at` ASC, s.`id` ASC
            LIMIT 1
        )
    ),
    `first_opener_session_id` = COALESCE(
        `first_opener_session_id`,
        (
            SELECT s.`id`
            FROM `lottery_session` s
            WHERE s.`lottery_id` = l.`id`
            ORDER BY s.`created_at` ASC, s.`id` ASC
            LIMIT 1
        )
    )
WHERE `first_opener_user_id` IS NULL
   OR `first_opener_session_id` IS NULL;
