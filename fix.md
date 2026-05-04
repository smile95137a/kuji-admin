
http://18.179.187.129/api/lottery/browse/d8251462-edc2-4e4b-bb2f-23e23c2bfc59/detail

{
    "success": false,
    "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByPrimaryKey-Inline\n### The error occurred while setting parameters\n### SQL: select            id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         ,           description, remark, gallery_images, content         from lottery     where id = ?\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []",
    "error": {
        "code": "BUSINESS_ERROR",
        "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByPrimaryKey-Inline\n### The error occurred while setting parameters\n### SQL: select            id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         ,           description, remark, gallery_images, content         from lottery     where id = ?\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []"
    },
    "meta": {
        "timestamp": "2026-04-30T13:02:19.035045703Z",
        "requestId": "9770cea0-aee9-4977-b5e8-496d890ac0a6"
    }
}


http://18.179.187.129/api/lottery/browse/list

{
    "success": false,
    "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByExample-Inline\n### The error occurred while setting parameters\n### SQL: select           'true' as QUERYID,           id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         from lottery                    WHERE (  status = ? )                        order by store_id ASC, created_at DESC\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []",
    "error": {
        "code": "BUSINESS_ERROR",
        "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByExample-Inline\n### The error occurred while setting parameters\n### SQL: select           'true' as QUERYID,           id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         from lottery                    WHERE (  status = ? )                        order by store_id ASC, created_at DESC\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []"
    },
    "meta": {
        "timestamp": "2026-04-30T13:05:29.833728210Z",
        "requestId": "9277c9e8-0793-4c28-9f48-4fdbfba7e14a"
    }
}



http://18.179.187.129/api/category/themes

{
    "success": false,
    "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByExample-Inline\n### The error occurred while setting parameters\n### SQL: select           'true' as QUERYID,           id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         from lottery                    WHERE (  status = ? )\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []",
    "error": {
        "code": "BUSINESS_ERROR",
        "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByExample-Inline\n### The error occurred while setting parameters\n### SQL: select           'true' as QUERYID,           id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         from lottery                    WHERE (  status = ? )\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []"
    },
    "meta": {
        "timestamp": "2026-04-30T13:05:40.046576693Z",
        "requestId": "8e12834e-1b44-4012-bd58-b1b1cb65df1d"
    }
}



http://18.179.187.129/api/wallet/transactions

{
    "success": false,
    "message": "系統發生未知錯誤，請稍後再試",
    "error": {
        "code": "COMMON_INTERNAL_001",
        "message": "系統發生未知錯誤，請稍後再試"
    },
    "meta": {
        "timestamp": "2026-04-30T13:06:00.356057960Z",
        "requestId": "8e99c32e-b046-4a4d-8885-f435f4e8e425"
    }
}


http://18.179.187.129/api/prize-box

{
    "success": false,
    "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByPrimaryKey-Inline\n### The error occurred while setting parameters\n### SQL: select            id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         ,           description, remark, gallery_images, content         from lottery     where id = ?\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []",
    "error": {
        "code": "BUSINESS_ERROR",
        "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByPrimaryKey-Inline\n### The error occurred while setting parameters\n### SQL: select            id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         ,           description, remark, gallery_images, content         from lottery     where id = ?\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []"
    },
    "meta": {
        "timestamp": "2026-04-30T13:06:16.093390969Z",
        "requestId": "d6498683-05d0-4fa8-b53c-d6e0bd5073b6"
    }
}


http://18.179.187.129/api/admin/lottery-with-prizes/list

{
    "success": false,
    "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByExample-Inline\n### The error occurred while setting parameters\n### SQL: select           'true' as QUERYID,           id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         from lottery                                            order by created_at DESC\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []",
    "error": {
        "code": "BUSINESS_ERROR",
        "message": "\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n### The error may exist in class path resource [mapper/LotteryMapper.xml]\n### The error may involve com.group.admin.mapper.LotteryMapper.selectByExample-Inline\n### The error occurred while setting parameters\n### SQL: select           'true' as QUERYID,           id, store_id, title, image_url, category, sub_category, game_mode, payment_type,      free_draw_threshold, delist_strategy, price_per_draw, discounted_price, auto_discount_enabled,      allow_multi_draw, multi_draw_options, scheduled_at,      start_time, end_time, total_draws, max_draws, protection_draws, protection_minutes,      free_draw_enabled, designated_prize_numbers, tickets_generated, status, order_num,      weight, created_by, created_at, updated_at, play_mode, hot_count, theme, tags, bonus_enabled,      bonus_points_per_draw, bonus_cost_per_draw         from lottery                                            order by created_at DESC\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'payment_type' in 'field list'\n; bad SQL grammar []"
    },
    "meta": {
        "timestamp": "2026-04-30T13:06:42.666858236Z",
        "requestId": "988ce1a8-2933-4a0b-bd56-31842211b6f1"
    }
}