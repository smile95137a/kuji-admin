package com.group.admin.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;

import com.group.admin.entity.User;
import com.group.admin.service.UserService;
import lombok.RequiredArgsConstructor;

/**
 * 前台使用者 API
 * 
 * URL: /api/user/**
 * 角色：前台使用者
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello world");
    }

    @GetMapping("/me")
    public ResponseEntity<User> me() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            return ResponseEntity.status(403).body(null);
        }
        String username = principal.toString();
        var user = userService.findByEmail(username);
        if (user == null) return ResponseEntity.status(404).body(null);
        return ResponseEntity.ok(user);
    }

}
