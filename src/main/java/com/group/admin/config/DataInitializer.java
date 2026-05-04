package com.group.admin.config;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.Menu;
import com.group.admin.entity.Role;
import com.group.admin.entity.RoleMenu;
import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.entity.ShippingMethod;
import com.group.admin.entity.SystemConfig;
import com.group.admin.entity.User;
import com.group.admin.example.MenuExample;
import com.group.admin.example.RoleExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.MenuMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.RoleMenuMapper;
import com.group.admin.mapper.ShippingMethodMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.mapper.SystemConfigMapper;
import com.group.admin.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 系統初始化數據載入器
 * 
 * 功能：
 * 1. 系統首次啟動時自動載入預設資料
 * 2. 使用 UUID 作為主鍵策略
 * 3. 包含：角色、選單、權限、測試帳號、測試店家、測試商品
 * 
 * 執行時機：Spring Boot 啟動完成後自動執行
 * 
 * @author KUJI System
 * @since 2025-12-18
 */
@Slf4j
@Component 
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final StoreMapper storeMapper;
    private final StoreUserMapper storeUserMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final UserMapper userMapper;
    private final LotteryMapper lotteryMapper;
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final ShippingMethodMapper shippingMethodMapper;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // UUID 常量（方便後續關聯使用）
    private String ROLE_ADMIN_ID;
    private String ROLE_STORE_OWNER_ID;
    private String ROLE_STORE_EDITOR_ID;
    
    private String ADMIN_USER_ID;
    private String STORE_OWNER_1_ID;
    private String STORE_OWNER_2_ID;
    private String STORE_EDITOR_1_ID;
    
    private String STORE_1_ID;
    private String STORE_2_ID;
    
    private String LOTTERY_1_ID;
    private String LOTTERY_2_ID;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("開始執行系統資料初始化...");
        log.info("========================================");

        // 檢查是否已有資料（避免重複初始化）
        if (isDataAlreadyInitialized()) {
            initializeSystemConfigs();
            log.info("系統資料已存在，跳過初始化");
            return;
        }

        try {
            initializeRoles();
            initializeMenus();
            initializeRoleMenuPermissions();
            initializeAdminUsers();
            initializeStores();
            initializeTestUsers();
            initializeLotteries();
            initializeShippingMethods();
            initializeSystemConfigs();
            
            log.info("========================================");
            log.info("系統資料初始化完成！");
            log.info("========================================");
            logDefaultCredentials();
            
        } catch (Exception e) {
            log.error("資料初始化失敗：{}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 初始化運送方式（可重複執行，具 idempotent）
     */
    private void initializeShippingMethods() {
        log.info("初始化運送方式...");
        insertShippingMethodIfAbsent("HOME_DELIVERY", "宅配到府", "黑貓宅急便", 100, 1);
        insertShippingMethodIfAbsent("SEVEN_ELEVEN", "7-11 取貨", "綠界", 60, 2);
        insertShippingMethodIfAbsent("FAMILY_MART", "全家取貨", "綠界", 60, 3);
        log.info("✓ 運送方式初始化完成");
    }

    private void insertShippingMethodIfAbsent(String code, String name, String provider, long fee, int sortOrder) {
        // 檢查是否已存在
        com.group.admin.example.ShippingMethodExample example = new com.group.admin.example.ShippingMethodExample();
        example.createCriteria().andCodeEqualTo(code);
        if (shippingMethodMapper.countByExample(example) > 0) {
            log.info("  ℹ️ 運送方式已存在：{}", code);
            return;
        }

        ShippingMethod method = new ShippingMethod();
        method.setId(UUID.randomUUID().toString());
        method.setCode(code);
        method.setName(name);
        method.setProvider(provider);
        method.setFee(fee);
        method.setStatus("ACTIVE");
        method.setSortOrder(sortOrder);
        method.setCreatedAt(LocalDateTime.now());
        method.setUpdatedAt(LocalDateTime.now());
        
        shippingMethodMapper.insertSelective(method);
        log.info("  ✓ 建立運送方式：code={}, name={}, fee={}", code, name, fee);
    }

    /**
     * 初始化系統參數（可重複執行，具 idempotent）
     */
    private void initializeSystemConfigs() {
        log.info("初始化系統參數...");
        insertSystemConfigIfAbsent("protection_initial_minutes", "5", "INTEGER", "DRAW", "保護初始時間（分鐘）");
        insertSystemConfigIfAbsent("protection_extension_minutes", "2", "INTEGER", "DRAW", "每次操作延長時間（分鐘）");
        insertSystemConfigIfAbsent("protection_max_minutes", "10", "INTEGER", "DRAW", "保護最大時間（分鐘）");
        insertSystemConfigIfAbsent("max_draws_per_request", "10", "INTEGER", "DRAW", "單次 API 最大抽獎數");
        log.info("✓ 系統參數初始化完成");
    }

    private void insertSystemConfigIfAbsent(String key, String value, String type, String group, String description) {
        SystemConfig existing = systemConfigMapper.selectByConfigKey(key);
        if (existing != null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        SystemConfig config = new SystemConfig();
        config.setId(UUID.randomUUID().toString());
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType(type);
        config.setConfigGroup(group);
        config.setDescription(description);
        config.setVersion(0);
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        systemConfigMapper.insert(config);
    }

    /**
     * 檢查系統是否已初始化（檢查 role 表是否有資料）
     */
    private boolean isDataAlreadyInitialized() {
        RoleExample example = new RoleExample();
        example.createCriteria().andCodeEqualTo("ROLE_ADMIN");
        return roleMapper.selectByExample(example).size() > 0;
    }

    /**
     * 初始化角色資料
     */
    private void initializeRoles() {
        log.info("初始化角色資料...");
        
        ROLE_ADMIN_ID = UUID.randomUUID().toString();
        ROLE_STORE_OWNER_ID = UUID.randomUUID().toString();
        ROLE_STORE_EDITOR_ID = UUID.randomUUID().toString();

        // Admin 角色
        Role adminRole = new Role();
        adminRole.setId(ROLE_ADMIN_ID);
        adminRole.setName("系統管理員");
        adminRole.setCode("ROLE_ADMIN");
        adminRole.setDescription("平台最高權限管理者，可管理所有店家與系統設定");
        adminRole.setCreatedAt(LocalDateTime.now());
        adminRole.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(adminRole);

        // StoreOwner 角色
        Role ownerRole = new Role();
        ownerRole.setId(ROLE_STORE_OWNER_ID);
        ownerRole.setName("店家負責人");
        ownerRole.setCode("ROLE_STORE_OWNER");
        ownerRole.setDescription("店家主帳號，可管理自己店家的商品、訂單與報表");
        ownerRole.setCreatedAt(LocalDateTime.now());
        ownerRole.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(ownerRole);

        // StoreEditor 角色
        Role editorRole = new Role();
        editorRole.setId(ROLE_STORE_EDITOR_ID);
        editorRole.setName("店家編輯");
        editorRole.setCode("ROLE_STORE_EDITOR");
        editorRole.setDescription("店家小編帳號，僅能編輯商品與查看訂單");
        editorRole.setCreatedAt(LocalDateTime.now());
        editorRole.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(editorRole);

        log.info("✓ 角色資料初始化完成（3 筆）");
    }

    /**
     * 初始化選單資料
     */
    private void initializeMenus() {
        log.info("初始化選單資料...");
        
        // 第一層選單
        String[] menuIds = new String[7];
        String[] menuNames = {"店家管理", "商品管理", "訂單管理", "會員管理", "報表中心", "權限管理", "系統設定"};
        String[] menuCodes = {"STORE_MANAGEMENT", "LOTTERY_MANAGEMENT", "ORDER_MANAGEMENT", 
                               "USER_MANAGEMENT", "REPORT_CENTER", "PERMISSION_MANAGEMENT", "SYSTEM_SETTING"};
        String[] menuPaths = {"/admin/stores", "/admin/lotteries", "/admin/orders", 
                               "/admin/users", "/admin/reports", "/admin/permissions", "/admin/system"};
        String[] menuIcons = {"store", "shopping", "receipt", "people", "chart", "security", "setting"};

        for (int i = 0; i < menuNames.length; i++) {
            menuIds[i] = UUID.randomUUID().toString();
            Menu menu = new Menu();
            menu.setId(menuIds[i]);
            menu.setName(menuNames[i]);
            menu.setCode(menuCodes[i]);
            menu.setPath(menuPaths[i]);
            menu.setParentId(null);
            menu.setIcon(menuIcons[i]);
            menu.setOrderNum(i + 1);
            menu.setIsVisible(true);
            menu.setCreatedAt(LocalDateTime.now());
            menu.setUpdatedAt(LocalDateTime.now());
            menuMapper.insert(menu);
        }

        // 第二層選單 - 店家管理
        insertSubMenu(menuIds[0], "店家列表", "STORE_LIST", "/admin/stores/list", 1);
        insertSubMenu(menuIds[0], "新增店家", "STORE_CREATE", "/admin/stores/create", 2);

        // 第二層選單 - 商品管理
        insertSubMenu(menuIds[1], "商品列表", "LOTTERY_LIST", "/admin/lotteries/list", 1);
        insertSubMenu(menuIds[1], "新增商品", "LOTTERY_CREATE", "/admin/lotteries/create", 2);
        insertSubMenu(menuIds[1], "獎品管理", "PRIZE_MANAGEMENT", "/admin/prizes", 3);

        // 第二層選單 - 訂單管理
        insertSubMenu(menuIds[2], "訂單列表", "ORDER_LIST", "/admin/orders/list", 1);
        insertSubMenu(menuIds[2], "配送管理", "SHIPPING_MANAGEMENT", "/admin/shipping", 2);

        // 第二層選單 - 報表中心
        insertSubMenu(menuIds[4], "營收報表", "REVENUE_REPORT", "/admin/reports/revenue", 1);
        insertSubMenu(menuIds[4], "抽獎統計", "DRAW_STATISTICS", "/admin/reports/draw-stats", 2);

        // 第二層選單 - 權限管理
        insertSubMenu(menuIds[5], "角色管理", "ROLE_MANAGEMENT", "/admin/permissions/roles", 1);
        insertSubMenu(menuIds[5], "選單管理", "MENU_MANAGEMENT", "/admin/permissions/menus", 2);
        insertSubMenu(menuIds[5], "帳號管理", "ACCOUNT_MANAGEMENT", "/admin/permissions/accounts", 3);

        log.info("✓ 選單資料初始化完成（19 筆）");
    }

    private void insertSubMenu(String parentId, String name, String code, String path, int orderNum) {
        Menu menu = new Menu();
        menu.setId(UUID.randomUUID().toString());
        menu.setName(name);
        menu.setCode(code);
        menu.setPath(path);
        menu.setParentId(parentId);
        menu.setIcon(null);
        menu.setOrderNum(orderNum);
        menu.setIsVisible(true);
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.insert(menu);
    }

    /**
     * 初始化角色選單權限
     */
    private void initializeRoleMenuPermissions() {
        log.info("初始化角色權限資料...");
        
        // Admin 擁有所有選單的完整權限
        MenuExample menuExample = new MenuExample();
        menuExample.createCriteria();
        for (Menu menu : menuMapper.selectByExample(menuExample)) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setId(UUID.randomUUID().toString());
            roleMenu.setRoleId(ROLE_ADMIN_ID);
            roleMenu.setMenuId(menu.getId());
            roleMenu.setCanView(true);
            roleMenu.setCanEdit(true);
            roleMenu.setCanDelete(true);
            roleMenu.setCreatedAt(LocalDateTime.now());
            roleMenuMapper.insert(roleMenu);
        }

        // StoreOwner 權限（不含權限管理）
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "LOTTERY_MANAGEMENT", true, true, true);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "LOTTERY_LIST", true, true, true);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "LOTTERY_CREATE", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "PRIZE_MANAGEMENT", true, true, true);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "ORDER_MANAGEMENT", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "ORDER_LIST", true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "SHIPPING_MANAGEMENT", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "REPORT_CENTER", true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "REVENUE_REPORT", true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "DRAW_STATISTICS", true, false, false);

        // StoreEditor 權限（僅商品與訂單查看）
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "LOTTERY_MANAGEMENT", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "LOTTERY_LIST", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "PRIZE_MANAGEMENT", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "ORDER_MANAGEMENT", true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "ORDER_LIST", true, false, false);

        log.info("✓ 角色權限資料初始化完成");
    }

    private void assignMenuPermissionByCode(String roleId, String menuCode, boolean canView, boolean canEdit, boolean canDelete) {
        MenuExample example = new MenuExample();
        example.createCriteria().andCodeEqualTo(menuCode);
        Menu menu = menuMapper.selectByExample(example).stream().findFirst().orElse(null);
        if (menu != null) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setId(UUID.randomUUID().toString());
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menu.getId());
            roleMenu.setCanView(canView);
            roleMenu.setCanEdit(canEdit);
            roleMenu.setCanDelete(canDelete);
            roleMenu.setCreatedAt(LocalDateTime.now());
            roleMenuMapper.insert(roleMenu);
        }
    }

    /**
     * 初始化管理者帳號
     */
    private void initializeAdminUsers() {
        log.info("初始化管理者帳號...");
        
        ADMIN_USER_ID = UUID.randomUUID().toString();
        STORE_OWNER_1_ID = UUID.randomUUID().toString();
        STORE_OWNER_2_ID = UUID.randomUUID().toString();
        STORE_EDITOR_1_ID = UUID.randomUUID().toString();

        // 系統管理員（密碼: admin123）
        AdminUser admin = new AdminUser();
        admin.setId(ADMIN_USER_ID);
        admin.setUsername("admin@kuji.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@kuji.com");
        admin.setDisplayName("系統管理員");
        admin.setPhone("0900000000");
        admin.setStatus("ACTIVE");
        admin.setForceChangePassword(false);
        admin.setCreatedBy(null);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setFailedLoginAttempts(0);
        adminUserMapper.insert(admin);
        assignRole(ADMIN_USER_ID, ROLE_ADMIN_ID);

        // 測試店家負責人 1（密碼: Test1234）
        AdminUser owner1 = new AdminUser();
        owner1.setId(STORE_OWNER_1_ID);
        owner1.setUsername("owner@teststore.com");
        owner1.setPassword(passwordEncoder.encode("Test1234"));
        owner1.setEmail("owner@teststore.com");
        owner1.setDisplayName("測試店家老闆");
        owner1.setPhone("0911111111");
        owner1.setStatus("ACTIVE");
        owner1.setForceChangePassword(false);
        owner1.setCreatedBy(ADMIN_USER_ID);
        owner1.setRemark("測試用店家負責人");
        owner1.setCreatedAt(LocalDateTime.now());
        owner1.setUpdatedAt(LocalDateTime.now());
        owner1.setFailedLoginAttempts(0);
        adminUserMapper.insert(owner1);
        assignRole(STORE_OWNER_1_ID, ROLE_STORE_OWNER_ID);

        // 測試店家負責人 2（密碼: Test1234）
        AdminUser owner2 = new AdminUser();
        owner2.setId(STORE_OWNER_2_ID);
        owner2.setUsername("owner2@teststore.com");
        owner2.setPassword(passwordEncoder.encode("Test1234"));
        owner2.setEmail("owner2@teststore.com");
        owner2.setDisplayName("第二間店家老闆");
        owner2.setPhone("0922222222");
        owner2.setStatus("ACTIVE");
        owner2.setForceChangePassword(false);
        owner2.setCreatedBy(ADMIN_USER_ID);
        owner2.setRemark("測試用店家負責人");
        owner2.setCreatedAt(LocalDateTime.now());
        owner2.setUpdatedAt(LocalDateTime.now());
        owner2.setFailedLoginAttempts(0);
        adminUserMapper.insert(owner2);
        assignRole(STORE_OWNER_2_ID, ROLE_STORE_OWNER_ID);

        // 測試店家編輯（密碼: Test1234）
        AdminUser editor = new AdminUser();
        editor.setId(STORE_EDITOR_1_ID);
        editor.setUsername("editor@teststore.com");
        editor.setPassword(passwordEncoder.encode("Test1234"));
        editor.setEmail("editor@teststore.com");
        editor.setDisplayName("測試店家小編");
        editor.setPhone("0933333333");
        editor.setStatus("ACTIVE");
        editor.setForceChangePassword(false);
        editor.setCreatedBy(ADMIN_USER_ID);
        editor.setRemark("測試用店家編輯人員");
        editor.setCreatedAt(LocalDateTime.now());
        editor.setUpdatedAt(LocalDateTime.now());
        editor.setFailedLoginAttempts(0);
        adminUserMapper.insert(editor);
        assignRole(STORE_EDITOR_1_ID, ROLE_STORE_EDITOR_ID);

        log.info("✓ 管理者帳號初始化完成（4 筆）");
    }

    private void assignRole(String adminUserId, String roleId) {
        AdminUserRole userRole = new AdminUserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setAdminUserId(adminUserId);
        userRole.setRoleId(roleId);
        userRole.setCreatedAt(LocalDateTime.now());
        adminUserRoleMapper.insert(userRole);
    }

    /**
     * 初始化店家資料
     */
    private void initializeStores() {
        log.info("初始化店家資料...");
        
        STORE_1_ID = UUID.randomUUID().toString();
        STORE_2_ID = UUID.randomUUID().toString();

        // KUJI 測試商店
        Store store1 = new Store();
        store1.setId(STORE_1_ID);
        store1.setOwnerId(STORE_OWNER_1_ID);
        store1.setStoreName("KUJI 測試商店");
        store1.setShortDescription("最好玩的抽獎商店");
        store1.setLongDescription("這是一間專門販售各種精美獎品的抽獎商店，歡迎來試手氣！");
        store1.setLogoUrl("https://via.placeholder.com/200");
        store1.setCoverImageUrl("https://via.placeholder.com/1200x400");
        store1.setEmail("owner@teststore.com");
        store1.setPhone("0911111111");
        store1.setAddress("台北市信義區信義路五段7號");
        store1.setFacebookUrl("https://facebook.com/kujitest");
        store1.setInstagramUrl("https://instagram.com/kujitest");
        store1.setLineId("@kujitest");
        store1.setBusinessHours("每日 10:00~22:00");
        store1.setStatus("ACTIVE");
        store1.setRemark("測試用店家");
        store1.setCreatedAt(LocalDateTime.now());
        store1.setUpdatedAt(LocalDateTime.now());
        store1.setUpdatedBy(ADMIN_USER_ID);
        storeMapper.insert(store1);
        
        // 店家使用者關聯
        StoreUser storeUser1 = new StoreUser();
        storeUser1.setId(UUID.randomUUID().toString());
        storeUser1.setStoreId(STORE_1_ID);
        storeUser1.setAdminUserId(STORE_OWNER_1_ID);
        storeUser1.setRoleType("OWNER");
        storeUser1.setCreatedAt(LocalDateTime.now());
        storeUserMapper.insert(storeUser1);

        StoreUser storeUser2 = new StoreUser();
        storeUser2.setId(UUID.randomUUID().toString());
        storeUser2.setStoreId(STORE_1_ID);
        storeUser2.setAdminUserId(STORE_EDITOR_1_ID);
        storeUser2.setRoleType("EDITOR");
        storeUser2.setCreatedAt(LocalDateTime.now());
        storeUserMapper.insert(storeUser2);

        // 動漫周邊專賣店
        Store store2 = new Store();
        store2.setId(STORE_2_ID);
        store2.setOwnerId(STORE_OWNER_2_ID);
        store2.setStoreName("動漫周邊專賣店");
        store2.setShortDescription("動漫迷必逛的抽獎店");
        store2.setLongDescription("專營日本動漫周邊、公仔、模型等精品，採用一番賞抽獎機制。");
        store2.setLogoUrl("https://via.placeholder.com/200");
        store2.setCoverImageUrl("https://via.placeholder.com/1200x400");
        store2.setEmail("owner2@teststore.com");
        store2.setPhone("0922222222");
        store2.setAddress("台北市中山區南京東路三段168號");
        store2.setFacebookUrl("https://facebook.com/animestore");
        store2.setInstagramUrl("https://instagram.com/animestore");
        store2.setLineId("@animestore");
        store2.setBusinessHours("每日 11:00~21:00");
        store2.setStatus("ACTIVE");
        store2.setRemark("測試用店家");
        store2.setCreatedAt(LocalDateTime.now());
        store2.setUpdatedAt(LocalDateTime.now());
        store2.setUpdatedBy(ADMIN_USER_ID);
        storeMapper.insert(store2);

        StoreUser storeUser3 = new StoreUser();
        storeUser3.setId(UUID.randomUUID().toString());
        storeUser3.setStoreId(STORE_2_ID);
        storeUser3.setAdminUserId(STORE_OWNER_2_ID);
        storeUser3.setRoleType("OWNER");
        storeUser3.setCreatedAt(LocalDateTime.now());
        storeUserMapper.insert(storeUser3);

        log.info("✓ 店家資料初始化完成（2 筆）");
    }

    /**
     * 初始化測試會員
     */
    private void initializeTestUsers() {
        log.info("初始化測試會員...");
        
        // 測試會員 A（密碼: Test1234）
        User user1 = new User();
        user1.setId(UUID.randomUUID().toString());
        user1.setEmail("user1@test.com");
        user1.setPassword(passwordEncoder.encode("Test1234"));
        user1.setNickname("測試會員A");
        user1.setAvatar("https://via.placeholder.com/100");
        user1.setProvider("EMAIL");
        user1.setProviderId(null);
        user1.setStatus("ACTIVE");
        user1.setPhoneNumber("0955555555");
        user1.setGoldCoins(1000L);
        user1.setBonusCoins(500L);
        user1.setFailedLoginAttempts(0);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user1);

        // 測試會員 B（密碼: Test1234）
        User user2 = new User();
        user2.setId(UUID.randomUUID().toString());
        user2.setEmail("user2@test.com");
        user2.setPassword(passwordEncoder.encode("Test1234"));
        user2.setNickname("測試會員B");
        user2.setAvatar("https://via.placeholder.com/100");
        user2.setProvider("EMAIL");
        user2.setProviderId(null);
        user2.setStatus("ACTIVE");
        user2.setPhoneNumber("0966666666");
        user2.setGoldCoins(2500L);
        user2.setBonusCoins(300L);
        user2.setFailedLoginAttempts(0);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user2);

        // Google 測試會員
        User user3 = new User();
        user3.setId(UUID.randomUUID().toString());
        user3.setEmail("googleuser@gmail.com");
        user3.setPassword(null);
        user3.setNickname("Google 測試會員");
        user3.setAvatar("https://via.placeholder.com/100");
        user3.setProvider("GOOGLE");
        user3.setProviderId("google_oauth_id_12345");
        user3.setStatus("ACTIVE");
        user3.setPhoneNumber(null);
        user3.setGoldCoins(500L);
        user3.setBonusCoins(100L);
        user3.setFailedLoginAttempts(0);
        user3.setCreatedAt(LocalDateTime.now());
        user3.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user3);

        log.info("✓ 測試會員初始化完成（3 筆）");
    }

    /**
     * 初始化測試抽獎商品與獎品
     */
    private void initializeLotteries() {
        log.info("初始化抽獎商品...");
        
        LOTTERY_1_ID = UUID.randomUUID().toString();
        LOTTERY_2_ID = UUID.randomUUID().toString();

        // 鬼滅之刃一番賞
        Lottery lottery1 = new Lottery();
        lottery1.setId(LOTTERY_1_ID);
        lottery1.setStoreId(STORE_1_ID);
        lottery1.setTitle("鬼滅之刃一番賞");
        lottery1.setDescription("超人氣鬼滅之刃一番賞，多款精美公仔等你來抽！");
        lottery1.setCategory("OFFICIAL_ICHIBAN");
        lottery1.setSubCategory("LOTTERY_MODE");
        lottery1.setStatus("ON_SHELF");
        lottery1.setPricePerDraw(80L);
        lottery1.setDiscountedPrice(720L);
        lottery1.setAutoDiscountEnabled((byte) 0);
        lottery1.setAllowMultiDraw((byte) 1);
        lottery1.setMultiDrawOptions("5,10");
        lottery1.setScheduledAt(LocalDateTime.now());
        lottery1.setStartTime(LocalDateTime.now());
        lottery1.setEndTime(LocalDateTime.now().plusDays(30));
        lottery1.setTotalDraws(0);
        lottery1.setMaxDraws(80);
        lottery1.setOrderNum(1);
        lottery1.setWeight(100);
        lottery1.setImageUrl("https://via.placeholder.com/400x300");
        lottery1.setCreatedBy(STORE_OWNER_1_ID);
        lottery1.setRemark("測試用商品");
        lottery1.setCreatedAt(LocalDateTime.now());
        lottery1.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.insert(lottery1);

        // 鬼滅之刃獎品
        insertPrize(LOTTERY_1_ID, "A", "炭治郎公仔（大）", "約 25cm 高品質公仔", 1, 1, "FIGURE", 1, 0, 0);
        insertPrize(LOTTERY_1_ID, "B", "禰豆子公仔（大）", "約 25cm 高品質公仔", 1, 1, "FIGURE", 2, 0, 0);
        insertPrize(LOTTERY_1_ID, "C", "善逸公仔", "約 18cm 精緻公仔", 3, 3, "FIGURE", 3, 0, 0);
        insertPrize(LOTTERY_1_ID, "D", "伊之助公仔", "約 18cm 精緻公仔", 5, 4, "FIGURE", 4, 0, 0);
        insertPrize(LOTTERY_1_ID, "E", "壓克力立牌", "隨機角色壓克力立牌", 20, 18, "GOODS", 5, 0, 0);
        insertPrize(LOTTERY_1_ID, "F", "徽章組", "隨機 3 入徽章組", 30, 25, "GOODS", 6, 0, 0);
        insertPrize(LOTTERY_1_ID, "G", "貼紙包", "隨機貼紙包", 19, 12, "GOODS", 7, 0, 0);
        insertPrize(LOTTERY_1_ID, "LAST_PRIZE", "特別版炭治郎公仔", "最後一抽限定公仔", 1, 1, "FIGURE", 8, 1, 0);

        // 咒術迴戰刮刮樂
        Lottery lottery2 = new Lottery();
        lottery2.setId(LOTTERY_2_ID);
        lottery2.setStoreId(STORE_1_ID);
        lottery2.setTitle("咒術迴戰刮刮樂");
        lottery2.setDescription("咒術迴戰限定刮刮樂，每張都有獎！");
        lottery2.setCategory("OFFICIAL_ICHIBAN");
        lottery2.setSubCategory("SCRATCH_CARD_MODE");
        lottery2.setStatus("ON_SHELF");
        lottery2.setPricePerDraw(60L);
        lottery2.setDiscountedPrice(540L);
        lottery2.setAutoDiscountEnabled((byte) 0);
        lottery2.setAllowMultiDraw((byte) 1);
        lottery2.setMultiDrawOptions("5,10");
        lottery2.setScheduledAt(LocalDateTime.now());
        lottery2.setStartTime(LocalDateTime.now());
        lottery2.setEndTime(LocalDateTime.now().plusDays(60));
        lottery2.setTotalDraws(0);
        lottery2.setMaxDraws(100);
        lottery2.setOrderNum(2);
        lottery2.setWeight(90);
        lottery2.setImageUrl("https://via.placeholder.com/400x300");
        lottery2.setCreatedBy(STORE_OWNER_1_ID);
        lottery2.setRemark("測試用商品");
        lottery2.setCreatedAt(LocalDateTime.now());
        lottery2.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.insert(lottery2);

        // 咒術迴戰獎品
        insertPrize(LOTTERY_2_ID, "A", "五條悟公仔", "約 20cm 公仔", 2, 2, "FIGURE", 1, 0, 1);
        insertPrize(LOTTERY_2_ID, "B", "虎杖悠仁公仔", "約 18cm 公仔", 5, 5, "FIGURE", 2, 0, 0);
        insertPrize(LOTTERY_2_ID, "C", "壓克力鑰匙圈", "隨機角色鑰匙圈", 20, 20, "GOODS", 3, 0, 0);
        insertPrize(LOTTERY_2_ID, "D", "透明資料夾", "隨機角色資料夾", 33, 33, "GOODS", 4, 0, 0);
        insertPrize(LOTTERY_2_ID, "E", "小貼紙", "隨機小貼紙", 40, 40, "GOODS", 5, 0, 0);

        log.info("✓ 抽獎商品與獎品初始化完成（2 個商品, 13 個獎品）");
    }

    private void insertPrize(String lotteryId, String level, String name, String description, 
                             int quantity, int remaining, String prizeType, int orderNum, 
                             int isLastPrize, int isGrandPrize) {
        LotteryPrize prize = new LotteryPrize();
        prize.setId(UUID.randomUUID().toString());
        prize.setLotteryId(lotteryId);
        prize.setLevel(level);
        prize.setName(name);
        prize.setDescription(description);
        prize.setQuantity(quantity);
        prize.setRemaining(remaining);
        prize.setImageUrl("https://via.placeholder.com/200");
        prize.setPrizeType(prizeType);
        prize.setOrderNum(orderNum);
        prize.setIsLastPrize((byte) isLastPrize);
        prize.setIsGrandPrize((byte) isGrandPrize);
        prize.setCreatedAt(LocalDateTime.now());
        prize.setUpdatedAt(LocalDateTime.now());
        lotteryPrizeMapper.insert(prize);
    }

    /**
     * 記錄預設帳號資訊
     */
    private void logDefaultCredentials() {
        log.info("");
        log.info("========================================");
        log.info("預設測試帳號資訊");
        log.info("========================================");
        log.info("【後台管理者】");
        log.info("  1. Admin: admin@kuji.com / admin123");
        log.info("  2. StoreOwner1: owner@teststore.com / Test1234");
        log.info("  3. StoreOwner2: owner2@teststore.com / Test1234");
        log.info("  4. StoreEditor: editor@teststore.com / Test1234");
        log.info("");
        log.info("【前台會員】");
        log.info("  1. user1@test.com / Test1234 (金點 1000, 紅利 500)");
        log.info("  2. user2@test.com / Test1234 (金點 2500, 紅利 300)");
        log.info("  3. googleuser@gmail.com (Google 登入, 金點 500, 紅利 100)");
        log.info("");
        log.info("【測試店家】");
        log.info("  1. KUJI 測試商店 (Owner: owner@teststore.com)");
        log.info("  2. 動漫周邊專賣店 (Owner: owner2@teststore.com)");
        log.info("");
        log.info("【測試商品】");
        log.info("  1. 鬼滅之刃一番賞 (80 抽, 已上架)");
        log.info("  2. 咒術迴戰刮刮樂 (100 抽, 已上架)");
        log.info("========================================");
    }
}
