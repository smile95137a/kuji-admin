# 🔍 網路連線問題診斷與解決

## 問題現象

- ✅ 服務已啟動（PID: 199499）
- ❌ 無法從外部存取 `http://18.179.187.129/api/actuator/health`

---

## 🚨 最可能的原因：EC2 Security Group 未開放 8080 端口

### 診斷步驟（在 EC2 上執行）

```bash
# 1. 檢查服務是否真的在監聽 8080 端口
netstat -tuln | grep 8080
# 或
ss -tuln | grep 8080

# 2. 檢查本機是否可以連線
curl http://localhost:8080/api/actuator/health

# 3. 檢查防火牆（如果有）
sudo firewall-cmd --list-all
# 或
sudo iptables -L -n
```

---

## ✅ 解決方案

### 方案 1：在 AWS Console 開放 EC2 Security Group（推薦）

1. **登入 AWS Console**
   - 前往 EC2 Dashboard
   - 找到您的 Instance：`18.179.187.129`

2. **修改 Security Group**
   - 點選 Instance → Security → Security Groups
   - 點選您的 Security Group
   - 點選 "Edit inbound rules"

3. **新增規則**
   ```
   Type: Custom TCP
   Protocol: TCP
   Port Range: 8080
   Source: 0.0.0.0/0 (或您的 IP)
   Description: KUJI Admin API
   ```

4. **儲存規則**

5. **測試連線**
   ```bash
   # 在本機執行
   curl http://18.179.187.129:8080/api/actuator/health
   ```

---

### 方案 2：使用 Nginx 反向代理（80 端口）

如果您想使用標準的 80 端口而不是 8080，可以安裝 Nginx：

```bash
# 在 EC2 上執行
sudo yum install -y nginx

# 配置 Nginx
sudo nano /etc/nginx/conf.d/kuji-admin.conf
```

**Nginx 配置內容：**

```nginx
server {
    listen 80;
    server_name 18.179.187.129;

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /actuator/ {
        proxy_pass http://localhost:8080/actuator/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**啟動 Nginx：**

```bash
sudo systemctl start nginx
sudo systemctl enable nginx
sudo systemctl status nginx
```

然後確保 Security Group 開放 **80 端口**：

```
Type: HTTP
Protocol: TCP
Port Range: 80
Source: 0.0.0.0/0
Description: HTTP
```

測試：
```bash
curl http://18.179.187.129/api/actuator/health
```

---

## 🔍 詳細檢查步驟

### 步驟 1：在 EC2 上測試本機連線

```bash
# 測試 localhost
curl http://localhost:8080/api/actuator/health

# 測試 127.0.0.1
curl http://127.0.0.1:8080/api/actuator/health

# 測試私有 IP（查看私有 IP）
hostname -I
curl http://<PRIVATE_IP>:8080/api/actuator/health
```

**預期結果**：應該返回類似以下內容
```json
{"status":"UP"}
```

如果本機可以連線，問題就是 Security Group。

---

### 步驟 2：檢查應用程式日誌

```bash
# 查看最新日誌
tail -100 /home/ec2-user/logs/app.log

# 搜尋啟動訊息
grep -i "started" /home/ec2-user/logs/app.log

# 搜尋端口訊息
grep -i "8080" /home/ec2-user/logs/app.log

# 搜尋錯誤
grep -i "error" /home/ec2-user/logs/app.log
```

**應該看到：**
```
Started AdminApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

---

### 步驟 3：檢查端口綁定

```bash
# 查看端口監聽狀態
sudo netstat -tulnp | grep 8080

# 或使用 ss
sudo ss -tulnp | grep 8080
```

**預期輸出：**
```
tcp6  0  0 :::8080  :::*  LISTEN  199499/java
```

---

## 📋 AWS Security Group 設定檢查清單

請確認以下 Security Group 規則：

### Inbound Rules（入站規則）

| Type | Protocol | Port Range | Source | Description |
|------|----------|------------|--------|-------------|
| SSH | TCP | 22 | Your IP | SSH access |
| Custom TCP | TCP | 8080 | 0.0.0.0/0 | KUJI Admin API |
| HTTP | TCP | 80 | 0.0.0.0/0 | HTTP (如果使用 Nginx) |

