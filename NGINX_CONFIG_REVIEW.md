# Nginx 配置檢查與優化建議

## 您目前的配置分析

### ✅ 配置正確的部分

1. **基本結構**：
   - ✅ 監聽 Port 80
   - ✅ Server name 正確
   - ✅ Gzip 壓縮設置良好
   - ✅ 後端 API 代理配置完整

2. **API 代理設置**：
   - ✅ `/api/` 正確代理到 `http://localhost:8080/api/`
   - ✅ 包含必要的 proxy headers
   - ✅ 超時設置合理

3. **前端設置**：
   - ✅ 前端在 `/kuji/` 路徑下
   - ✅ 靜態資源緩存策略正確

---

## ⚠️ 需要注意的問題

### 問題 1：健康檢查端點遮蔽

**目前配置：**
```nginx
location /health {
    access_log off;
    return 200 "healthy\n";
    add_header Content-Type text/plain;
}
```

**問題：**
- 這會遮蔽後端的健康檢查 API
- 如果您想檢查後端應用程式的健康狀態，應該代理到後端

**建議修改：**

#### 選項 A：Nginx 自身健康檢查 + 後端健康檢查分離
```nginx
# Nginx 自身健康檢查
location /health {
    access_log off;
    return 200 "nginx healthy\n";
    add_header Content-Type text/plain;
}

# 後端應用健康檢查（透過 /api/ 代理）
# 訪問：http://18.179.187.129/api/actuator/health
```

#### 選項 B：直接代理到後端健康檢查（推薦）
```nginx
# 移除獨立的 /health 端點
# 直接使用 /api/actuator/health
# 訪問：http://18.179.187.129/api/actuator/health
```

---

### 問題 2：前端 index.html 路徑可能不對

**目前配置：**
```nginx
location /kuji/ {
    alias /var/www/kuji-admin/;
    try_files $uri $uri/ /kuji/index.html;
}
```

**問題：**
- `try_files` 使用 `/kuji/index.html` 但 `alias` 已經指向 `/var/www/kuji-admin/`
- 可能導致 404

**正確配置：**
```nginx
location /kuji/ {
    alias /var/www/kuji-admin/;
    # ✅ 使用相對於 alias 的路徑
    try_files $uri $uri/ /kuji/index.html;
    index index.html;
}
```

或者更清楚的寫法：
```nginx
location /kuji/ {
    alias /var/www/kuji-admin/;
    try_files $uri $uri/ @kuji_fallback;
    index index.html;
}

location @kuji_fallback {
    rewrite ^/kuji/(.*)$ /kuji/index.html break;
}
```

---

## 📋 完整優化後的配置（建議）

```nginx
server {
    listen 80;
    server_name 18.179.187.129;

    # 前端靜態文件根目錄
    root /var/www/kuji-admin;

    # 日誌配置
    access_log /var/log/nginx/kuji-access.log;
    error_log /var/log/nginx/kuji-error.log;

    # Gzip 壓縮設置
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/json application/javascript;

    # 🔹 後端 API 代理（最高優先級）
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # 超時設置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        # 緩衝設置
        proxy_buffering off;
    }

    # 🔹 前端路由 - 管理後台在 /kuji/ 路徑下
    location /kuji/ {
        alias /var/www/kuji-admin/;
        try_files $uri $uri/ /kuji/index.html;
        index index.html;
        
        # 靜態資源緩存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
        
        # HTML 文件不緩存
        location ~* \.(html)$ {
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
    }

    # 🔹 根路徑重定向到 /kuji/
    location = / {
        return 301 /kuji/;
    }

    # 🔹 Nginx 健康檢查（可選）
    location /nginx-health {
        access_log off;
        return 200 "nginx ok\n";
        add_header Content-Type text/plain;
    }

    # 🔹 錯誤頁面
    error_page 404 /kuji/index.html;
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

---

## 🔧 立即檢查與修復步驟

### 步驟 1：測試當前配置語法

SSH 到 EC2 執行：
```bash
sudo nginx -t
```

**預期輸出：**
```
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

---

### 步驟 2：檢查 Nginx 服務狀態

```bash
sudo systemctl status nginx
```

**預期：**
```
● nginx.service - The nginx HTTP and reverse proxy server
   Active: active (running)
```

---

### 步驟 3：測試各個端點

#### 測試後端 API（健康檢查）
```bash
# 從 EC2 內部測試
curl http://localhost/api/actuator/health

# 從外部測試
curl http://18.179.187.129/api/actuator/health
```

**預期：**
```json
{"status":"UP"}
```

#### 測試前端頁面
```bash
curl -I http://18.179.187.129/kuji/
```

**預期：**
```
HTTP/1.1 200 OK
Content-Type: text/html
```

#### 測試根路徑
```bash
curl -I http://18.179.187.129/
```

---

### 步驟 4：查看 Nginx 日誌

```bash
# 查看訪問日誌
sudo tail -f /var/log/nginx/access.log

# 查看錯誤日誌
sudo tail -f /var/log/nginx/error.log
```

---

## 🐛 常見問題排除

### 問題 A：404 Not Found（前端頁面）

**檢查：**
```bash
# 確認前端文件是否存在
ls -la /var/www/kuji-admin/
ls -la /var/www/kuji-admin/index.html
```

