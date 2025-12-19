-- =============================================
-- KUJI 抽獎平台完整 DDL - UUID 版本
-- 生成時間: 2025-12-18
-- 主鍵策略: 全面採用 UUID (VARCHAR(36))
-- 說明: 所有 ID 欄位改為 VARCHAR(36) 並使用 UUID
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 清除舊表（依序執行，注意外鍵依賴）
-- =============================================

DROP TABLE IF EXISTS lottery_draw_record;
DROP TABLE IF EXISTS lottery_lock;
DROP TABLE IF EXISTS lottery_prize;
DROP TABLE IF EXISTS lottery;
DROP TABLE IF EXISTS point_log;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS banner;
DROP TABLE IF EXISTS store_user;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS role_menu;
DROP TABLE IF EXISTS admin_user_role;
DROP TABLE IF EXISTS admin_operation_log;
DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS admin_user;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS `user`;

-- =============================================
-- 一、後台會員系統 (store-account-management.prompt.md)
-- =============================================

-- 後台管理者帳號（Admin / StoreOwner / StoreEditor）
CREATE TABLE admin_user (
    id VARCHAR(36) PRIMARY KEY COMMENT '使用者 ID (UUID)',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '登入帳號（使用 Email）',
    password VARCHAR(255) NOT NULL COMMENT '密碼（BCrypt 加密）',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Email',
    display_name VARCHAR(100) NOT NULL COMMENT '顯示名稱',
    phone VARCHAR(20) COMMENT '聯絡電話',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '狀態：ACTIVE/INACTIVE/PENDING',
    force_change_password TINYINT(1) NOT NULL DEFAULT 1 COMMENT '首次登入需改密碼：0=否, 1=是',
    last_login_at DATETIME COMMENT '最後登入時間',
    created_by VARCHAR(36) COMMENT '建立者 ID（Admin UUID）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36) COMMENT '最後修改者 ID',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark TEXT COMMENT '備註',
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台管理者帳號表';


-- 角色表 (permissions-rbac.prompt.md)
CREATE TABLE role (
    id VARCHAR(36) PRIMARY KEY COMMENT '角色 ID (UUID)',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名稱：Admin/StoreOwner/StoreEditor',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代碼：ROLE_ADMIN/ROLE_STORE_OWNER/ROLE_STORE_EDITOR',
    description VARCHAR(255) COMMENT '角色描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';


-- 選單/功能表 (permissions-rbac.prompt.md)
CREATE TABLE menu (
    id VARCHAR(36) PRIMARY KEY COMMENT '選單 ID (UUID)',
    name VARCHAR(50) NOT NULL COMMENT '選單名稱',
    code VARCHAR(50) COMMENT '權限代碼（用於程式判斷）',
    path VARCHAR(255) COMMENT '前端路由路徑',
    parent_id VARCHAR(36) DEFAULT NULL COMMENT '父選單 ID，NULL=頂層',
    icon VARCHAR(50) COMMENT '選單圖標',
    order_num INT DEFAULT 0 COMMENT '排序',
    is_visible TINYINT(1) DEFAULT 1 COMMENT '是否顯示：0=隱藏, 1=顯示',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES menu(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台選單表';


-- 使用者角色關聯表（多對多）
CREATE TABLE admin_user_role (
    id VARCHAR(36) PRIMARY KEY COMMENT '關聯 ID (UUID)',
    admin_user_id VARCHAR(36) NOT NULL COMMENT '使用者 ID',
    role_id VARCHAR(36) NOT NULL COMMENT '角色 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (admin_user_id, role_id),
    CONSTRAINT fk_admin_user_role_user FOREIGN KEY (admin_user_id) REFERENCES admin_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用者角色關聯表';


-- 角色選單權限表（多對多，含細部權限）
CREATE TABLE role_menu (
    id VARCHAR(36) PRIMARY KEY COMMENT '關聯 ID (UUID)',
    role_id VARCHAR(36) NOT NULL COMMENT '角色 ID',
    menu_id VARCHAR(36) NOT NULL COMMENT '選單 ID',
    can_view TINYINT(1) DEFAULT 1 COMMENT '可查看',
    can_edit TINYINT(1) DEFAULT 0 COMMENT '可編輯',
    can_delete TINYINT(1) DEFAULT 0 COMMENT '可刪除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色選單權限表';


-- =============================================
-- 二、店家管理 (store-management.prompt.md)
-- =============================================

-- 店家資料表
CREATE TABLE store (
    id VARCHAR(36) PRIMARY KEY COMMENT '店家 ID (UUID)',
    owner_id VARCHAR(36) NOT NULL COMMENT '店家主帳號 ID（StoreOwner）',
    
    -- 基本資料
    store_name VARCHAR(100) NOT NULL COMMENT '店家公開顯示名稱',
    short_description VARCHAR(255) NOT NULL COMMENT '短描述（列表用）',
    long_description TEXT COMMENT '詳細介紹、品牌故事',
    
    -- 圖片
    logo_url VARCHAR(255) NOT NULL COMMENT 'Logo 圖片 URL',
    cover_image_url VARCHAR(255) COMMENT '封面圖片 URL',
    
    -- 聯絡資訊
    email VARCHAR(100) NOT NULL COMMENT '聯絡 Email',
    phone VARCHAR(20) NOT NULL COMMENT '聯絡電話',
    address VARCHAR(255) NOT NULL COMMENT '地址',
    
    -- 社群連結
    facebook_url VARCHAR(255) COMMENT 'Facebook 連結',
    instagram_url VARCHAR(255) COMMENT 'Instagram 連結',
    line_id VARCHAR(100) COMMENT 'LINE ID',
    
    -- 營業資訊
    business_hours VARCHAR(100) NOT NULL COMMENT '營業時間',
    
    -- 狀態
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態：ACTIVE/INACTIVE',
    
    -- 後台用
    remark TEXT COMMENT '後台備註',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(36) COMMENT '最後修改者 ID',
    
    INDEX idx_status (status),
    INDEX idx_owner_id (owner_id),
    CONSTRAINT fk_store_owner FOREIGN KEY (owner_id) REFERENCES admin_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店家資料表';


-- 店家與使用者關聯表（StoreEditor 多對多）
CREATE TABLE store_user (
    id VARCHAR(36) PRIMARY KEY COMMENT '關聯 ID (UUID)',
    store_id VARCHAR(36) NOT NULL COMMENT '店家 ID',
    admin_user_id VARCHAR(36) NOT NULL COMMENT '使用者 ID',
    role_type VARCHAR(20) NOT NULL COMMENT '角色類型：OWNER/EDITOR',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_store_user (store_id, admin_user_id),
    CONSTRAINT fk_store_user_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    CONSTRAINT fk_store_user_admin FOREIGN KEY (admin_user_id) REFERENCES admin_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店家與使用者關聯表';


-- Banner 表
CREATE TABLE banner (
    id VARCHAR(36) PRIMARY KEY COMMENT 'Banner ID (UUID)',
    store_id VARCHAR(36) NOT NULL COMMENT '綁定店家 ID',
    title VARCHAR(100) NOT NULL COMMENT 'Banner 標題',
    image_url VARCHAR(255) NOT NULL COMMENT '圖片 URL',
    link_url VARCHAR(255) COMMENT '點擊連結',
    order_num INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態：ACTIVE/INACTIVE',
    start_time DATETIME COMMENT '開始顯示時間',
    end_time DATETIME COMMENT '結束顯示時間',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_id (store_id),
    INDEX idx_status (status),
    CONSTRAINT fk_banner_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner 表';


-- 後台操作紀錄（審計日誌）
CREATE TABLE admin_operation_log (
    id VARCHAR(36) PRIMARY KEY COMMENT '紀錄 ID (UUID)',
    admin_id VARCHAR(36) NOT NULL COMMENT '操作者 ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作類型：CREATE/UPDATE/DELETE/LOGIN',
    target_type VARCHAR(50) COMMENT '目標類型：ADMIN_USER/STORE/LOTTERY',
    target_id VARCHAR(36) COMMENT '目標 ID',
    description TEXT COMMENT '操作描述',
    ip_address VARCHAR(50) COMMENT 'IP 位址',
    user_agent VARCHAR(500) COMMENT '瀏覽器資訊',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_operation_log_admin FOREIGN KEY (admin_id) REFERENCES admin_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台操作紀錄表';


-- =============================================
-- 三、前台會員系統 (user-member-system.prompt.md)
-- =============================================

-- 前台玩家
CREATE TABLE `user` (
    id VARCHAR(36) PRIMARY KEY COMMENT '使用者 ID (UUID)',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'Email',
    nickname VARCHAR(100) COMMENT '暱稱',
    password VARCHAR(255) COMMENT '密碼（BCrypt 加密，OAuth 用戶可為空）',
    avatar VARCHAR(500) COMMENT '頭像網址',
    
    -- OAuth 登入
    provider VARCHAR(50) DEFAULT 'EMAIL' COMMENT '登入來源：EMAIL/GOOGLE',
    provider_id VARCHAR(255) COMMENT 'OAuth Provider 的用戶 ID',
    
    -- 點數（雙軌制）
    gold_coins BIGINT DEFAULT 0 COMMENT '儲值金（付費購買）',
    bonus_coins BIGINT DEFAULT 0 COMMENT '紅利金（系統贈送）',
    
    -- 狀態
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '狀態：ACTIVE/INACTIVE',
    email_verified TINYINT DEFAULT 0 COMMENT 'Email 是否驗證：0=否, 1=是',
    
    -- 額外欄位（對應現有 User Entity）
    phone_number VARCHAR(20) COMMENT '手機號碼',
    
    last_login_at DATETIME COMMENT '最後登入時間',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_provider (provider, provider_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前台玩家表';


-- 點數變動紀錄
CREATE TABLE point_log (
    id VARCHAR(36) PRIMARY KEY COMMENT '紀錄 ID (UUID)',
    user_id VARCHAR(36) NOT NULL COMMENT '玩家 ID',
    point_type VARCHAR(20) NOT NULL COMMENT '點數類型：GOLD/BONUS',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作類型：DEPOSIT/DRAW/REFUND/BONUS_GRANT/BONUS_EXPIRE',
    amount BIGINT NOT NULL COMMENT '變動金額（正=增加，負=減少）',
    before_balance BIGINT COMMENT '變動前餘額',
    after_balance BIGINT COMMENT '變動後餘額',
    reference_type VARCHAR(50) COMMENT '關聯類型：PAYMENT/LOTTERY_DRAW/SYSTEM',
    reference_id VARCHAR(255) COMMENT '關聯 ID',
    remark VARCHAR(255) COMMENT '備註',
    expire_at DATETIME COMMENT '紅利金到期日（僅 bonus 用）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_point_log_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='點數變動紀錄表';


-- Refresh Token（前台 + 後台共用）
CREATE TABLE refresh_token (
    id VARCHAR(36) PRIMARY KEY COMMENT 'Token ID (UUID)',
    user_type VARCHAR(20) NOT NULL COMMENT '用戶類型：USER/ADMIN',
    user_id VARCHAR(36) NOT NULL COMMENT '用戶 ID',
    token VARCHAR(500) NOT NULL UNIQUE COMMENT 'Refresh Token',
    device_info VARCHAR(255) COMMENT '裝置資訊',
    ip_address VARCHAR(50) COMMENT 'IP 位址',
    expires_at DATETIME NOT NULL COMMENT '到期時間',
    is_revoked TINYINT DEFAULT 0 COMMENT '是否已撤銷：0=否, 1=是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_type, user_id),
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Refresh Token 表';


-- =============================================
-- 四、抽獎系統 (game-management.prompt.md)
-- =============================================

-- 抽獎活動/商品
CREATE TABLE lottery (
    id VARCHAR(36) PRIMARY KEY COMMENT '商品 ID (UUID)',
    store_id VARCHAR(36) NOT NULL COMMENT '所屬店家 ID',
    
    -- 基本資訊
    title VARCHAR(255) NOT NULL COMMENT '商品/活動名稱',
    description TEXT COMMENT '詳細描述',
    image_url VARCHAR(255) COMMENT '商品主圖 URL',
    
    -- 分類（固定列舉）
    category VARCHAR(50) NOT NULL COMMENT '分類：OFFICIAL_ICHIBAN/GACHA/TRADING_CARD/CUSTOM_GACHA',
    sub_category VARCHAR(50) COMMENT '子類型：LOTTERY_MODE/SCRATCH_CARD_MODE',
    
    -- 價格設定
    price_per_draw BIGINT NOT NULL DEFAULT 0 COMMENT '每抽價格',
    discounted_price BIGINT COMMENT '折扣價格',
    auto_discount_enabled TINYINT DEFAULT 0 COMMENT '是否啟用自動降價：0=否, 1=是',
    
    -- 多抽設定
    allow_multi_draw TINYINT DEFAULT 1 COMMENT '是否允許多抽：0=否, 1=是',
    multi_draw_options VARCHAR(100) COMMENT '多抽選項，逗號分隔如：5,10,50',
    
    -- 時間設定
    scheduled_at DATETIME COMMENT '定時上架時間',
    start_time DATETIME COMMENT '活動開始時間',
    end_time DATETIME COMMENT '活動結束時間',
    
    -- 抽獎次數統計
    total_draws INT DEFAULT 0 COMMENT '已抽次數',
    max_draws INT DEFAULT 0 COMMENT '總抽數上限',
    
    -- 狀態
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '狀態：DRAFT/ON_SHELF/OFF_SHELF',
    
    -- 排序與推薦
    order_num INT DEFAULT 0 COMMENT '顯示排序',
    weight INT DEFAULT 0 COMMENT '推薦權重',
    
    -- 稽核
    created_by VARCHAR(36) COMMENT '建立者 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark TEXT COMMENT '內部備註',
    
    INDEX idx_store_id (store_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_scheduled_at (scheduled_at),
    CONSTRAINT fk_lottery_store FOREIGN KEY (store_id) REFERENCES store(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎活動/商品表';


-- 抽獎獎項
CREATE TABLE lottery_prize (
    id VARCHAR(36) PRIMARY KEY COMMENT '獎項 ID (UUID)',
    lottery_id VARCHAR(36) NOT NULL COMMENT '所屬抽獎活動 ID',
    
    -- 基本資訊
    name VARCHAR(255) NOT NULL COMMENT '獎項名稱',
    description TEXT COMMENT '獎項描述',
    image_url VARCHAR(500) COMMENT '獎項圖片 URL',
    
    -- 等級與編號
    level VARCHAR(20) COMMENT '等級：A/B/C/D/E/F/G/LAST_PRIZE/GRAND_PRIZE',
    prize_number VARCHAR(50) COMMENT '籤號（刮刮樂用）',
    
    -- 數量
    quantity INT NOT NULL DEFAULT 1 COMMENT '總數量',
    remaining INT NOT NULL DEFAULT 0 COMMENT '剩餘數量',
    
    -- 抽獎權重
    weight INT DEFAULT 1 COMMENT '抽中權重',
    
    -- 獎項類型
    prize_type VARCHAR(50) DEFAULT 'FIGURE' COMMENT '類型：FIGURE/GOODS/POINT',
    point_value BIGINT DEFAULT 0 COMMENT '若為點數獎項，代表點數金額',
    
    -- 特殊標記
    is_last_prize TINYINT DEFAULT 0 COMMENT '是否為最後賞：0=否, 1=是',
    is_grand_prize TINYINT DEFAULT 0 COMMENT '是否為大賞：0=否, 1=是',
    
    -- 排序
    order_num INT DEFAULT 0 COMMENT '顯示排序',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_lottery_id (lottery_id),
    INDEX idx_remaining (remaining),
    INDEX idx_level (level),
    CONSTRAINT fk_lottery_prize_lottery FOREIGN KEY (lottery_id) REFERENCES lottery(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎獎項表';


-- 抽獎鎖定表（保護時間機制）
CREATE TABLE lottery_lock (
    id VARCHAR(36) PRIMARY KEY COMMENT '鎖定 ID (UUID)',
    lottery_id VARCHAR(36) NOT NULL COMMENT '被鎖定的商品 ID',
    user_id VARCHAR(36) NOT NULL COMMENT '鎖定者的使用者 ID',
    lock_start_time DATETIME NOT NULL COMMENT '鎖定開始時間',
    lock_end_time DATETIME NOT NULL COMMENT '鎖定結束時間',
    is_active TINYINT DEFAULT 1 COMMENT '鎖定是否有效：0=否, 1=是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lottery_id (lottery_id),
    INDEX idx_user_id (user_id),
    INDEX idx_lock_end_time (lock_end_time),
    CONSTRAINT fk_lottery_lock_lottery FOREIGN KEY (lottery_id) REFERENCES lottery(id) ON DELETE CASCADE,
    CONSTRAINT fk_lottery_lock_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎鎖定表';


-- 抽獎紀錄
CREATE TABLE lottery_draw_record (
    id VARCHAR(36) PRIMARY KEY COMMENT '紀錄 ID (UUID)',
    lottery_id VARCHAR(36) NOT NULL COMMENT '抽獎活動 ID',
    user_id VARCHAR(36) NOT NULL COMMENT '玩家 ID',
    prize_id VARCHAR(36) COMMENT '中獎獎項 ID',
    
    -- 選號（刮刮樂用）
    selected_number VARCHAR(50) COMMENT '玩家選擇的號碼',
    
    -- 花費
    cost_type VARCHAR(20) COMMENT '花費類型：GOLD/BONUS',
    cost_amount BIGINT DEFAULT 0 COMMENT '花費金額',
    
    -- 狀態
    status VARCHAR(50) DEFAULT 'COMPLETED' COMMENT '狀態：PENDING/COMPLETED/CANCELLED',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_lottery_id (lottery_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_draw_record_lottery FOREIGN KEY (lottery_id) REFERENCES lottery(id),
    CONSTRAINT fk_draw_record_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_draw_record_prize FOREIGN KEY (prize_id) REFERENCES lottery_prize(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎紀錄表';


-- =============================================
-- 五、訂單系統 (order.prompt.md)
-- =============================================

-- 訂單
CREATE TABLE `order` (
    id VARCHAR(36) PRIMARY KEY COMMENT '訂單 ID (UUID)',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '訂單編號',
    user_id VARCHAR(36) NOT NULL COMMENT '玩家 ID',
    
    -- 訂單資訊
    amount BIGINT NOT NULL COMMENT '金額',
    order_type VARCHAR(50) NOT NULL COMMENT '類型：TOPUP/DRAW/REFUND',
    
    -- 金流資訊
    payment_provider VARCHAR(50) COMMENT '金流商：ECPAY/LINEPAY/NEWEBPAY',
    provider_trade_no VARCHAR(255) COMMENT '金流商交易編號',
    
    -- 狀態
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '狀態：PENDING/PAID/FAILED/REFUNDED',
    
    -- 備註
    remark VARCHAR(500) COMMENT '備註',
    
    paid_at DATETIME COMMENT '付款時間',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單表';


SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- DDL 建立完成
-- 
-- 注意事項:
-- 1. 所有 ID 欄位皆為 VARCHAR(36) 儲存 UUID
-- 2. 應用程式層需使用 UUID.randomUUID() 生成 ID
-- 3. 初始資料請使用 Java DataInitializer 載入
-- 4. MyBatis Generator 配置需調整為 String 類型
-- =============================================
