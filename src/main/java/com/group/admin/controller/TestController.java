package com.group.admin.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group.admin.entity.AdminUser;
import com.group.admin.entity.Lottery;
import com.group.admin.entity.Menu;
import com.group.admin.entity.Role;
import com.group.admin.entity.Store;
import com.group.admin.entity.User;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.LotteryExample;
import com.group.admin.example.MenuExample;
import com.group.admin.example.RoleExample;
import com.group.admin.example.StoreExample;
import com.group.admin.example.UserExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.MenuMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.auth.AdminLoginReq;
import com.group.admin.service.AdminAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API 測試控制器
 * 
 * 提供各種測試端點以驗證系統功能
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Tag(name = "API測試", description = "系統功能測試端點")
public class TestController {

    private final AdminAuthService adminAuthService;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final AdminUserMapper adminUserMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;
    private final LotteryMapper lotteryMapper;

    /**
     * 健康檢查
     */
    @GetMapping("/health")
    @Operation(summary = "健康檢查", description = "檢查系統是否正常運行")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "KUJI Admin System is running!");
        return ResponseEntity.ok(result);
    }

    /**
     * 測試資料庫連接
     */
    @GetMapping("/db-check")
    @Operation(summary = "資料庫連接測試", description = "檢查資料庫是否正常連接並返回統計資料")
    public ResponseEntity<Map<String, Object>> dbCheck() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查詢各表資料數量
            RoleExample roleExample = new RoleExample();
            long roleCount = roleMapper.countByExample(roleExample);
            
            MenuExample menuExample = new MenuExample();
            long menuCount = menuMapper.countByExample(menuExample);
            
            AdminUserExample adminUserExample = new AdminUserExample();
            long adminUserCount = adminUserMapper.countByExample(adminUserExample);
            
            StoreExample storeExample = new StoreExample();
            long storeCount = storeMapper.countByExample(storeExample);
            
            UserExample userExample = new UserExample();
            long userCount = userMapper.countByExample(userExample);
            
            LotteryExample lotteryExample = new LotteryExample();
            long lotteryCount = lotteryMapper.countByExample(lotteryExample);
            
            result.put("status", "SUCCESS");
            result.put("database", "Connected");
            result.put("statistics", Map.of(
                "roles", roleCount,
                "menus", menuCount,
                "adminUsers", adminUserCount,
                "stores", storeCount,
                "users", userCount,
                "lotteries", lotteryCount
            ));
            
            log.info("資料庫檢查成功: 角色={}, 選單={}, 管理員={}, 店家={}, 會員={}, 商品={}", 
                roleCount, menuCount, adminUserCount, storeCount, userCount, lotteryCount);
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            log.error("資料庫檢查失敗", e);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 查詢所有角色
     */
    @GetMapping("/roles")
    @Operation(summary = "查詢所有角色", description = "返回系統中所有角色列表")
    public ResponseEntity<List<Role>> getAllRoles() {
        RoleExample example = new RoleExample();
        List<Role> roles = roleMapper.selectByExample(example);
        log.info("查詢角色列表，共 {} 筆", roles.size());
        return ResponseEntity.ok(roles);
    }

    /**
     * 查詢所有選單
     */
    @GetMapping("/menus")
    @Operation(summary = "查詢所有選單", description = "返回系統中所有選單列表")
    public ResponseEntity<List<Menu>> getAllMenus() {
        MenuExample example = new MenuExample();
        List<Menu> menus = menuMapper.selectByExample(example);
        log.info("查詢選單列表，共 {} 筆", menus.size());
        return ResponseEntity.ok(menus);
    }

    /**
     * 查詢所有管理員
     */
    @GetMapping("/admin-users")
    @Operation(summary = "查詢所有管理員", description = "返回系統中所有管理員列表")
    public ResponseEntity<List<AdminUser>> getAllAdminUsers() {
        AdminUserExample example = new AdminUserExample();
        List<AdminUser> adminUsers = adminUserMapper.selectByExample(example);
        // 移除密碼欄位
        adminUsers.forEach(user -> user.setPassword("******"));
        log.info("查詢管理員列表，共 {} 筆", adminUsers.size());
        return ResponseEntity.ok(adminUsers);
    }

    /**
     * 查詢所有店家
     */
    @GetMapping("/stores")
    @Operation(summary = "查詢所有店家", description = "返回系統中所有店家列表")
    public ResponseEntity<List<Store>> getAllStores() {
        StoreExample example = new StoreExample();
        List<Store> stores = storeMapper.selectByExample(example);
        log.info("查詢店家列表，共 {} 筆", stores.size());
        return ResponseEntity.ok(stores);
    }

    /**
     * 查詢所有會員
     */
    @GetMapping("/users")
    @Operation(summary = "查詢所有會員", description = "返回系統中所有會員列表")
    public ResponseEntity<List<User>> getAllUsers() {
        UserExample example = new UserExample();
        List<User> users = userMapper.selectByExample(example);
        // 移除密碼欄位
        users.forEach(user -> user.setPassword("******"));
        log.info("查詢會員列表，共 {} 筆", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * 查詢所有商品
     */
    @GetMapping("/lotteries")
    @Operation(summary = "查詢所有商品", description = "返回系統中所有抽獎商品列表")
    public ResponseEntity<List<Lottery>> getAllLotteries() {
        LotteryExample example = new LotteryExample();
        List<Lottery> lotteries = lotteryMapper.selectByExampleWithBLOBs(example);
        log.info("查詢商品列表，共 {} 筆", lotteries.size());
        return ResponseEntity.ok(lotteries);
    }

    /**
     * 測試管理員登入
     */
    @PostMapping("/admin-login")
    @Operation(summary = "測試管理員登入", description = "使用預設帳號測試登入功能")
    public ResponseEntity<Map<String, Object>> testAdminLogin(@RequestBody AdminLoginReq req) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var authRes = adminAuthService.login(req);
            result.put("status", "SUCCESS");
            result.put("message", "登入成功");
            result.put("data", authRes);
            log.info("管理員登入測試成功: {}", req.getUsername());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "登入失敗: " + e.getMessage());
            log.error("管理員登入測試失敗", e);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 取得預設測試帳號資訊
     */
    @GetMapping("/default-accounts")
    @Operation(summary = "預設測試帳號", description = "返回系統預設的測試帳號資訊")
    public ResponseEntity<Map<String, Object>> getDefaultAccounts() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("adminAccounts", List.of(
            Map.of("username", "admin@kuji.com", "password", "admin123", "role", "系統管理員"),
            Map.of("username", "owner@teststore.com", "password", "Test1234", "role", "店家負責人"),
            Map.of("username", "owner2@teststore.com", "password", "Test1234", "role", "店家負責人"),
            Map.of("username", "editor@teststore.com", "password", "Test1234", "role", "店家編輯")
        ));
        
        result.put("userAccounts", List.of(
            Map.of("email", "user1@test.com", "password", "Test1234", "goldCoins", 1000, "bonusCoins", 500),
            Map.of("email", "user2@test.com", "password", "Test1234", "goldCoins", 2500, "bonusCoins", 300),
            Map.of("email", "googleuser@gmail.com", "password", "N/A (Google OAuth)", "goldCoins", 500, "bonusCoins", 100)
        ));
        
        result.put("stores", List.of(
            Map.of("name", "KUJI 測試商店", "owner", "owner@teststore.com"),
            Map.of("name", "動漫周邊專賣店", "owner", "owner2@teststore.com")
        ));
        
        result.put("lotteries", List.of(
            Map.of("title", "鬼滅之刃一番賞", "pricePerDraw", 80, "maxDraws", 80, "status", "ON_SHELF"),
            Map.of("title", "咒術迴戰刮刮樂", "pricePerDraw", 60, "maxDraws", 100, "status", "ON_SHELF")
        ));
        
        return ResponseEntity.ok(result);
    }

    /**
     * 系統資訊
     */
    @GetMapping("/system-info")
    @Operation(summary = "系統資訊", description = "返回系統版本與環境資訊")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("applicationName", "KUJI Admin System");
        result.put("version", "1.0.0");
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        result.put("timestamp", LocalDateTime.now());
        result.put("profiles", System.getProperty("spring.profiles.active", "dev"));
        return ResponseEntity.ok(result);
    }
}
