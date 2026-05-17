-- 類別管理選單補齊
-- 說明：
-- 1. 在「商品管理」底下新增「類別管理」子選單
-- 2. 補上 ROLE_ADMIN / ROLE_STORE_OWNER / ROLE_STORE_EDITOR 的 role_menu 權限
-- 3. 使用 NOT EXISTS 避免重複建立

SET @lottery_parent_id := (
    SELECT id
    FROM menu
    WHERE code IN (
        'LOTTERY_MANAGEMENT',
        'PRODUCT_MANAGEMENT',
        'PRODUCT_MANAGE',
        'LOTTERY_MANAGE',
        'lottery_management',
        'product_management'
    )
       OR name = '商品管理'
       OR (parent_id IS NULL AND path IN ('/admin/lotteries', '/home/lottery-with-prizes'))
    ORDER BY created_at ASC
    LIMIT 1
);

INSERT INTO menu (
    id,
    name,
    code,
    path,
    parent_id,
    icon,
    order_num,
    is_visible,
    created_at,
    updated_at
)
SELECT
    UUID(),
    '商品管理',
    'LOTTERY_MANAGEMENT',
    '/admin/lotteries',
    NULL,
    'shopping',
    20,
    1,
    NOW(),
    NOW()
FROM dual
WHERE @lottery_parent_id IS NULL;

SET @lottery_parent_id := (
    SELECT id
    FROM menu
    WHERE code = 'LOTTERY_MANAGEMENT'
       OR name = '商品管理'
       OR (parent_id IS NULL AND path IN ('/admin/lotteries', '/home/lottery-with-prizes'))
    ORDER BY created_at ASC
    LIMIT 1
);

UPDATE menu
SET name = '商品管理',
    code = 'LOTTERY_MANAGEMENT',
    path = '/admin/lotteries',
    icon = 'shopping',
    is_visible = 1,
    updated_at = NOW()
WHERE id = @lottery_parent_id;

INSERT INTO menu (
    id,
    name,
    code,
    path,
    parent_id,
    icon,
    order_num,
    is_visible,
    created_at,
    updated_at
)
SELECT
    UUID(),
    '類別管理',
    'CATEGORY_MANAGEMENT',
    '/home/categories',
    @lottery_parent_id,
    NULL,
    4,
    1,
    NOW(),
    NOW()
FROM dual
WHERE @lottery_parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM menu
      WHERE code = 'CATEGORY_MANAGEMENT'
  );

UPDATE menu
SET name = '類別管理',
    path = '/home/categories',
    parent_id = @lottery_parent_id,
    order_num = 4,
    is_visible = 1,
    updated_at = NOW()
WHERE code = 'CATEGORY_MANAGEMENT';

SET @category_menu_id := (
    SELECT id
    FROM menu
    WHERE code = 'CATEGORY_MANAGEMENT'
    ORDER BY created_at ASC
    LIMIT 1
);

SET @role_admin_id := (
    SELECT id
    FROM role
    WHERE code = 'ROLE_ADMIN'
    ORDER BY created_at ASC
    LIMIT 1
);

SET @role_store_owner_id := (
    SELECT id
    FROM role
    WHERE code = 'ROLE_STORE_OWNER'
    ORDER BY created_at ASC
    LIMIT 1
);

SET @role_store_editor_id := (
    SELECT id
    FROM role
    WHERE code = 'ROLE_STORE_EDITOR'
    ORDER BY created_at ASC
    LIMIT 1
);

INSERT INTO role_menu (
    id,
    role_id,
    menu_id,
    can_view,
    can_edit,
    can_delete,
    created_at
)
SELECT
    UUID(),
    @role_admin_id,
    @category_menu_id,
    1,
    1,
    1,
    NOW()
FROM dual
WHERE @role_admin_id IS NOT NULL
  AND @category_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM role_menu
      WHERE role_id = @role_admin_id
        AND menu_id = @category_menu_id
  );

INSERT INTO role_menu (
    id,
    role_id,
    menu_id,
    can_view,
    can_edit,
    can_delete,
    created_at
)
SELECT
    UUID(),
    @role_store_owner_id,
    @category_menu_id,
    1,
    1,
    0,
    NOW()
FROM dual
WHERE @role_store_owner_id IS NOT NULL
  AND @category_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM role_menu
      WHERE role_id = @role_store_owner_id
        AND menu_id = @category_menu_id
  );

INSERT INTO role_menu (
    id,
    role_id,
    menu_id,
    can_view,
    can_edit,
    can_delete,
    created_at
)
SELECT
    UUID(),
    @role_store_editor_id,
    @category_menu_id,
    1,
    1,
    0,
    NOW()
FROM dual
WHERE @role_store_editor_id IS NOT NULL
  AND @category_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM role_menu
      WHERE role_id = @role_store_editor_id
        AND menu_id = @category_menu_id
  );
