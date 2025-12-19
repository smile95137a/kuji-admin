package com.group.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.group.admin.entity.LotteryDrawRecord;
import com.group.admin.entity.User;
import com.group.admin.service.LotteryService;
import com.group.admin.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;
    private final UserService userService;

    @PostMapping("/{id}/draw")
    public ResponseEntity<LotteryDrawRecord> draw(@PathVariable("id") String lotteryId,
            @RequestParam(value = "costType", required = false, defaultValue = "gold") String costType) {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            return ResponseEntity.status(403).body(null);
        }
        String username = principal.toString();
        User user = userService.findByEmail(username);
        if (user == null) {
            return ResponseEntity.status(403).body(null);
        }

        LotteryDrawRecord record = lotteryService.draw(lotteryId, user.getId(), costType);
        return ResponseEntity.ok(record);
    }
}
