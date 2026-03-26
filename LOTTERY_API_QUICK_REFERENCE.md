# 🎯 前後台商品 API 快速參考

## API 路由
```
前台：POST /api/lottery/browse/list    （不需要 token，只查上架商品）
後台：POST /api/admin/lottery/list     （需要 token，可查所有狀態）
```

## 回應格式（含中文）
```json
{
  "category": "CUSTOM_GACHA",
  "categoryName": "自製賞",         ✅ 中文
  "subCategory": "SCRATCH_MODE",
  "subCategoryName": "刮刮樂型",    ✅ 中文（新增）
  "status": "ON_SHELF",
  "statusName": "已上架"            ✅ 中文
}
```

## Enum 對照表

### 商品分類
- `OFFICIAL_ICHIBAN` → 官方一番賞
- `GACHA` → 扭蛋
- `TRADING_CARD` → 卡牌
- `CUSTOM_GACHA` → 自製賞

### 子分類
- `LOTTERY_MODE` → 抽籤型
- `SCRATCH_MODE` → 刮刮樂型

### 狀態
- `DRAFT` → 草稿
- `OFF_SHELF` → 已下架
- `ON_SHELF` → 已上架
- `IN_PROGRESS` → 抽獎中
- `ENDED` → 已結束
- `FORCED_OFF` → 強制下架

## 測試命令
```bash
# 測試腳本
test-lottery-chinese-names.bat

# 手動測試前台
curl -X POST http://localhost:8080/api/lottery/browse/list -H "Content-Type: application/json" -d "{}"

# 手動測試後台
curl -X POST http://localhost:8080/api/admin/lottery/list -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{}"
```

## 前端使用
```typescript
// ✅ 直接顯示中文
console.log(lottery.categoryName);     // "自製賞"
console.log(lottery.subCategoryName);  // "刮刮樂型"
console.log(lottery.statusName);       // "已上架"

// 過濾敏感欄位（前台選擇性）
const { createdBy, remark, ...public } = lottery;
```

## 修改檔案
- ✅ LotteryRes.java（新增 subCategoryName）
- ✅ LotteryServiceImpl.java（設定中文名稱）
- ✅ 測試腳本與文件

**版本**: 2.0.0 | **狀態**: ✅ 完成
