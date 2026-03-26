# ✅ 啟動前檢查清單

## 📋 檔案檢查

- [x] `LocalFileServiceImpl.java` - 無編譯錯誤
- [x] `UploadController.java` - 無編譯錯誤
- [x] `application.yml` - 無編譯錯誤（IntelliJ 警告可忽略）
- [x] `S3Config.java` - 已刪除
- [x] `S3ServiceImpl.java` - 已刪除
- [x] 圖片目錄已建立（news/banner/lottery/prize）

## 🚀 現在可以啟動！

### 方式 1：使用快速啟動腳本（推薦）
```bash
start-test.bat
```

### 方式 2：手動啟動
```bash
# 1. 編譯
mvn clean package -DskipTests

# 2. 啟動
mvn spring-boot:run
```

## 📝 啟動後的第一個測試

### 1. 取得 Admin Token
```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}"
```

**設定為環境變數：**
```bash
set TOKEN=Bearer eyJhbGc...
```

### 2. 測試 Enum API（無需登入）
```bash
curl http://localhost:8080/api/enums/all
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "lotteryStatus": [...],
    "bannerStatus": [...],
    "newsStatus": [...]
  }
}
```

### 3. 測試店家選項 API（無需登入）
```bash
curl http://localhost:8080/api/stores/options
```

**預期回應：**
```json
{
  "success": true,
  "data": [
    {
      "label": "官方旗艦店",
      "value": "uuid-store-123",
      "description": "KUJI 官方商店"
    }
  ]
}
```

### 4. 測試圖片上傳（需 Admin 權限）

**準備測試圖片：**
- 檔名：`test.jpg`
- 大小：< 5MB
- 格式：jpg/png/gif/webp

```bash
curl -X POST http://localhost:8080/api/admin/upload/news ^
  -H "Authorization: %TOKEN%" ^
  -F "file=@test.jpg"
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "imageUrl": "/img/news/uuid-123.jpg"
  }
}
```

**驗證檔案是否儲存：**
```bash
dir src\main\resources\static\img\news
```

**驗證是否可存取：**
```
在瀏覽器開啟：http://localhost:8080/img/news/uuid-123.jpg
```

---

## ⚠️ 已知 IntelliJ 警告（可忽略）

1. **Unknown property 'spring.devtools'**
   - 原因：IntelliJ 不認識自定義 devtools 設定
   - 影響：無，Spring Boot 可正常讀取

2. **Unknown property 'file'**
   - 原因：IntelliJ 不認識自定義 file.upload 設定
   - 影響：無，@Value 可正常注入

3. **This key is used in a map and contains special characters**
   - 原因：`com.group` 包含點號
   - 影響：無，YAML 可正常解析

---

## 🎯 測試重點

### 必測項目（優先）：
1. ✅ Enum API 返回正確格式
2. ✅ 店家選項 API 可用
3. ✅ 圖片上傳成功且可存取
4. ✅ Banner 新增時可選擇店家
5. ✅ Banner 查詢時 storeName 有正確顯示

### 完整測試（參考 COMPLETE_TEST_PLAN.md）：
- News 完整 CRUD（8 個測試）
- Banner 完整 CRUD（10 個測試）
- 時間排程（1 個測試）
- 店家關聯（2 個測試）
- 權限控管（2 個測試）

---

**準備完成！執行 `start-test.bat` 開始測試！** 🚀
