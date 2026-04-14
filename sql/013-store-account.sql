CREATE TABLE IF NOT EXISTS `admin_token_blacklist` (
  `admin_user_id` VARCHAR(36) NOT NULL,
  `blacklist_gen` INT NOT NULL DEFAULT 0,
  `updated_at` DATETIME,
  PRIMARY KEY (`admin_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
