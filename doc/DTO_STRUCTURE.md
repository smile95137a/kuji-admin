# DTO 目錄結構設計

## 📁 目錄結構

```
src/main/java/com/group/admin/
├── res/
│   ├── prizebox/
│   │   ├── PrizeBoxItemRes.java           # 賞品盒項目回應
│   │   └── PrizeBoxSummaryRes.java        # 賞品盒摘要（按店家分組）
│   ├── wallet/
│   │   ├── UserWalletRes.java             # 錢包資訊回應
│   │   ├── WalletTransactionRes.java      # 交易記錄回應
│   │   └── RechargePlanRes.java           # 儲值方案回應
│   └── order/
│       ├── OrderRes.java                   # 訂單回應
│       ├── OrderDetailRes.java             # 訂單詳情回應
│       └── OrderItemRes.java               # 訂單項目回應
├── req/
│   ├── prizebox/
│   │   ├── PrizeBoxShipReq.java           # 出貨請求
│   │   └── PrizeBoxRecycleReq.java        # 回收請求
│   ├── wallet/
│   │   ├── RechargeReq.java               # 儲值請求
│   │   └── WalletAdjustReq.java           # 手動調整請求（Admin）
│   ├── recharge/
│   │   ├── RechargePlanCreateReq.java     # 新增儲值方案
│   │   └── RechargePlanUpdateReq.java     # 更新儲值方案
│   └── order/
│       ├── OrderCondition.java             # 訂單查詢條件
│       ├── OrderShipReq.java               # 訂單出貨請求
│       └── OrderCancelReq.java             # 訂單取消請求
└── condition/
    ├── WalletTransactionCondition.java     # 交易記錄查詢條件
    └── OrderCondition.java                 # 訂單查詢條件
```

## 🎯 優先順序

### Phase 1: 錢包系統（立即需要）
- [x] UserWalletRes
- [x] WalletTransactionRes
- [x] WalletAdjustReq
- [ ] WalletTransactionCondition

### Phase 2: 賞品盒（核心功能）
- [ ] PrizeBoxItemRes
- [ ] PrizeBoxSummaryRes
- [ ] PrizeBoxShipReq
- [ ] PrizeBoxRecycleReq

### Phase 3: 儲值系統
- [ ] RechargePlanRes
- [ ] RechargePlanCreateReq
- [ ] RechargePlanUpdateReq
- [ ] RechargeReq

### Phase 4: 訂單系統
- [ ] OrderRes
- [ ] OrderDetailRes
- [ ] OrderItemRes
- [ ] OrderCondition
- [ ] OrderShipReq
- [ ] OrderCancelReq

## 📝 命名規範

- **Res**：回應 DTO（給前端）
- **Req**：請求 DTO（從前端接收）
- **Condition**：查詢條件 DTO（繼承 BaseCondition）
- **Detail**：詳細資訊 DTO（包含關聯物件）
- **Summary**：摘要資訊 DTO（精簡版）
