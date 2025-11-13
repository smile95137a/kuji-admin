CREATE TABLE admin_user (
    id CHAR(36) PRIMARY KEY COMMENT 'UUID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '帳號',
    password VARCHAR(255) NOT NULL COMMENT '密碼（加密存儲）',
    status TINYINT DEFAULT 1 COMMENT '狀態: 0=停用, 1=啟用',
    last_login DATETIME COMMENT '最後登入時間',
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台會員';


CREATE TABLE role (
    id CHAR(36) PRIMARY KEY COMMENT 'UUID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名稱，例如系統管理員、商品管理員',
    description VARCHAR(255) COMMENT '角色描述',
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';


CREATE TABLE menu (
    id CHAR(36) PRIMARY KEY COMMENT 'UUID',
    name VARCHAR(50) NOT NULL COMMENT '選單名稱',
    path VARCHAR(255) COMMENT '前端路由路徑',
    parent_id CHAR(36) DEFAULT NULL COMMENT '父選單id，NULL代表頂層',
    icon VARCHAR(50) COMMENT '選單圖標',
    order_num INT DEFAULT 0 COMMENT '排序',
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES menu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台選單表';


CREATE TABLE admin_user_role (
    admin_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    PRIMARY KEY (admin_id, role_id),
    CONSTRAINT fk_admin_user_role_admin FOREIGN KEY (admin_id) REFERENCES admin_user(id),
    CONSTRAINT fk_admin_user_role_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台會員角色關聯';


CREATE TABLE role_menu (
    role_id CHAR(36) NOT NULL,
    menu_id CHAR(36) NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES menu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜單關聯';

