# 🔧 新增 Health Check API 完整步驟

## ✅ 已完成的修改

1. **pom.xml** - 新增 Spring Boot Actuator 依賴
2. **application.yml** - 配置 Actuator 端點
3. **SecurityConfig.java** - 允許公開訪問 `/actuator/health`

---

## 🚀 部署步驟

### 步驟 1：在本機編譯（Windows）

```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

mvn clean package -DskipTests
```

等待編譯完成，應該會看到：
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

生成的 JAR 檔案：`target/admin-1.0.0.jar`

---

### 步驟 2：上傳到 EC2

```cmd
scp -i C:\Users\KD\Desktop\onekuji\ourkuji\ourkuji.pem target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/
```

---

### 步驟 3：在 EC2 上重新部署

SSH 到 EC2：
```cmd
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129
```

執行部署命令（一鍵）：
```bash
# 設定變數
APP_DIR="/home/ec2-user/kuji-admin"
LOG_DIR="/home/ec2-user/logs"
JAR_NAME="admin-1.0.0.jar"

# 停止舊服務
PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
[ -n "$PID" ] && kill -15 $PID && echo "停止舊服務 PID: $PID" && sleep 5

# 備份舊版本
[ -f "$APP_DIR/$JAR_NAME" ] && mv "$APP_DIR/$JAR_NAME" "$APP_DIR/$JAR_NAME.backup.$(date +%Y%m%d_%H%M%S)"

# 部署新版本
cp $JAR_NAME $APP_DIR/
echo "✅ 部署完成"

# 啟動服務
cd $APP_DIR
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    -XX:+UseG1GC \
    $JAR_NAME \
    > $LOG_DIR/app.log 2>&1 &

echo "🚀 服務已啟動，PID: $!"
sleep 10
echo "📋 最近日誌："
tail -50 $LOG_DIR/app.log
```

---

### 步驟 4：驗證部署

#### 4.1 檢查服務狀態

```bash
# 檢查進程
ps aux | grep admin-1.0.0.jar | grep -v grep

# 查看日誌
tail -50 /home/ec2-user/logs/app.log
```

應該看到：
```
Started AdminApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

#### 4.2 測試 Health Check API

**在 EC2 上測試（本機）：**
```bash
curl http://localhost:8080/api/actuator/health
```

**在您的電腦上測試（外部）：**
```bash
# 需要先開放 Security Group 的 8080 端口
curl http://18.179.187.129:8080/api/actuator/health
```

**預期回應：**
```json
{
  "status": "UP"
}
```

或者（如果 AOP 包裝了）：
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

## 🌐 可用的 Actuator 端點

您現在可以訪問以下端點：

| 端點 | URL | 說明 |
|------|-----|------|
| Health Check | `http://18.179.187.129:8080/api/actuator/health` | 服務健康狀態 |
| Info | `http://18.179.187.129:8080/api/actuator/info` | 應用程式資訊 |

---

## 🔍 測試其他 API

### 1. 登入 API

```bash
curl -X POST http://18.179.187.129:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@kuji.com",
    "password": "admin123"
  }'
```

### 2. 在瀏覽器測試

直接在瀏覽器開啟：
```
http://18.179.187.129:8080/api/actuator/health
```

---

## 🆘 遇到問題？

### 問題 1：編譯失敗

```bash
# 清理 Maven 快取
mvn clean

# 重新下載依賴
mvn dependency:resolve

# 再次編譯
mvn clean package -DskipTests
```

### 問題 2：無法訪問（外部）

**→ 需要開放 EC2 Security Group 的 8080 端口**

1. 登入 AWS Console
2. EC2 → Instances → 選擇您的 Instance
3. Security → Security Groups
4. Inbound rules → Edit inbound rules
5. Add rule:
   - Type: Custom TCP
   - Port: 8080
   - Source: 0.0.0.0/0
   - Description: KUJI Admin API
6. Save rules

### 問題 3：本機可以訪問，外部不行

```bash
# 在 EC2 上測試
curl http://localhost:8080/api/actuator/health

# 如果成功，問題就是 Security Group
```

### 問題 4：404 Not Found

檢查日誌是否有錯誤：
```bash
tail -100 /home/ec2-user/logs/app.log | grep -i error
```

---

## 📊 Health Check 詳細資訊（可選）

如果您想看到更詳細的健康資訊，可以修改 `application.yml`：

```yaml
management:
  endpoint:
    health:
      show-details: always  # 改為 always
```

這樣會顯示：
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 10737418240,
        "free": 5368709120,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## ✅ 完成檢查清單

- [ ] 本機編譯成功（`mvn clean package -DskipTests`）
- [ ] JAR 上傳到 EC2
- [ ] EC2 上舊服務已停止
- [ ] 新服務已啟動
- [ ] 日誌顯示 "Started AdminApplication"
- [ ] 本機測試成功（`curl http://localhost:8080/api/actuator/health`）
- [ ] 外部測試成功（開放 Security Group 後）

---

## 🎯 下一步

健康檢查 API 啟用後，您可以：

1. ✅ 使用它來監控服務狀態
2. ✅ 配置負載均衡器的健康檢查
3. ✅ 設定 CloudWatch 監控
4. ✅ 自動化部署腳本中的健康檢查

---

*Health Check API 現在已經可以使用！* 🎉

**測試 URL**: `http://18.179.187.129:8080/api/actuator/health`