### Outbound Rules（出站規則）

通常預設允許所有出站流量即可。

---

## 🛠️ 快速測試腳本

在 EC2 上執行以下腳本進行診斷：

```bash
#!/bin/bash

echo "======================================"
echo "KUJI Admin 網路診斷"
echo "======================================"
echo ""

echo "1. 檢查 Java 進程..."
ps aux | grep admin-1.0.0.jar | grep -v grep
echo ""

echo "2. 檢查端口監聽..."
sudo netstat -tulnp | grep 8080
echo ""

echo "3. 測試本機連線（localhost）..."
curl -s http://localhost:8080/api/actuator/health
echo ""

echo "4. 測試本機連線（127.0.0.1）..."
curl -s http://127.0.0.1:8080/api/actuator/health
echo ""

echo "5. 檢查私有 IP..."
PRIVATE_IP=$(hostname -I | awk '{print $1}')
echo "私有 IP: $PRIVATE_IP"
curl -s http://$PRIVATE_IP:8080/api/actuator/health
echo ""

echo "6. 檢查最新日誌..."
tail -20 /home/ec2-user/logs/app.log
echo ""

echo "======================================"
echo "診斷完成"
echo "======================================"
```

儲存為 `diagnose.sh` 並執行：

```bash
chmod +x diagnose.sh
./diagnose.sh
```

---

## 🎯 最快速的解決方法

### 如果本機可以連線（curl localhost:8080 成功）

**→ 問題是 Security Group，去 AWS Console 開放 8080 端口**

### 如果本機也無法連線

**→ 檢查應用程式日誌，查看是否有錯誤**

```bash
tail -100 /home/ec2-user/logs/app.log
```

---

## 📞 AWS Console 操作步驟（圖文說明）

### 開放 8080 端口

1. **AWS Console** → **EC2** → **Instances**
2. 選擇您的 Instance（IP: 18.179.187.129）
3. 下方面板 → **Security** 標籤
4. 點選 Security Group 連結（例如：`sg-xxxxxxxxxx`）
5. **Inbound rules** 標籤 → **Edit inbound rules**
6. **Add rule**
   - Type: Custom TCP
   - Port: 8080
   - Source: 0.0.0.0/0
   - Description: KUJI Admin API
7. **Save rules**

### 測試

```bash
# 本機執行
curl http://18.179.187.129:8080/api/actuator/health

# 瀏覽器開啟
http://18.179.187.129:8080/api/actuator/health
```

---

## ✅ 成功標誌

看到以下回應表示成功：

```json
{
  "status": "UP"
}
```

或

```json
{
  "success": true,
  "data": {
    "status": "UP"
  },
  "error": null,
  "meta": {
    "timestamp": "2026-01-14T...",
    "requestId": "..."
  }
}
```

---

## 🔧 其他可能問題

### 問題 1：應用程式綁定到 localhost 而非 0.0.0.0

檢查 `application-prod.yml`：

```yaml
server:
  port: 8080
  address: 0.0.0.0  # 確保綁定到所有網路介面
```

### 問題 2：防火牆阻擋

```bash
# 檢查 firewalld（如果啟用）
sudo systemctl status firewalld

# 如果啟用，開放 8080
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

# 或暫時停用（測試用）
sudo systemctl stop firewalld
```

### 問題 3：SELinux 阻擋

```bash
# 檢查 SELinux 狀態
getenforce

# 暫時停用（測試用）
sudo setenforce 0

# 永久停用（修改 /etc/selinux/config）
```

---

## 📚 相關文件

- **部署指南**: `PRODUCTION_DEPLOYMENT_GUIDE.md`
- **Java 安裝**: `EC2_JAVA_SETUP_GUIDE.md`
- **快速命令**: `EC2_QUICK_DEPLOY_COMMANDS.md`

---

**99% 的情況是 Security Group 未開放 8080 端口！** 🔐

請先在 AWS Console 開放端口，然後測試即可！✨
