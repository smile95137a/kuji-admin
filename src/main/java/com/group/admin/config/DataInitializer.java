package com.group.admin.config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.LotteryTag;
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
import com.group.admin.example.RoleMenuExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.LotteryTagMapper;
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
 * 資料初始化器
 * 
 * 說明：
 * 1. 首次啟動時自動建立角色、選單、帳號等基礎資料
 * 2. 使用 UUID 作為主鍵策略
 * 3. 包含測試用帳號、商店、商品及對應關聯
 * 
 * 注意事項：Spring Boot 啟動時自動執行
 * 
 * @author KUJI System
 * @since 2025-12-18
 */
@Slf4j
@Component 
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String REPORT_PARENT_CODE = "REPORT_CENTER";
    private static final String[][] REPORT_MENU_DEFINITIONS = {
            {"營收報表", "REVENUE_REPORT", "/home/report/revenue", "1"},
            {"推薦獎金報表", "REFERRAL_REPORT", "/home/report/referral", "2"},
            {"抽獎結果報表", "LOTTERY_RESULT_REPORT", "/home/report/lottery-result", "3", "DRAW_STATISTICS"},
            {"儲值報表", "RECHARGE_REPORT", "/home/report/recharge", "4"},
            {"紅利報表", "BONUS_REPORT", "/home/report/bonus", "5"},
            {"會員成長報表", "MEMBER_GROWTH_REPORT", "/home/report/member-growth", "6"},
            {"平台營收報表", "PLATFORM_REVENUE_REPORT", "/home/report/platform-revenue", "7"},
            {"抽獎銷售報表", "LOTTERY_SALES_REPORT", "/home/report/lottery-sales", "8"},
            {"店鋪績效報表", "STORE_PERFORMANCE_REPORT", "/home/report/store-performance", "9", "STORE_PERF_REPORT"},
            {"獎品出貨報表", "PRIZE_SHIPMENT_REPORT", "/home/report/prize-shipment", "10"}
    };
    private static final List<String> DEFAULT_LOTTERY_TAGS = Arrays.asList(
            "熱門",
            "新品",
            "現貨",
            "公仔",
            "模型",
            "動漫",
            "盲盒",
            "一番賞",
            "周邊",
            "收藏");

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
    private final LotteryTagMapper lotteryTagMapper;
    private final ShippingMethodMapper shippingMethodMapper;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // UUID 欄位：在初始化時動態生成，非靜態常數
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
        log.info("開始執行資料初始化...");
        log.info("========================================");

        // 如果資料已初始化，僅執行補資料與校正流程
        if (isDataAlreadyInitialized()) {
            initializeSystemConfigs();
            ensureDefaultLotteryTags();
            // 若 role_menu 為空，補建角色選單權限
            if (isRoleMenuEmpty()) {
                log.warn("偵測到 role_menu 為空，開始補建角色選單權限...");
                loadRoleIdsFromDb();
                initializeRoleMenuPermissions();
                log.info("role_menu 補建完成");
            }
            // 補齊報表相關選單
            deduplicateMenusByCode();
            rescueMissingReportMenus();
            // 補齊系統設定相關選單
            rescueMissingSystemMenus();
            rescueMissingOperationMenus();
            rescueMissingWebsiteContentMenus();
            // 補齊會員管理選單
            rescueMissingMemberMenus();
            // 補齊分類管理選單
            rescueMissingCategoryMenu();
            log.info("資料初始化檢查完成，已補齊缺漏資料");
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
            ensureDefaultLotteryTags();
            deduplicateMenusByCode();
            rescueMissingReportMenus();
            rescueMissingSystemMenus();
            rescueMissingOperationMenus();
            rescueMissingWebsiteContentMenus();
            rescueMissingMemberMenus();
            rescueMissingCategoryMenu();
            
            log.info("========================================");
            log.info("資料初始化完成");
            log.info("========================================");
            logDefaultCredentials();
            
        } catch (Exception e) {
            log.error("資料初始化失敗：{}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 初始化配送方式（冪等）
     */
    private void initializeShippingMethods() {
        log.info("初始化配送方式...");
        insertShippingMethodIfAbsent("HOME_DELIVERY", "宅配到府", "黑貓宅急便", 100, 1);
        insertShippingMethodIfAbsent("SEVEN_ELEVEN", "7-11 超商取貨", "統一超商", 60, 2);
        insertShippingMethodIfAbsent("FAMILY_MART", "全家超商取貨", "全家便利商店", 60, 3);
        log.info("配送方式初始化完成");
    }

    private void insertShippingMethodIfAbsent(String code, String name, String provider, long fee, int sortOrder) {
        // 檢查是否已存在
        com.group.admin.example.ShippingMethodExample example = new com.group.admin.example.ShippingMethodExample();
        example.createCriteria().andCodeEqualTo(code);
        if (shippingMethodMapper.countByExample(example) > 0) {
            log.info("  已存在，跳過配送方式：{}", code);
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
        log.info("  新增配送方式：code={}, name={}, fee={}", code, name, fee);
    }

    /**
     * 新增系統配置如果不存在（冪等）
     */
    private void initializeSystemConfigs() {
        log.info("初始化系統參數...");
        // protection_initial_minutes / protection_extension_minutes / protection_max_minutes 已廢棄
        // 實際保護時間由 draw_protection_base_seconds 等秒數參數控制
        insertSystemConfigIfAbsent("max_draws_per_request", "10", "INTEGER", "DRAW", "單次 API 最大抽數");
        insertSystemConfigIfAbsent("draw_protection_base_seconds", "300", "INTEGER", "DRAW", "單抽保護期基礎秒數");
        insertSystemConfigIfAbsent("draw_protection_extra_seconds_per_draw", "30", "INTEGER", "DRAW", "每次抽獎請求額外增加的保護秒數");
        insertSystemConfigIfAbsent("draw_protection_max_seconds", "600", "INTEGER", "DRAW", "抽獎保護期最大秒數");
        insertSystemConfigIfAbsent("draw_bonus_title", "多抽優惠", "STRING", "DRAW", "抽獎頁 BONUS 區塊標題");
        insertSystemConfigIfAbsent("draw_bonus_description", "現貨在倉，限定帶抽才會贈送。", "STRING", "DRAW", "抽獎頁 BONUS 區塊說明");
        insertSystemConfigIfAbsent("draw_protection_title", "保護期說明", "STRING", "DRAW", "抽獎頁保護期區塊標題");
        insertSystemConfigIfAbsent("draw_protection_description", "第一次抽獎保護 300 秒，每次再抽額外延長 30 秒，上限 600 秒。", "STRING", "DRAW", "抽獎頁保護期說明文字");
        insertSystemConfigIfAbsent("draw_bonus_tiers_json", "3:60\n5:120\n8:180\n10:240", "STRING", "DRAW", "多抽贈送紅利階梯設定（格式：抽數:紅利，每行一組）");
        log.info("系統參數初始化完成");
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
     * 檢查資料是否已初始化，依據 role 表是否有資料
     */
    private boolean isDataAlreadyInitialized() {
        RoleExample example = new RoleExample();
        example.createCriteria().andCodeEqualTo("ROLE_ADMIN");
        return roleMapper.selectByExample(example).size() > 0;
    }

    /**
     * 檢查 role_menu 表是否為空（首次執行判斷）
     */
    private boolean isRoleMenuEmpty() {
        RoleMenuExample example = new RoleMenuExample();
        return roleMenuMapper.countByExample(example) == 0;
    }

    /**
     * 從 DB 載入角色 ID，用於補救程式重啟時的狀態
     */
    private void loadRoleIdsFromDb() {
        RoleExample ex = new RoleExample();
        ex.createCriteria().andCodeEqualTo("ROLE_ADMIN");
        roleMapper.selectByExample(ex).stream().findFirst().ifPresent(r -> ROLE_ADMIN_ID = r.getId());

        ex = new RoleExample();
        ex.createCriteria().andCodeEqualTo("ROLE_STORE_OWNER");
        roleMapper.selectByExample(ex).stream().findFirst().ifPresent(r -> ROLE_STORE_OWNER_ID = r.getId());

        ex = new RoleExample();
        ex.createCriteria().andCodeEqualTo("ROLE_STORE_EDITOR");
        roleMapper.selectByExample(ex).stream().findFirst().ifPresent(r -> ROLE_STORE_EDITOR_ID = r.getId());

        log.info("從 DB 載入角色 ID: ADMIN={}, STORE_OWNER={}, STORE_EDITOR={}",
                ROLE_ADMIN_ID, ROLE_STORE_OWNER_ID, ROLE_STORE_EDITOR_ID);
    }

    /**
     * 初始化角色
     */
    private void initializeRoles() {
        log.info("初始化角色..");
        
        ROLE_ADMIN_ID = UUID.randomUUID().toString();
        ROLE_STORE_OWNER_ID = UUID.randomUUID().toString();
        ROLE_STORE_EDITOR_ID = UUID.randomUUID().toString();

        // Admin 角色
        Role adminRole = new Role();
        adminRole.setId(ROLE_ADMIN_ID);
        adminRole.setName("系統管理員");
        adminRole.setCode("ROLE_ADMIN");
        adminRole.setDescription("擁有最高權限，可以管理整個後台系統");
        adminRole.setCreatedAt(LocalDateTime.now());
        adminRole.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(adminRole);

        // StoreOwner 角色
        Role ownerRole = new Role();
        ownerRole.setId(ROLE_STORE_OWNER_ID);
        ownerRole.setName("店家負責人");
        ownerRole.setCode("ROLE_STORE_OWNER");
        ownerRole.setDescription("店家負責人，擁有自己店家的完整管理權限");
        ownerRole.setCreatedAt(LocalDateTime.now());
        ownerRole.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(ownerRole);

        // StoreEditor 角色
        Role editorRole = new Role();
        editorRole.setId(ROLE_STORE_EDITOR_ID);
        editorRole.setName("店家編輯員");
        editorRole.setCode("ROLE_STORE_EDITOR");
        editorRole.setDescription("店家編輯員，可以管理商品但無刪除權限");
        editorRole.setCreatedAt(LocalDateTime.now());
        editorRole.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(editorRole);

        log.info("角色初始化完成，共 3 筆");
    }

    /**
     * 初始化選單
     */
    private void initializeMenus() {
        log.info("初始化選單...");
        
        // 建立頂層選單（orderNum 以 10 為間距）
        // 0:店鋪管理 1:抽獎管理 2:訂單管理 3:會員管理 4:營運工具 5:網站內容管理 6:報表中心 7:系統設定 8:權限管理
        String[] menuIds = new String[9];
        String[] menuNames = {"店鋪管理", "抽獎管理", "訂單管理", "會員管理", "營運工具",
                               "網站內容管理", "報表中心", "系統設定", "權限管理"};
        String[] menuCodes = {"STORE_MANAGEMENT", "LOTTERY_MANAGEMENT", "ORDER_MANAGEMENT",
                               "USER_MANAGEMENT", "OPERATION_TOOLS", "WEBSITE_CONTENT_MANAGEMENT",
                               "REPORT_CENTER", "SYSTEM_SETTING", "PERMISSION_MANAGEMENT"};
        String[] menuPaths = {"/home/stores", "/home/lottery-with-prizes", "/home/order",
                               "/home/member/list", "/home/recharge-plan", "/home/banner",
                               "/home/report", "/home/system-config", "/home/roles"};
        String[] menuIcons = {"store", "shopping", "receipt", "people", "build", "public", "chart", "setting", "security"};
        int[]    orderNums = {10, 20, 30, 40, 50, 60, 70, 80, 90};

        for (int i = 0; i < menuNames.length; i++) {
            menuIds[i] = UUID.randomUUID().toString();
            Menu menu = new Menu();
            menu.setId(menuIds[i]);
            menu.setName(menuNames[i]);
            menu.setCode(menuCodes[i]);
            menu.setPath(menuPaths[i]);
            menu.setParentId(null);
            menu.setIcon(menuIcons[i]);
            menu.setOrderNum(orderNums[i]);
            menu.setIsVisible(true);
            menu.setCreatedAt(LocalDateTime.now());
            menu.setUpdatedAt(LocalDateTime.now());
            menuMapper.insert(menu);
        }

        // 建立子選單 - 店鋪管理 (index 0)
        insertSubMenu(menuIds[0], "店鋪列表", "STORE_LIST", "/home/stores", 1);
        insertSubMenu(menuIds[0], "新增店鋪", "STORE_CREATE", "/home/stores/add", 2);

        // 建立子選單 - 抽獎管理 (index 1)
        insertSubMenu(menuIds[1], "商品與獎品列表", "LOTTERY_LIST", "/home/lottery-with-prizes", 1);
        insertSubMenu(menuIds[1], "新增抽獎", "LOTTERY_CREATE", "/home/lottery-with-prizes/add", 2);
        insertSubMenu(menuIds[1], "獎項管理", "PRIZE_MANAGEMENT", "/home/lottery-with-prizes", 3);
        insertSubMenu(menuIds[1], "分類管理", "CATEGORY_MANAGEMENT", "/home/category", 4);

        // 建立子選單 - 訂單管理 (index 2)
        insertSubMenu(menuIds[2], "訂單列表", "ORDER_LIST", "/home/order", 1);
        insertSubMenu(menuIds[2], "物流管理", "SHIPPING_MANAGEMENT", "/home/order", 2);

        // 建立子選單 - 會員管理 (index 3)
        insertSubMenu(menuIds[3], "前台使用者", "MEMBER_LIST", "/home/member/list", 1);
        insertSubMenu(menuIds[3], "後台使用者", "ADMIN_USER_LIST", "/home/admin-users", 2);

        // 建立子選單 - 營運工具 (index 4)
        insertSubMenu(menuIds[4], "儲值方案", "RECHARGE_MANAGE", "/home/recharge-plan", 1);
        insertSubMenu(menuIds[4], "推薦碼管理", "REFERRAL_CODE_MANAGE", "/home/referral-codes", 2);

        // 建立子選單 - 網站內容管理 (index 5)
        insertSubMenu(menuIds[5], "Banner 管理", "BANNER_MANAGE", "/home/banner", 1);
        insertSubMenu(menuIds[5], "最新消息", "NEWS_MANAGE", "/home/news", 2);

        // 建立子選單 - 報表中心 (index 6)
        insertSubMenu(menuIds[6], "營收報表",     "REVENUE_REPORT",       "/home/report/revenue",           1);
        insertSubMenu(menuIds[6], "抽獎統計",     "DRAW_STATISTICS",      "/home/report/lottery-result",    2);
        insertSubMenu(menuIds[6], "推薦獎金報表", "REFERRAL_REPORT",      "/home/report/referral",          3);
        insertSubMenu(menuIds[6], "儲值報表",     "RECHARGE_REPORT",      "/home/report/recharge",          4);
        insertSubMenu(menuIds[6], "紅利報表",     "BONUS_REPORT",         "/home/report/bonus",             5);
        insertSubMenu(menuIds[6], "會員成長報表", "MEMBER_GROWTH_REPORT", "/home/report/member-growth",     6);
        insertSubMenu(menuIds[6], "抽獎銷售報表", "LOTTERY_SALES_REPORT", "/home/report/lottery-sales",     7);
        insertSubMenu(menuIds[6], "店鋪績效報表", "STORE_PERF_REPORT",    "/home/report/store-performance", 8);
        insertSubMenu(menuIds[6], "獎品出貨報表", "PRIZE_SHIPMENT_REPORT","/home/report/prize-shipment",    9);

        // 建立子選單 - 系統設定 (index 7)
        insertSubMenu(menuIds[7], "參數設定",   "SYSTEM_CONFIG",  "/home/system-config",           1);
        insertSubMenu(menuIds[7], "系統日誌",   "SYSTEM_LOG",     "/home/system-log",              2);
        insertSubMenu(menuIds[7], "跑馬燈管理", "MARQUEE_MANAGE", "/home/marquee",                 3);
        insertSubMenu(menuIds[7], "系統公告",   "SYSTEM_NOTICE",  "/home/emergency-announcements", 4);

        // 建立子選單 - 權限管理 (index 8)（僅 Admin 可見）
        insertSubMenu(menuIds[8], "角色管理", "ROLE_MANAGEMENT", "/home/roles", 1);
        insertSubMenu(menuIds[8], "選單管理", "MENU_MANAGEMENT", "/home/menus", 2);

        log.info("選單初始化完成");
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

    private Menu ensureReportCenterMenu() {
        Menu reportCenter = findMenuByCodes(REPORT_PARENT_CODE, "report_management");
        if (reportCenter != null) {
            reportCenter.setName("報表中心");
            reportCenter.setCode(REPORT_PARENT_CODE);
            reportCenter.setPath("/home/report");
            reportCenter.setIcon("chart");
            reportCenter.setOrderNum(80);
            reportCenter.setIsVisible(true);
            reportCenter.setUpdatedAt(LocalDateTime.now());
            menuMapper.updateByPrimaryKeySelective(reportCenter);
            return reportCenter;
        }

        Menu menu = new Menu();
        menu.setId(UUID.randomUUID().toString());
        menu.setName("報表中心");
        menu.setCode(REPORT_PARENT_CODE);
        menu.setPath("/home/report");
        menu.setParentId(null);
        menu.setIcon("chart");
        menu.setOrderNum(80);
        menu.setIsVisible(true);
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.insert(menu);
        return menu;
    }

    private Menu ensureLotteryManagementMenu() {
        Menu lotteryManagement = findMenuByCodes(
                "LOTTERY_MANAGEMENT",
                "PRODUCT_MANAGEMENT",
                "PRODUCT_MANAGE",
                "LOTTERY_MANAGE",
                "lottery_management",
                "product_management");
        if (lotteryManagement == null) {
            lotteryManagement = findMenuByName("抽獎管理");
        }
        if (lotteryManagement == null) {
            lotteryManagement = findTopLevelMenuByPath("/admin/lotteries");
        }
        if (lotteryManagement == null) {
            lotteryManagement = findTopLevelMenuByPath("/home/lottery-with-prizes");
        }

        if (lotteryManagement != null) {
            lotteryManagement.setName("抽獎管理");
            lotteryManagement.setCode("LOTTERY_MANAGEMENT");
            lotteryManagement.setPath("/home/lottery-with-prizes");
            lotteryManagement.setIcon("shopping");
            lotteryManagement.setIsVisible(true);
            lotteryManagement.setUpdatedAt(LocalDateTime.now());
            menuMapper.updateByPrimaryKeySelective(lotteryManagement);
            return lotteryManagement;
        }

        Menu menu = new Menu();
        menu.setId(UUID.randomUUID().toString());
        menu.setName("抽獎管理");
        menu.setCode("LOTTERY_MANAGEMENT");
        menu.setPath("/home/lottery-with-prizes");
        menu.setParentId(null);
        menu.setIcon("shopping");
        menu.setOrderNum(20);
        menu.setIsVisible(true);
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.insert(menu);
        log.warn("未找到抽獎管理選單，正在新建 LOTTERY_MANAGEMENT");
        return menu;
    }

    private Menu ensureRootMenu(String name, String code, String path, String icon, int orderNum,
            String roleId, boolean canView, boolean canEdit, boolean canDelete) {
        Menu menu = findMenuByCode(code);
        if (menu == null) {
            menu = findMenuByName(name);
        }
        if (menu != null) {
            menu.setName(name);
            menu.setCode(code);
            menu.setPath(path);
            menu.setIcon(icon);
            menu.setOrderNum(orderNum);
            menu.setIsVisible(true);
            menu.setUpdatedAt(LocalDateTime.now());
            menuMapper.updateByPrimaryKeySelective(menu);
        } else {
            menu = new Menu();
            menu.setId(UUID.randomUUID().toString());
            menu.setName(name);
            menu.setCode(code);
            menu.setPath(path);
            menu.setParentId(null);
            menu.setIcon(icon);
            menu.setOrderNum(orderNum);
            menu.setIsVisible(true);
            menu.setCreatedAt(LocalDateTime.now());
            menu.setUpdatedAt(LocalDateTime.now());
            menuMapper.insert(menu);
        }
        if (roleId != null) {
            ensureRoleMenuPermission(roleId, menu.getId(), canView, canEdit, canDelete);
        }
        return menu;
    }

    private Menu upsertSubMenu(String parentId, String name, String code, String path, int orderNum, String... legacyCodes) {
        Menu menu = findMenuByCodes(code, legacyCodes);
        if (menu == null) {
            menu = findMenuByName(name);
        }
        if (menu == null) {
            insertSubMenu(parentId, name, code, path, orderNum);
            return findMenuByCodes(code);
        }

        menu.setParentId(parentId);
        menu.setName(name);
        menu.setCode(code);
        menu.setPath(path);
        menu.setOrderNum(orderNum);
        menu.setIsVisible(true);
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.updateByPrimaryKeySelective(menu);
        return menu;
    }

    private Menu findMenuByCodes(String primaryCode, String... extraCodes) {
        Menu menu = findMenuByCode(primaryCode);
        if (menu != null) {
            return menu;
        }
        for (String extraCode : extraCodes) {
            menu = findMenuByCode(extraCode);
            if (menu != null) {
                return menu;
            }
        }
        return null;
    }

    private Menu findMenuByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        MenuExample example = new MenuExample();
        example.createCriteria().andCodeEqualTo(code);
        return menuMapper.selectByExample(example).stream().findFirst().orElse(null);
    }

    private Menu findMenuByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        MenuExample example = new MenuExample();
        example.createCriteria().andNameEqualTo(name);
        return menuMapper.selectByExample(example).stream().findFirst().orElse(null);
    }

    private Menu findTopLevelMenuByPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        MenuExample example = new MenuExample();
        example.createCriteria().andPathEqualTo(path);
        return menuMapper.selectByExample(example).stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId().isBlank())
                .findFirst()
                .orElse(null);
    }

    private void deduplicateMenusByCode() {
        MenuExample example = new MenuExample();
        example.createCriteria().andCodeIsNotNull();
        example.setOrderByClause("code ASC, is_visible DESC, updated_at DESC, created_at DESC");

        java.util.Map<String, java.util.List<Menu>> menuGroups = new java.util.LinkedHashMap<>();
        for (Menu menu : menuMapper.selectByExample(example)) {
            if (menu.getCode() == null || menu.getCode().isBlank()) {
                continue;
            }
            menuGroups.computeIfAbsent(menu.getCode(), key -> new java.util.ArrayList<>()).add(menu);
        }

        for (java.util.Map.Entry<String, java.util.List<Menu>> entry : menuGroups.entrySet()) {
            java.util.List<Menu> menus = entry.getValue();
            if (menus.size() <= 1) {
                continue;
            }

            Menu canonical = menus.get(0);
            for (int i = 1; i < menus.size(); i++) {
                mergeMenuIntoCanonical(canonical, menus.get(i));
            }
            log.warn("偵測到重複選單，保留最新版本並合併：code={}, count={}", entry.getKey(), menus.size());
        }
    }

    private void mergeMenuIntoCanonical(Menu canonical, Menu duplicate) {
        if (canonical == null || duplicate == null || canonical.getId().equals(duplicate.getId())) {
            return;
        }

        rebindChildMenus(duplicate.getId(), canonical.getId());
        mergeRoleMenuPermissions(canonical.getId(), duplicate.getId());
        menuMapper.deleteByPrimaryKey(duplicate.getId());
    }

    private void rebindChildMenus(String sourceParentId, String targetParentId) {
        MenuExample example = new MenuExample();
        example.createCriteria().andParentIdEqualTo(sourceParentId);
        for (Menu child : menuMapper.selectByExample(example)) {
            child.setParentId(targetParentId);
            child.setUpdatedAt(LocalDateTime.now());
            menuMapper.updateByPrimaryKeySelective(child);
        }
    }

    private void mergeRoleMenuPermissions(String targetMenuId, String sourceMenuId) {
        RoleMenuExample example = new RoleMenuExample();
        example.createCriteria().andMenuIdEqualTo(sourceMenuId);
        for (RoleMenu sourceRoleMenu : roleMenuMapper.selectByExample(example)) {
            RoleMenuExample targetExample = new RoleMenuExample();
            targetExample.createCriteria()
                    .andRoleIdEqualTo(sourceRoleMenu.getRoleId())
                    .andMenuIdEqualTo(targetMenuId);
            RoleMenu targetRoleMenu = roleMenuMapper.selectByExample(targetExample).stream().findFirst().orElse(null);

            if (targetRoleMenu == null) {
                sourceRoleMenu.setMenuId(targetMenuId);
                roleMenuMapper.updateByPrimaryKeySelective(sourceRoleMenu);
                continue;
            }

            targetRoleMenu.setCanView(Boolean.TRUE.equals(targetRoleMenu.getCanView()) || Boolean.TRUE.equals(sourceRoleMenu.getCanView()));
            targetRoleMenu.setCanEdit(Boolean.TRUE.equals(targetRoleMenu.getCanEdit()) || Boolean.TRUE.equals(sourceRoleMenu.getCanEdit()));
            targetRoleMenu.setCanDelete(Boolean.TRUE.equals(targetRoleMenu.getCanDelete()) || Boolean.TRUE.equals(sourceRoleMenu.getCanDelete()));
            roleMenuMapper.updateByPrimaryKeySelective(targetRoleMenu);
            roleMenuMapper.deleteByPrimaryKey(sourceRoleMenu.getId());
        }
    }

    private void ensureRoleMenuPermission(String roleId, String menuId, boolean canView, boolean canEdit, boolean canDelete) {
        if (roleId == null || menuId == null) {
            return;
        }

        RoleMenuExample example = new RoleMenuExample();
        example.createCriteria().andRoleIdEqualTo(roleId).andMenuIdEqualTo(menuId);
        RoleMenu roleMenu = roleMenuMapper.selectByExample(example).stream().findFirst().orElse(null);

        if (roleMenu == null) {
            roleMenu = new RoleMenu();
            roleMenu.setId(UUID.randomUUID().toString());
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setCreatedAt(LocalDateTime.now());
            roleMenu.setCanView(canView);
            roleMenu.setCanEdit(canEdit);
            roleMenu.setCanDelete(canDelete);
            roleMenuMapper.insert(roleMenu);
            return;
        }

        roleMenu.setCanView(canView);
        roleMenu.setCanEdit(canEdit);
        roleMenu.setCanDelete(canDelete);
        roleMenuMapper.updateByPrimaryKeySelective(roleMenu);
    }

    private void removeRoleMenuPermission(String roleId, String menuId) {
        if (roleId == null || menuId == null) {
            return;
        }

        RoleMenuExample example = new RoleMenuExample();
        example.createCriteria().andRoleIdEqualTo(roleId).andMenuIdEqualTo(menuId);
        roleMenuMapper.deleteByExample(example);
    }

    /**
     * 初始化角色選單權限
     */
    private void initializeRoleMenuPermissions() {
        log.info("初始化角色選單權限...");
        
        // Admin 角色取得所有選單權限
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

        // StoreOwner 角色權限設定
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "LOTTERY_MANAGEMENT", true, true, true);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "LOTTERY_LIST", true, true, true);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "LOTTERY_CREATE", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "PRIZE_MANAGEMENT", true, true, true);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "ORDER_MANAGEMENT", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "ORDER_LIST", true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "SHIPPING_MANAGEMENT", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "REPORT_CENTER",        true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "REVENUE_REPORT",       true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "DRAW_STATISTICS",      true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "REFERRAL_REPORT",      true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "RECHARGE_REPORT",      true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "BONUS_REPORT",         true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "LOTTERY_SALES_REPORT", true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "STORE_PERF_REPORT",    true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_OWNER_ID, "PRIZE_SHIPMENT_REPORT",true, false, false);

        // StoreEditor 角色權限設定
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "LOTTERY_MANAGEMENT", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "LOTTERY_LIST", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "PRIZE_MANAGEMENT", true, true, false);
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "ORDER_MANAGEMENT", true, false, false);
        assignMenuPermissionByCode(ROLE_STORE_EDITOR_ID, "ORDER_LIST", true, false, false);

        log.info("角色選單權限初始化完成");
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
     * 補救報表選單缺失，執行增量補救並重新設定角色選單權限
     * 直接從 DB 操作，確保冪等性
     */
    private void rescueMissingReportMenus() {
        if (ROLE_ADMIN_ID == null || ROLE_STORE_OWNER_ID == null || ROLE_STORE_EDITOR_ID == null) {
            loadRoleIdsFromDb();
        }
        Menu reportCenter = ensureRootMenu(
                "報表中心",
                "REPORT_CENTER",
                "/home/report",
                "TrendCharts",
                40,
                ROLE_ADMIN_ID,
                true,
                true,
                true);
        if (reportCenter == null) {
            log.warn("無法建立或取得報表中心選單，略過子選單補救");
            return;
        }
        String[][] reportMenus = {
                {"營收報表", "REVENUE_REPORT", "/home/report/revenue", "1"},
                {"抽獎統計", "DRAW_STATISTICS", "/home/report/lottery-result", "2"},
                {"推薦獎金報表", "REFERRAL_REPORT", "/home/report/referral", "3"},
                {"儲值報表", "RECHARGE_REPORT", "/home/report/recharge", "4"},
                {"紅利報表", "BONUS_REPORT", "/home/report/bonus", "5"},
                {"會員成長報表", "MEMBER_GROWTH_REPORT", "/home/report/member-growth", "6"},
                {"一番賞銷售報表", "LOTTERY_SALES_REPORT", "/home/report/lottery-sales", "7"},
                {"店鋪績效報表", "STORE_PERF_REPORT", "/home/report/store-performance", "8"},
                {"獎品出貨報表", "PRIZE_SHIPMENT_REPORT", "/home/report/prize-shipment", "9"}
        };
        boolean anyAdded = false;
        for (String[] menuDef : reportMenus) {
            Menu menu = upsertSubMenu(
                    reportCenter.getId(),
                    menuDef[0],
                    menuDef[1],
                    menuDef[2],
                    Integer.parseInt(menuDef[3]));
            ensureRoleMenuPermission(ROLE_ADMIN_ID, menu.getId(), true, true, true);
            boolean ownerCanView = !"MEMBER_GROWTH_REPORT".equals(menuDef[1]);
            if (ownerCanView) {
                ensureRoleMenuPermission(ROLE_STORE_OWNER_ID, menu.getId(), true, false, false);
            } else {
                removeRoleMenuPermission(ROLE_STORE_OWNER_ID, menu.getId());
            }
            removeRoleMenuPermission(ROLE_STORE_EDITOR_ID, menu.getId());
            anyAdded = true;
        }
        hideLegacyReportMenus(reportCenter.getId());
        if (anyAdded) {
            log.info("報表中心子選單補救完成");
        }
    }
    /**
     * 隱藏舊版報表路徑下的重複選單，並依 path 去重。
     */
    private void hideLegacyReportMenus(String reportCenterId) {
        MenuExample legacyEx = new MenuExample();
        legacyEx.createCriteria().andPathLike("/admin/reports%");
        for (Menu legacyMenu : menuMapper.selectByExample(legacyEx)) {
            legacyMenu.setIsVisible(false);
            legacyMenu.setUpdatedAt(LocalDateTime.now());
            menuMapper.updateByPrimaryKeySelective(legacyMenu);
        }
        deduplicateChildMenusByPath(reportCenterId);
    }

    /**
     * 補救系統設定選單缺失，補建缺少的選單。
     */
    private void rescueMissingSystemMenus() {
        if (ROLE_ADMIN_ID == null) {
            loadRoleIdsFromDb();
        }
        Menu systemSetting = ensureRootMenu(
                "系統設定",
                "SYSTEM_SETTING",
                "/home/system-config",
                "setting",
                80,
                ROLE_ADMIN_ID,
                true,
                true,
                true);
        if (systemSetting == null) {
            return;
        }
        String[][] systemMenus = {
                {"參數設定",   "SYSTEM_CONFIG",  "/home/system-config",           "1"},
                {"系統日誌",   "SYSTEM_LOG",     "/home/system-log",              "2"},
                {"跑馬燈管理", "MARQUEE_MANAGE", "/home/marquee",                 "3"},
                {"系統公告",   "SYSTEM_NOTICE",  "/home/emergency-announcements", "4"}
        };
        boolean anyAdded = false;
        for (String[] menuDef : systemMenus) {
            Menu menu = upsertSubMenu(
                    systemSetting.getId(),
                    menuDef[0],
                    menuDef[1],
                    menuDef[2],
                    Integer.parseInt(menuDef[3]));
            ensureRoleMenuPermission(ROLE_ADMIN_ID, menu.getId(), true, true, true);
            anyAdded = true;
        }
        if (anyAdded) {
            log.info("系統設定子選單補救完成");
        }
    }

    private void rescueMissingOperationMenus() {
        if (ROLE_ADMIN_ID == null) {
            loadRoleIdsFromDb();
        }

        Menu operationTools = ensureRootMenu(
                "營運工具",
                "OPERATION_TOOLS",
                "/home/recharge-plan",
                "build",
                50,
                ROLE_ADMIN_ID,
                true,
                true,
                true);
        if (operationTools == null) {
            return;
        }

        String[][] operationMenus = {
                {"儲值方案",   "RECHARGE_MANAGE",      "/home/recharge-plan",  "1"},
                {"推薦碼管理", "REFERRAL_CODE_MANAGE", "/home/referral-codes", "2"}
        };
        boolean anyAdded = false;
        for (String[] menuDef : operationMenus) {
            Menu menu = upsertSubMenu(
                    operationTools.getId(),
                    menuDef[0],
                    menuDef[1],
                    menuDef[2],
                    Integer.parseInt(menuDef[3]));
            ensureRoleMenuPermission(ROLE_ADMIN_ID, menu.getId(), true, true, true);
            anyAdded = true;
        }

        if (anyAdded) {
            log.info("營運工具子選單補救完成");
        }
    }

    private void rescueMissingWebsiteContentMenus() {
        if (ROLE_ADMIN_ID == null) {
            loadRoleIdsFromDb();
        }

        Menu websiteManagement = ensureRootMenu(
                "網站內容管理",
                "WEBSITE_CONTENT_MANAGEMENT",
                "/home/banner",
                "public",
                60,
                ROLE_ADMIN_ID,
                true,
                true,
                true);
        if (websiteManagement == null) {
            return;
        }

        String[][] websiteMenus = {
                {"Banner 管理", "BANNER_MANAGE", "/home/banner", "1"},
                {"最新消息",    "NEWS_MANAGE",   "/home/news",   "2"}
        };
        boolean anyAdded = false;
        for (String[] menuDef : websiteMenus) {
            Menu menu = upsertSubMenu(
                    websiteManagement.getId(),
                    menuDef[0],
                    menuDef[1],
                    menuDef[2],
                    Integer.parseInt(menuDef[3]));
            ensureRoleMenuPermission(ROLE_ADMIN_ID, menu.getId(), true, true, true);
            anyAdded = true;
        }

        if (anyAdded) {
            log.info("網站內容管理子選單補救完成");
        }
    }

    private void ensureDefaultLotteryTags() {
        for (int i = 0; i < DEFAULT_LOTTERY_TAGS.size(); i++) {
            upsertLotteryTag(DEFAULT_LOTTERY_TAGS.get(i), i + 1);
        }
        log.info("抽獎標籤預設資料已同步: {}", DEFAULT_LOTTERY_TAGS);
    }

    private void upsertLotteryTag(String name, int displayOrder) {
        LotteryTag tag = lotteryTagMapper.selectByNameIgnoreCase(name);
        LocalDateTime now = LocalDateTime.now();
        if (tag == null) {
            tag = new LotteryTag();
            tag.setId(UUID.randomUUID().toString());
            tag.setName(name);
            tag.setStatus("ACTIVE");
            tag.setDisplayOrder(displayOrder);
            tag.setCreatedAt(now);
            tag.setUpdatedAt(now);
            lotteryTagMapper.insert(tag);
            return;
        }

        tag.setName(name);
        tag.setStatus("ACTIVE");
        tag.setDisplayOrder(displayOrder);
        tag.setUpdatedAt(now);
        lotteryTagMapper.updateByPrimaryKeySelective(tag);
    }

    private void deduplicateChildMenusByPath(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return;
        }

        MenuExample example = new MenuExample();
        example.createCriteria().andParentIdEqualTo(parentId);
        example.setOrderByClause("path ASC, is_visible DESC, updated_at DESC, created_at DESC");

        java.util.Map<String, java.util.List<Menu>> menuGroups = new java.util.LinkedHashMap<>();
        for (Menu menu : menuMapper.selectByExample(example)) {
            if (menu.getPath() == null || menu.getPath().isBlank()) {
                continue;
            }
            menuGroups.computeIfAbsent(menu.getPath(), key -> new java.util.ArrayList<>()).add(menu);
        }

        for (java.util.Map.Entry<String, java.util.List<Menu>> entry : menuGroups.entrySet()) {
            java.util.List<Menu> menus = entry.getValue();
            if (menus.size() <= 1) {
                continue;
            }

            Menu canonical = menus.get(0);
            for (int i = 1; i < menus.size(); i++) {
                mergeMenuIntoCanonical(canonical, menus.get(i));
            }
            log.warn("偵測到重複子選單，保留最新版本並合併（依路徑）: parentId={}, path={}, count={}", parentId, entry.getKey(), menus.size());
        }
    }

    /**
     * 補救會員管理選單：
     * - 修正 USER_MANAGEMENT 路徑為 /home/member/list
     * - 補建 MEMBER_LIST（前台使用者）和 ADMIN_USER_LIST（後台使用者）子選單
     * - 將 ACCOUNT_MANAGEMENT 從 PERMISSION_MANAGEMENT 下隱藏（已移至此處）
     */
    private void rescueMissingMemberMenus() {
        if (ROLE_ADMIN_ID == null || ROLE_STORE_OWNER_ID == null || ROLE_STORE_EDITOR_ID == null) {
            loadRoleIdsFromDb();
        }

        // 確保 USER_MANAGEMENT 頂層選單路徑正確
        Menu userManagement = ensureRootMenu(
                "會員管理",
                "USER_MANAGEMENT",
                "/home/member/list",
                "people",
                40,
                ROLE_ADMIN_ID,
                true,
                true,
                true);
        if (userManagement == null) {
            log.warn("無法建立或取得會員管理選單，略過子選單補救");
            return;
        }

        // 補建子選單
        Menu memberList = upsertSubMenu(userManagement.getId(), "前台使用者", "MEMBER_LIST", "/home/member/list", 1);
        ensureRoleMenuPermission(ROLE_ADMIN_ID, memberList.getId(), true, true, true);

        Menu adminUserList = upsertSubMenu(userManagement.getId(), "後台使用者", "ADMIN_USER_LIST", "/home/admin-users", 2);
        ensureRoleMenuPermission(ROLE_ADMIN_ID, adminUserList.getId(), true, true, true);

        // 隱藏舊的「會員列表」選單（已由「前台使用者」取代，名稱不同所以 upsert 無法自動合併）
        MenuExample memberListEx = new MenuExample();
        memberListEx.createCriteria()
                .andParentIdEqualTo(userManagement.getId())
                .andNameEqualTo("會員列表");
        for (Menu old : menuMapper.selectByExample(memberListEx)) {
            if (Boolean.TRUE.equals(old.getIsVisible())) {
                old.setIsVisible(false);
                old.setUpdatedAt(LocalDateTime.now());
                menuMapper.updateByPrimaryKeySelective(old);
                log.info("已隱藏舊會員列表選單：id={}", old.getId());
            }
        }

        // 隱藏舊的 ACCOUNT_MANAGEMENT（帳號管理）選單，避免在 PERMISSION_MANAGEMENT 下重複出現
        MenuExample accExample = new MenuExample();
        accExample.createCriteria().andCodeEqualTo("ACCOUNT_MANAGEMENT");
        for (Menu acc : menuMapper.selectByExample(accExample)) {
            if (Boolean.TRUE.equals(acc.getIsVisible())) {
                acc.setIsVisible(false);
                acc.setUpdatedAt(LocalDateTime.now());
                menuMapper.updateByPrimaryKeySelective(acc);
                log.info("已隱藏舊帳號管理選單：id={}", acc.getId());
            }
        }

        log.info("會員管理選單補救完成");
    }

    /**
     * 補救缺失的分類管理選單，若不存在則自動建立並設定角色權限
     */
    private void rescueMissingCategoryMenu() {
        if (ROLE_ADMIN_ID == null || ROLE_STORE_OWNER_ID == null || ROLE_STORE_EDITOR_ID == null) {
            loadRoleIdsFromDb();
        }

        Menu lotteryManagement = ensureLotteryManagementMenu();
        ensureRoleMenuPermission(ROLE_ADMIN_ID, lotteryManagement.getId(), true, true, true);
        ensureRoleMenuPermission(ROLE_STORE_OWNER_ID, lotteryManagement.getId(), true, true, true);
        ensureRoleMenuPermission(ROLE_STORE_EDITOR_ID, lotteryManagement.getId(), true, true, false);

        Menu categoryMenu = upsertSubMenu(
                lotteryManagement.getId(),
                "分類管理",
                "CATEGORY_MANAGEMENT",
                "/home/category",
                4);

        ensureRoleMenuPermission(ROLE_ADMIN_ID, categoryMenu.getId(), true, true, true);
        ensureRoleMenuPermission(ROLE_STORE_OWNER_ID, categoryMenu.getId(), true, true, false);
        ensureRoleMenuPermission(ROLE_STORE_EDITOR_ID, categoryMenu.getId(), true, true, false);

        log.info("分類管理選單補救完成");
    }

    /**
     * 初始化管理帳號
     */
    private void initializeAdminUsers() {
        log.info("初始化管理帳號...");
        
        ADMIN_USER_ID = UUID.randomUUID().toString();
        STORE_OWNER_1_ID = UUID.randomUUID().toString();
        STORE_OWNER_2_ID = UUID.randomUUID().toString();
        STORE_EDITOR_1_ID = UUID.randomUUID().toString();

        // 系統管理員帳號（密碼: admin123）
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

        // 測試店家負責人1（密碼: Test1234）
        AdminUser owner1 = new AdminUser();
        owner1.setId(STORE_OWNER_1_ID);
        owner1.setUsername("owner@teststore.com");
        owner1.setPassword(passwordEncoder.encode("Test1234"));
        owner1.setEmail("owner@teststore.com");
        owner1.setDisplayName("測試店家負責人");
        owner1.setPhone("0911111111");
        owner1.setStatus("ACTIVE");
        owner1.setForceChangePassword(false);
        owner1.setCreatedBy(ADMIN_USER_ID);
        owner1.setRemark("測試用帳號");
        owner1.setCreatedAt(LocalDateTime.now());
        owner1.setUpdatedAt(LocalDateTime.now());
        owner1.setFailedLoginAttempts(0);
        adminUserMapper.insert(owner1);
        assignRole(STORE_OWNER_1_ID, ROLE_STORE_OWNER_ID);

        // 測試店家負責人2（密碼: Test1234）
        AdminUser owner2 = new AdminUser();
        owner2.setId(STORE_OWNER_2_ID);
        owner2.setUsername("owner2@teststore.com");
        owner2.setPassword(passwordEncoder.encode("Test1234"));
        owner2.setEmail("owner2@teststore.com");
        owner2.setDisplayName("動漫商城負責人");
        owner2.setPhone("0922222222");
        owner2.setStatus("ACTIVE");
        owner2.setForceChangePassword(false);
        owner2.setCreatedBy(ADMIN_USER_ID);
        owner2.setRemark("測試用帳號");
        owner2.setCreatedAt(LocalDateTime.now());
        owner2.setUpdatedAt(LocalDateTime.now());
        owner2.setFailedLoginAttempts(0);
        adminUserMapper.insert(owner2);
        assignRole(STORE_OWNER_2_ID, ROLE_STORE_OWNER_ID);

        // 測試店家編輯員（密碼: Test1234）
        AdminUser editor = new AdminUser();
        editor.setId(STORE_EDITOR_1_ID);
        editor.setUsername("editor@teststore.com");
        editor.setPassword(passwordEncoder.encode("Test1234"));
        editor.setEmail("editor@teststore.com");
        editor.setDisplayName("測試店家編輯員");
        editor.setPhone("0933333333");
        editor.setStatus("ACTIVE");
        editor.setForceChangePassword(false);
        editor.setCreatedBy(ADMIN_USER_ID);
        editor.setRemark("測試用編輯帳號");
        editor.setCreatedAt(LocalDateTime.now());
        editor.setUpdatedAt(LocalDateTime.now());
        editor.setFailedLoginAttempts(0);
        adminUserMapper.insert(editor);
        assignRole(STORE_EDITOR_1_ID, ROLE_STORE_EDITOR_ID);

        log.info("管理帳號初始化完成，共 4 筆");
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
     * 初始化測試商店
     */
    private void initializeStores() {
        log.info("初始化測試商店...");
        
        STORE_1_ID = UUID.randomUUID().toString();
        STORE_2_ID = UUID.randomUUID().toString();

        // KUJI 測試商店
        Store store1 = new Store();
        store1.setId(STORE_1_ID);
        store1.setOwnerId(STORE_OWNER_1_ID);
        store1.setStoreName("KUJI 測試商店");
        store1.setShortDescription("一番賞測試商店");
        store1.setLongDescription("提供精美一番賞商品的測試商店，商品均為限量發售，請把握機會");
        store1.setLogoUrl("https://via.placeholder.com/200");
        store1.setCoverImageUrl("https://via.placeholder.com/1200x400");
        store1.setEmail("owner@teststore.com");
        store1.setPhone("0911111111");
        store1.setAddress("台北市信義區信義路五段100號");
        store1.setFacebookUrl("https://facebook.com/kujitest");
        store1.setInstagramUrl("https://instagram.com/kujitest");
        store1.setLineId("@kujitest");
        store1.setBusinessHours("每日 10:00~22:00");
        store1.setStatus("ACTIVE");
        store1.setRemark("測試用商店");
        store1.setCreatedAt(LocalDateTime.now());
        store1.setUpdatedAt(LocalDateTime.now());
        store1.setUpdatedBy(ADMIN_USER_ID);
        storeMapper.insert(store1);
        
        // 建立店家用戶關聯
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

        // 動漫精品商城
        Store store2 = new Store();
        store2.setId(STORE_2_ID);
        store2.setOwnerId(STORE_OWNER_2_ID);
        store2.setStoreName("動漫精品商城");
        store2.setShortDescription("動漫周邊精品商城");
        store2.setLongDescription("專業動漫周邊精品商城，精選海內外動漫商品，商品定期更新");
        store2.setLogoUrl("https://via.placeholder.com/200");
        store2.setCoverImageUrl("https://via.placeholder.com/1200x400");
        store2.setEmail("owner2@teststore.com");
        store2.setPhone("0922222222");
        store2.setAddress("台北市大安區忠孝東路四段168號");
        store2.setFacebookUrl("https://facebook.com/animestore");
        store2.setInstagramUrl("https://instagram.com/animestore");
        store2.setLineId("@animestore");
        store2.setBusinessHours("每日 11:00~21:00");
        store2.setStatus("ACTIVE");
        store2.setRemark("測試用商店");
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

        log.info("測試商店初始化完成，共 2 筆");
    }

    /**
     * 初始化測試用戶
     */
    private void initializeTestUsers() {
        log.info("初始化測試用戶...");
        
        // 測試用戶 A（密碼: Test1234）
        User user1 = new User();
        user1.setId(UUID.randomUUID().toString());
        user1.setEmail("user1@test.com");
        user1.setPassword(passwordEncoder.encode("Test1234"));
        user1.setNickname("測試用戶A");
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

        // 測試用戶 B（密碼: Test1234）
        User user2 = new User();
        user2.setId(UUID.randomUUID().toString());
        user2.setEmail("user2@test.com");
        user2.setPassword(passwordEncoder.encode("Test1234"));
        user2.setNickname("測試用戶B");
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

        // Google 測試用戶
        User user3 = new User();
        user3.setId(UUID.randomUUID().toString());
        user3.setEmail("googleuser@gmail.com");
        user3.setPassword(null);
        user3.setNickname("Google 測試用戶");
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

        log.info("測試用戶初始化完成，共 3 筆");
    }

    /**
     * 初始化測試商品
     */
    private void initializeLotteries() {
        log.info("初始化測試商品...");
        
        LOTTERY_1_ID = UUID.randomUUID().toString();
        LOTTERY_2_ID = UUID.randomUUID().toString();

        // 鬼滅之刃精品一番賞
        Lottery lottery1 = new Lottery();
        lottery1.setId(LOTTERY_1_ID);
        lottery1.setStoreId(STORE_1_ID);
        lottery1.setTitle("鬼滅之刃精品一番賞");
        lottery1.setDescription("超人氣鬼滅之刃精品一番賞，限量發售精美人偶");
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
        insertPrize(LOTTERY_1_ID, "A", "竈門炭治郎人偶", "限量25cm 精雕版人偶", 1, 1, "FIGURE", 1, 0, 0);
        insertPrize(LOTTERY_1_ID, "B", "我妻善逸人偶", "限量25cm 精雕版人偶", 1, 1, "FIGURE", 2, 0, 0);
        insertPrize(LOTTERY_1_ID, "C", "小芥子立像", "限量18cm 彩繪人偶", 3, 3, "FIGURE", 3, 0, 0);
        insertPrize(LOTTERY_1_ID, "D", "嘴平伊之助立像", "限量18cm 彩繪人偶", 5, 4, "FIGURE", 4, 0, 0);
        insertPrize(LOTTERY_1_ID, "E", "精品鑰匙圈", "精品版鑰匙圈", 20, 18, "GOODS", 5, 0, 0);
        insertPrize(LOTTERY_1_ID, "F", "限定徽章組", "精品 3 件徽章組", 30, 25, "GOODS", 6, 0, 0);
        insertPrize(LOTTERY_1_ID, "G", "精品抱枕", "精品抱枕套", 19, 12, "GOODS", 7, 0, 0);
        insertPrize(LOTTERY_1_ID, "LAST_PRIZE", "特製典藏人偶", "超稀有典藏人偶組", 1, 1, "FIGURE", 8, 1, 0);

        // 進擊的巨人紀念賞
        Lottery lottery2 = new Lottery();
        lottery2.setId(LOTTERY_2_ID);
        lottery2.setStoreId(STORE_1_ID);
        lottery2.setTitle("進擊的巨人紀念賞");
        lottery2.setDescription("進擊的巨人限定紀念賞，每日精選商品");
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

        // 進擊的巨人獎品
        insertPrize(LOTTERY_2_ID, "A", "調查兵團人偶", "限量20cm 人偶", 2, 2, "FIGURE", 1, 0, 1);
        insertPrize(LOTTERY_2_ID, "B", "艾倫鐵質人偶", "限量18cm 人偶", 5, 5, "FIGURE", 2, 0, 0);
        insertPrize(LOTTERY_2_ID, "C", "精品收藏組", "精品版收藏組", 20, 20, "GOODS", 3, 0, 0);
        insertPrize(LOTTERY_2_ID, "D", "特製手錶", "精品版手錶", 33, 33, "GOODS", 4, 0, 0);
        insertPrize(LOTTERY_2_ID, "E", "貼紙組", "精品版貼紙組", 40, 40, "GOODS", 5, 0, 0);

        log.info("測試商品初始化完成，共 2 件商品、13 個獎品");
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
     * 印出預設帳號資訊
     */
    private void logDefaultCredentials() {
        log.info("");
        log.info("========================================");
        log.info("預設測試帳號資訊");
        log.info("========================================");
        log.info("後台管理員：");
        log.info("  1. Admin: admin@kuji.com / admin123");
        log.info("  2. StoreOwner1: owner@teststore.com / Test1234");
        log.info("  3. StoreOwner2: owner2@teststore.com / Test1234");
        log.info("  4. StoreEditor: editor@teststore.com / Test1234");
        log.info("");
        log.info("前台測試會員：");
        log.info("  1. user1@test.com / Test1234 (金幣 1000, 紅利 500)");
        log.info("  2. user2@test.com / Test1234 (金幣 2500, 紅利 300)");
        log.info("  3. googleuser@gmail.com (Google 登入, 金幣 500, 紅利 100)");
        log.info("");
        log.info("測試商店：");
        log.info("  1. KUJI 測試商店 (Owner: owner@teststore.com)");
        log.info("  2. 動漫精品商城 (Owner: owner2@teststore.com)");
        log.info("");
        log.info("測試商品：");
        log.info("  1. 鬼滅之刃精品一番賞 (80 金幣一抽)");
        log.info("  2. 進擊的巨人紀念賞 (60 金幣一抽)");
        log.info("========================================");
    }
}

