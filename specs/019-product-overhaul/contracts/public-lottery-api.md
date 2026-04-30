# Public Lottery API Contract

**Feature**: `019-product-overhaul`  
**Scope**: 前台商品列表/詳情讀取相容性

## 1. 路徑

| Method | Path | 契約要求 |
|--------|------|----------|
| `GET` | `/lottery` | 列表分頁查詢維持既有行為 |
| `POST` | `/lottery/list` | 列表查詢維持既有行為 |
| `GET` | `/lottery/{uuid}` | 公開商品詳情維持既有可存取規則 |
| `GET` | `/lottery/{id}` | 舊格式詳情路徑維持相容 |

## 2. Response Semantics

| 欄位 | 契約 |
|------|------|
| `playMode` | 由後端依 `category + subCategory` 推導，不受 client 影響 |
| `gameMode` | 僅刮刮樂有值；`CUSTOM_GACHA + LOTTERY_MODE` 不得誤回傳刮刮樂模式 |
| `paymentType` | 若公開回應包含此欄位，應反映實際扣款幣種 |
| `freeDrawThreshold` | 僅 `CUSTOM_GACHA + SCRATCH_MODE` 可能有值；`NULL` 代表未啟用免費抽/免單機制 |
| `delistStrategy` | 若公開回應包含此欄位，應反映實際生效策略，而非 client 原始輸入 |

## 3. Downstream Compatibility

- 本次規則變更不得破壞既有前台依 `playMode` / `gameMode` 進行畫面或抽獎流程分流的邏輯。
- `CUSTOM_GACHA + LOTTERY_MODE` 應繼續走籤位制行為；`CUSTOM_GACHA + SCRATCH_MODE` 應繼續走刮刮樂流程。
- 若前台既有文件聲明 `playMode` 不需送入 request，後端必須維持此假設成立。
