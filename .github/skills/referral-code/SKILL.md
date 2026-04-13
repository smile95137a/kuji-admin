---
name: referral-code
description: "推薦碼系統完整流程。推薦碼生成、驗證、獎勵分配、推薦記錄追蹤。"
---

# 推薦碼系統

## When to Use
- 了解推薦碼生成規則
- 修改推薦碼綁定流程
- 調整雙方紅利發放邏輯
- 修改使用次數限制

## 核心原則
- **一碼一會員玩家**：推薦碼創建保證唯一，策略不可複資取漫
- **經驗帽屬涉**：誰英述下次顯示例、詢英實數不可購子補窗，刑事 填伏購子一子
- **素屬罷技受分傷**：推薦記錄不切件爹加出貨方漁漇潛水下潭一子潯可幣方漆罰方子子
- **方漇冊筵漁字**：對部扶菰已推薦值不可檔善秘常

---

## 核心設計

| 欄位 | 說明 |
|------|------|
| `code` | 6 位英數字推薦碼（唯一） |
| `storeId` | 歸屬的店家（非必填，平台級推薦碼可為 null） |
| `userId` | 推薦碼建立者（非必填） |
| `bonusPerUse` | 每次使用發放的紅利（預設 50） |
| `maxUsage` | 最大使用次數（預設 100，0=無限） |
| `usageCount` | 目前已使用次數 |
| `isActive` | 1=啟用, 0=停用 |
| `expiresAt` | 過期時間（null=不限期） |

---

## 推薦碼生成

```java
// ReferralCodeServiceImpl.createCode()
@Transactional
public ReferralCodeRes createCode(ReferralCodeCreateReq req) {
    // 生成唯一 6 位推薦碼
    String code = generateUniqueCode();

    ReferralCode referralCode = new ReferralCode();
    referralCode.setId(UUID.randomUUID().toString());
    referralCode.setCode(code);
    referralCode.setStoreId(req.getStoreId());
    referralCode.setBonusPerUse(req.getBonusPerUse() != null ? req.getBonusPerUse() : DEFAULT_BONUS_PER_USE);
    referralCode.setMaxUsage(req.getMaxUsage() != null ? req.getMaxUsage() : DEFAULT_MAX_USAGE);
    referralCode.setUsageCount(0);
    referralCode.setIsActive(1);
    referralCode.setCreatedAt(LocalDateTime.now());
    referralCodeMapper.insert(referralCode);

    return toRes(referralCode);
}

private String generateUniqueCode() {
    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去掉易混淆字符 O, 0, I, 1
    for (int attempt = 0; attempt < 10; attempt++) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        // 檢查唯一性
        ReferralCodeExample example = new ReferralCodeExample();
        example.createCriteria().andCodeEqualTo(code.toString());
        if (referralCodeMapper.selectByExample(example).isEmpty()) {
            return code.toString();
        }
    }
    throw new BusinessException("推薦碼生成失敗，請重試");
}
```

---

## 推薦碼綁定流程（新用戶註冊時）

```java
// ReferralCodeServiceImpl.bindReferralCode()
@Transactional
public void bindReferralCode(String newUserId, String code) {
    // 1. 查詢推薦碼
    ReferralCodeExample example = new ReferralCodeExample();
    example.createCriteria().andCodeEqualTo(code.toUpperCase()).andIsActiveEqualTo(1);
    List<ReferralCode> codes = referralCodeMapper.selectByExample(example);

    if (codes.isEmpty()) {
        log.warn("⚠️ 推薦碼不存在或已停用: {}", code);
        return; // 靜默失敗，不影響主流程
    }
    ReferralCode referralCode = codes.get(0);

    // 2. 驗證有效性
    if (referralCode.getMaxUsage() > 0 && referralCode.getUsageCount() >= referralCode.getMaxUsage()) {
        log.warn("⚠️ 推薦碼已達上限: {}", code);
        return;
    }
    if (referralCode.getExpiresAt() != null && referralCode.getExpiresAt().isBefore(LocalDateTime.now())) {
        log.warn("⚠️ 推薦碼已過期: {}", code);
        return;
    }

    // 3. 防止重複綁定（一個用戶只能使用一次推薦碼）
    ReferralRecordExample recordExample = new ReferralRecordExample();
    recordExample.createCriteria()
        .andNewUserIdEqualTo(newUserId)
        .andReferralCodeIdEqualTo(referralCode.getId());
    if (!referralRecordMapper.selectByExample(recordExample).isEmpty()) {
        return; // 已綁定過，靜默跳過
    }

    // 4. 建立綁定記錄
    ReferralRecord record = new ReferralRecord();
    record.setId(UUID.randomUUID().toString());
    record.setReferralCodeId(referralCode.getId());
    record.setNewUserId(newUserId);
    record.setBonusAwarded(referralCode.getBonusPerUse());
    record.setCreatedAt(LocalDateTime.now());
    referralRecordMapper.insert(record);

    // 5. 發放紅利給新用戶
    walletService.addBonus(newUserId, referralCode.getBonusPerUse(), "REFERRAL", referralCode.getId(), "推薦碼獎勵");

    // 6. 更新使用次數
    referralCode.setUsageCount(referralCode.getUsageCount() + 1);
    referralCode.setUpdatedAt(LocalDateTime.now());
    referralCodeMapper.updateByPrimaryKey(referralCode);

    log.info("✅ 推薦碼綁定成功: newUserId={}, code={}, bonus={}", newUserId, code, referralCode.getBonusPerUse());
}
```

---

## 推薦統計

```java
// GET /api/referral/stats
public ReferralStatsRes getStats(String userId) {
    // 查詢該用戶建立的所有推薦碼的使用記錄
    // 返回：總使用次數、總發出紅利、各推薦碼統計
}
```

---

## ⚠️ 禁止操作

- ❌ 不要讓同一用戶重複使用同一個推薦碼
- ❌ 推薦碼驗證失敗時不要拋例外（靜默失敗，不影響主流程）
- ❌ 不要在推薦碼已達上限或過期時仍然發放紅利
- ❌ 不要在沒有 @Transactional 的情況下執行綁定 + 發放紅利
- ❌ 推薦碼大小寫不敏感（統一轉大寫存儲和查詢）
