# 00 - 整體架構與通用規則

## API 基礎設定

```
Base URL      : http://localhost:8080
Context Path  : /api
前台路由（公開）: /api/stores/**, /api/lottery/browse/**, /api/recharge-plan/**
前台路由（需登入）: /api/user/**, /api/lottery/draw/**, /api/lottery/random/**,
                   /api/prize-box/**, /api/order/**, /api/wallet/**, /api/referral/**
認證路由（無需登入）: /api/auth/**
```

---

## 統一回應格式

```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: {
    code: string;       // 業務錯誤碼
    message: string;    // 人類可讀訊息
  } | null;
  meta: {
    timestamp: string;  // ISO 8601
    requestId: string;  // UUID
  };
}
```

---

## HTTP 狀態碼

| 狀態碼 | 含義 | 前端處理 |
|--------|------|---------|
| 200 | 成功 | 讀取 `data` |
| 201 | 建立成功 | 讀取 `data` |
| 204 | 操作成功（無 body） | 無需處理 |
| 400 | 請求參數錯誤 | 顯示 `error.message` |
| 401 | Token 過期或未登入 | 嘗試刷新 Token，失敗則導向登入頁 |
| 403 | 無權限（帳號被停用等） | 顯示「帳號已停用」 |
| 404 | 資源不存在 | 顯示找不到 |
| 409 | 衝突（Email 重複等） | 顯示具體訊息 |
| 500 | 伺服器錯誤 | 顯示「稍後再試」 |

---

## 端點分類

### 公開端點（不需 Authorization Header）
```
GET  /api/stores/list
POST /api/lottery/browse/list
GET  /api/lottery/browse/{id}/detail
GET  /api/lottery/{id}
GET  /api/recharge-plan/list
GET  /api/recharge-plan/{id}
GET  /api/news/...
GET  /api/banner/...
GET  /api/marquee/...
POST /api/auth/register
POST /api/auth/login
POST /api/auth/google
POST /api/auth/refresh
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

### 需要登入的端點
```
GET/PUT  /api/user/me
POST/GET /api/user/addresses/**
POST     /api/lottery/draw/{id}/draw
POST     /api/lottery/random/{id}/draw
GET/POST /api/prize-box/**
POST/GET /api/order/**
GET/POST /api/wallet/**
GET/POST /api/referral/**
POST     /api/recharge
```

---

## Token Axios Interceptor 模板

```javascript
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const api = axios.create({ baseURL: 'http://localhost:8080/api' })

// 請求攔截：自動帶 Token
api.interceptors.request.use(config => {
  const token = useAuthStore().accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 回應攔截：Token 過期自動刷新
api.interceptors.response.use(
  res => res,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const { data } = await axios.post('/api/auth/refresh', { refreshToken })
          useAuthStore().setToken(data.data.accessToken)
          localStorage.setItem('refreshToken', data.data.refreshToken)
          error.config.headers.Authorization = `Bearer ${data.data.accessToken}`
          return api(error.config)
        } catch {
          useAuthStore().logout()
          router.push('/login')
        }
      } else {
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default api
```

---

## 點數系統說明

### 雙幣種
| 欄位 | 名稱 | 說明 |
|------|------|------|
| `goldCoins` | 金幣 | 儲值購買，主要貨幣 |
| `bonusCoins` | 紅利 | 系統贈送，次要貨幣 |

### 扣款優先順序
```
抽獎費用 = pricePerDraw × count
if (goldCoins >= 費用) → 全用金幣
else → 先用完所有金幣，剩餘用紅利補足
```

### 紅利取得方式
- 推薦碼獎勵（被推薦人首次抽獎後，推薦人獲得）
- 回收獎品（將獎品盒中的獎品轉換為紅利）
- 系統不定期贈送

---

## 商品類型與抽獎路由速查

| category | subCategory | 前台抽獎方式 | API 路由 |
|---------|------------|------------|---------|
| `OFFICIAL_ICHIBAN` | - | 籤位制 | `POST /lottery/draw/{id}/draw` |
| `GACHA` | - | 加權隨機 | `POST /lottery/random/{id}/draw` |
| `TRADING_CARD` | - | 籤位制 | `POST /lottery/draw/{id}/draw` |
| `CUSTOM_GACHA` | `LOTTERY_MODE` | 籤位制 | `POST /lottery/draw/{id}/draw` |
| `CUSTOM_GACHA` | `SCRATCH_MODE` | 刮刮樂（籤位制變形） | `POST /lottery/draw/{id}/draw` |

---

## 商品狀態（前台只顯示 ON_SHELF）
```
DRAFT      草稿（前台不顯示）
ON_SHELF   上架中（前台可見）
OFF_SHELF  已下架（前台不顯示）
RUNNING    抽獎進行中（前台可見、可抽）
COMPLETED  已完結（前台可見但不可抽）
```

---

## 枚舉值速查

### 訂單狀態
```
PENDING_PAYMENT   待付款（出貨申請後，玩家確認前）
PAID              已付款（金幣扣除完成）
PREPARING         備貨中（店家確認）
SHIPPED           已出貨
DELIVERED         已送達
COMPLETED         已完成
CANCELLED         已取消
REFUNDED          已退款
```

### 獎品盒狀態
```
AVAILABLE    在盒中（可申請出貨或回收）
SHIPPING     出貨中（申請後等待物流）
SHIPPED      已出貨
RECYCLED     已回收（轉換為紅利）
```

### 帳號狀態
```
ACTIVE      正常
INACTIVE    停用（登入會返回 403）
SUSPENDED   暫停（違規）
```
