package com.group.admin.controller;

import com.group.admin.req.menu.MenuCreateReq;
import com.group.admin.req.menu.MenuUpdateReq;
import com.group.admin.res.menu.MenuRes;
import com.group.admin.res.menu.MenuTreeRes;
import com.group.admin.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 選單管理 Controller
 *
 * <p>提供選單的 CRUD 操作及樹狀結構查詢</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Tag(name = "選單管理", description = "選單的新增、修改、刪除、查詢等操作")
@RestController
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 建立選單
     *
     * @param req 選單建立請求
     * @return 建立後的選單資料
     */
    @Operation(summary = "建立選單", description = "建立新的選單項目")
    @PostMapping
    public ResponseEntity<MenuRes> createMenu(
            @Valid @RequestBody MenuCreateReq req) {
        MenuRes res = menuService.createMenu(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 更新選單
     *
     * @param req 選單更新請求
     * @return 更新後的選單資料
     */
    @Operation(summary = "更新選單", description = "更新現有選單的資訊")
    @PutMapping
    public ResponseEntity<MenuRes> updateMenu(
            @Valid @RequestBody MenuUpdateReq req) {
        MenuRes res = menuService.updateMenu(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 刪除選單
     *
     * @param id 選單ID
     * @return 無內容
     */
    @Operation(summary = "刪除選單", description = "刪除指定的選單（不可刪除有子選單的選單）")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(
            @Parameter(description = "選單ID") @PathVariable String id) {
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根據ID查詢選單
     *
     * @param id 選單ID
     * @return 選單資料
     */
    @Operation(summary = "查詢選單", description = "根據ID查詢單一選單")
    @GetMapping("/{id}")
    public ResponseEntity<MenuRes> getMenuById(
            @Parameter(description = "選單ID") @PathVariable String id) {
        MenuRes res = menuService.getMenuById(id);
        return ResponseEntity.ok(res);
    }

    /**
     * 查詢所有選單（平面列表）
     *
     * @return 選單列表
     */
    @Operation(summary = "查詢所有選單", description = "取得所有選單的平面列表")
    @GetMapping
    public ResponseEntity<List<MenuRes>> getAllMenus() {
        List<MenuRes> res = menuService.getAllMenus();
        return ResponseEntity.ok(res);
    }

    /**
     * 查詢選單樹狀結構
     *
     * @return 選單樹
     */
    @Operation(summary = "查詢選單樹", description = "取得選單的階層式樹狀結構")
    @GetMapping("/tree")
    public ResponseEntity<List<MenuTreeRes>> getMenuTree() {
        List<MenuTreeRes> res = menuService.getMenuTree();
        return ResponseEntity.ok(res);
    }

    /**
     * 根據當前登入用戶查詢可訪問的選單樹
     *
     * @return 可訪問的選單樹
     */
    @Operation(summary = "查詢可訪問選單", description = "根據當前登入用戶的角色取得可訪問的選單樹（用於前端動態渲染選單）")
    @GetMapping("/accessible")
    public ResponseEntity<List<MenuTreeRes>> getAccessibleMenuTree() {
        String adminUserId = com.group.admin.util.SecurityUtils.getCurrentAdminUserId();
        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }
        List<MenuTreeRes> res = menuService.getAccessibleMenuTree(adminUserId);
        return ResponseEntity.ok(res);
    }

    /**
     * 根據選單代碼查詢
     *
     * @param code 選單代碼
     * @return 選單資料
     */
    @Operation(summary = "根據代碼查詢選單", description = "根據選單代碼查詢選單資訊")
    @GetMapping("/code/{code}")
    public ResponseEntity<MenuRes> getMenuByCode(
            @Parameter(description = "選單代碼") @PathVariable String code) {
        MenuRes res = menuService.getMenuByCode(code);
        return ResponseEntity.ok(res);
    }
}
