# 賞品盒管理 Skill

## 適用情境
- 修改賞品盒狀態流轉邏輯
- 新增回收換紅利功能
- 了解 PrizeBox 與 Order 的關係
- 修改申請出貨流程

---

## 賞品盒狀態機

```
抽獎成功
    ↓
IN_BOX（在賞品盒）
    ├── 玩家申請出貨 → SHIPPING（OrderService 處理，並建立 Order）
    └── 玩家回收換紅利 → RECYCLED
```

### PrizeBoxStatusEnum
```java
IN_BOX     // 在賞品盒，可申請出貨或回收
SHIPPING   // 申請出貨中（已建立 Order）
DELIVERED  // 已到貨（Order 完成後更新）
RECYCLED   // 已回收換紅利
```

---

## 新增賞品到賞品盒（抽獎後呼叫）

```java
// PrizeBoxServiceImpl.addToPrizeBox()
@Transactional
public void addToPrizeBox(String userId, String lotteryId, String prizeId, String storeId, Long recycleBonus) {
    PrizeBox prizeBox = new PrizeBox();
    prizeBox.setId(UUID.randomUUID().toString());
    prizeBox.setUserId(userId);
    prizeBox.setLotteryId(lotteryId);
    prizeBox.setPrizeId(prizeId);
    prizeBox.setStoreId(storeId);
    prizeBox.setStatus(PrizeBoxStatusEnum.IN_BOX.getCode());
    prizeBox.setRecycleBonus(recycleBonus);  // 預計可回收的紅利值
    prizeBox.setIsShippable((byte) 1);
    prizeBox.setCreatedAt(LocalDateTime.now());
    prizeBox.setUpdatedAt(LocalDateTime.now());
    prizeBoxMapper.insert(prizeBox);
    log.info("✅ 獎品加入賞品盒: userId={}, prizeId={}", userId, prizeId);
}
```

---

## 回收換紅利

```java
// POST /api/prize-box/recycle
// Body: { "prizeBoxIds": ["uuid1", "uuid2"] }

@Transactional
public RecycleResultRes recycleForBonus(String userId, PrizeBoxRecycleReq req) {
    long totalBonus = 0L;

    for (String prizeBoxId : req.getPrizeBoxIds()) {
        // 1. 驗證所有權
        PrizeBox box = prizeBoxMapper.selectByPrimaryKey(prizeBoxId);
        if (box == null || !userId.equals(box.getUserId())) {
            throw new BusinessException("賞品盒 " + prizeBoxId + " 不存在或不屬於你");
        }

        // 2. 驗證狀態
        if (!PrizeBoxStatusEnum.IN_BOX.getCode().equals(box.getStatus())) {
            throw new BusinessException("賞品盒 " + prizeBoxId + " 狀態不允許回收");
        }

        // 3. 計算回收紅利
        Long bonus = box.getRecycleBonus() != null ? box.getRecycleBonus() : 0L;
        totalBonus += bonus;

        // 4. 更新狀態為已回收
        box.setStatus(PrizeBoxStatusEnum.RECYCLED.getCode());
        box.setRecycledAt(LocalDateTime.now());
        box.setUpdatedAt(LocalDateTime.now());
        prizeBoxMapper.updateByPrimaryKey(box);
    }

    // 5. 發放紅利
    if (totalBonus > 0) {
        walletService.addBonus(userId, totalBonus, "RECYCLE", null, "回收賞品換紅利");
    }

    log.info("✅ 回收完成: userId={}, 回收數={}, 總紅利={}", userId, req.getPrizeBoxIds().size(), totalBonus);
    return new RecycleResultRes(req.getPrizeBoxIds().size(), totalBonus);
}
```

---

## 查詢賞品盒（前台）

```java
// GET /api/prize-box?status=IN_BOX
public PageResult<PrizeBoxItemRes> getMyPrizeBox(String userId, String status, int page, int size) {
    PrizeBoxExample example = new PrizeBoxExample();
    PrizeBoxExample.Criteria criteria = example.createCriteria().andUserIdEqualTo(userId);

    if (status != null) {
        criteria.andStatusEqualTo(status);
    }
    example.setOrderByClause("created_at DESC");

    List<PrizeBox> boxes = prizeBoxMapper.selectByExample(example);

    // 轉換並補充獎品資訊
    List<PrizeBoxItemRes> items = boxes.stream().map(box -> {
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(box.getPrizeId());
        Lottery lottery = lotteryMapper.selectByPrimaryKey(box.getLotteryId());
        return toPrizeBoxItemRes(box, prize, lottery);
    }).collect(Collectors.toList());

    return PageResult.of(items, page, size);
}
```

---

## recycle_bonus 計算規則

```java
// 在 LotteryTicketServiceImpl.draw() 中計算
Long recycleBonus = lottery.getPricePerDraw() != null
    ? lottery.getPricePerDraw() / 2
    : 0L;
// 回收紅利 = 單抽價格的 50%
```

---

## PrizeBoxSummaryRes（摘要統計）

```java
// GET /api/prize-box/summary
public PrizeBoxSummaryRes getSummary(String userId) {
    // 統計各狀態數量
    // IN_BOX 數量、SHIPPING 數量、RECYCLED 總紅利
}
```

---

## 後台管理（Admin 查看）

```
GET /admin/prize-box?userId=xxx&status=IN_BOX
// Admin 可查看任意用戶的賞品盒
// StoreOwner 只能查看自己店家的賞品
```

---

## ⚠️ 禁止操作

- ❌ 不要讓 SHIPPING / RECYCLED 狀態的賞品再次被操作
- ❌ 不要讓一個賞品盒同時出現在多個 Order 中
- ❌ 回收時不要忘記同時寫入 WalletTransaction（addBonus 內部已處理）
- ❌ 不要在沒有驗證 userId 所有權的情況下執行回收或出貨
- ❌ recycle_bonus 只是預估值，不要在建立賞品盒時自動發放