**如果文件不存在：**
```bash
# 確認正確的路徑
sudo find /var -name "index.html" -type f 2>/dev/null
```

---

### 問題 B：502 Bad Gateway（後端 API）

**原因：後端應用未運行**

**檢查：**
```bash
# 檢查 Java 應用是否運行
ps aux | grep admin-1.0.0.jar

# 檢查 8080 端口
sudo netstat -tlnp | grep 8080
```

**解決：**
```bash
# 啟動後端應用
cd /home/ec2-user/kuji-admin
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m -Xmx2048m -XX:+UseG1GC \
    admin-1.0.0.jar > /home/ec2-user/logs/app.log 2>&1 &
```

---

### 問題 C：後端健康檢查被 Nginx 的 /health 遮蔽

**解決：**

#### 選項 1：修改 Nginx 配置（推薦）
```bash
sudo nano /etc/nginx/conf.d/kuji.conf
```

移除或註釋掉：
```nginx
# location /health {
#     access_log off;
#     return 200 "healthy\n";
#     add_header Content-Type text/plain;
# }
```

重新加載：
```bash
sudo nginx -s reload
```

#### 選項 2：使用不同的路徑
- Nginx 健康檢查：`/nginx-health`
- 後端健康檢查：`/api/actuator/health`

---

## 📊 完整測試清單

完成配置後，執行以下測試：

### 從 EC2 內部測試

```bash
# 1. Nginx 自身
curl http://localhost/

# 2. 前端頁面
curl -I http://localhost/kuji/

# 3. 後端 API 健康檢查
curl http://localhost/api/actuator/health

# 4. 後端登入 API（測試用）
curl -X POST http://localhost/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@kuji.com","password":"admin123"}'
```

### 從外部測試（本地 Windows）

```bash
# 1. 前端頁面
curl -I http://18.179.187.129/kuji/

# 2. 後端健康檢查
curl http://18.179.187.129/api/actuator/health

# 3. 瀏覽器訪問
# http://18.179.187.129/kuji/
# http://18.179.187.129/api/actuator/health
```

---

## 🎯 建議的最終配置（複製貼上版）

如果您想完全替換配置，使用以下完整版本：

```bash
# 備份現有配置
sudo cp /etc/nginx/conf.d/kuji.conf /etc/nginx/conf.d/kuji.conf.backup.$(date +%Y%m%d_%H%M%S)

# 編輯配置
sudo nano /etc/nginx/conf.d/kuji.conf
```

**完整配置內容：**

```nginx
server {
    listen 80;
    server_name 18.179.187.129;

    # 日誌
    access_log /var/log/nginx/kuji-access.log;
    error_log /var/log/nginx/kuji-error.log;

    # Gzip 壓縮
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/json application/javascript;

    # 後端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        proxy_buffering off;
    }

    # 前端靜態文件
    location /kuji/ {
        alias /var/www/kuji-admin/;
        try_files $uri $uri/ /kuji/index.html;
        index index.html;
        
        # JS/CSS 長期緩存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
        
        # HTML 不緩存
        location ~* \.(html)$ {
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
    }

    # 根路徑重定向
    location = / {
        return 301 /kuji/;
    }

    # 錯誤頁面
    error_page 404 /kuji/index.html;
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

**保存後測試並重新加載：**
```bash
sudo nginx -t && sudo nginx -s reload
```

---

## ✅ 配置驗證步驟（立即執行）

請在 SSH 連線中執行以下命令，然後提供輸出結果：

```bash
# 1. 檢查配置語法
echo "=== Nginx 配置測試 ===" && sudo nginx -t

# 2. 檢查服務狀態
echo -e "\n=== Nginx 服務狀態 ===" && sudo systemctl status nginx | head -10

# 3. 檢查後端應用
echo -e "\n=== 後端應用狀態 ===" && ps aux | grep admin-1.0.0.jar | grep -v grep

# 4. 檢查端口
echo -e "\n=== 端口監聽 ===" && sudo netstat -tlnp | grep -E ':(80|8080)'

# 5. 測試本地訪問
echo -e "\n=== 後端健康檢查 ===" && curl -s http://localhost/api/actuator/health

# 6. 測試前端
echo -e "\n=== 前端頁面 ===" && curl -I http://localhost/kuji/ 2>&1 | head -5
```

---

## 📝 總結

### 您的配置狀況：

| 項目 | 狀態 | 說明 |
|------|------|------|
| 基本結構 | ✅ 正確 | 監聽 80，代理 8080 |
| API 代理 | ✅ 正確 | `/api/` → `http://localhost:8080/api/` |
| 前端設置 | ⚠️ 需檢查 | `try_files` 路徑可能需要調整 |
| 健康檢查 | ⚠️ 需修改 | 可能遮蔽後端健康檢查 |
| 緩存策略 | ✅ 正確 | 靜態資源緩存設置良好 |

### 建議行動：

1. ✅ **立即執行驗證命令**（見上方）
2. ⚠️ **考慮移除獨立的 `/health` 端點**（避免遮蔽後端）
3. ✅ **測試所有端點**（前端、後端 API、健康檢查）
4. ✅ **確認前端文件路徑正確**

---

請執行上方的「配置驗證步驟」命令，並提供輸出結果，我可以幫您進一步診斷！🔍
