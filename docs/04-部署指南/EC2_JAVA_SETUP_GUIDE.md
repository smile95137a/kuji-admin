# 🔧 EC2 Java 環境設置指南

## ❌ 問題診斷

錯誤訊息：
```
nohup: failed to run command 'java': No such file or directory
```

**原因**：EC2 伺服器上沒有安裝 Java Runtime。

---

## ✅ 解決方案：在 EC2 上安裝 Java 21

### 步驟 1：檢查當前 Java 版本

```bash
java -version
```

如果顯示 "command not found"，表示沒有安裝 Java。

---

### 步驟 2：安裝 Amazon Corretto 21（推薦）

Amazon Corretto 是 AWS 提供的免費、多平台、生產就緒的 OpenJDK 發行版。

```bash
# 下載 Amazon Corretto 21
sudo rpm --import https://yum.corretto.aws/corretto.key

# 添加 Corretto 倉庫
sudo curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo

# 安裝 Java 21
sudo yum install -y java-21-amazon-corretto-devel

# 驗證安裝
java -version
```

預期輸出：
```
openjdk version "21.0.x" 2024-xx-xx LTS
OpenJDK Runtime Environment Corretto-21.0.x.x (build 21.0.x+x-LTS)
OpenJDK 64-Bit Server VM Corretto-21.0.x.x (build 21.0.x+x-LTS, mixed mode, sharing)
```

---

### 步驟 3：設置 JAVA_HOME（可選但推薦）

```bash
# 找到 Java 安裝路徑
sudo alternatives --config java

# 通常是 /usr/lib/jvm/java-21-amazon-corretto.x86_64/bin/java
# 編輯環境變數
sudo nano /etc/profile.d/java.sh
```

在 `java.sh` 中添加：

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64/bin/java
export PATH=$JAVA_HOME/bin:$PATH
```

使環境變數生效：

```bash
source /etc/profile.d/java.sh

# 驗證
echo $JAVA_HOME
```

---

## 🚀 完整部署流程（安裝 Java 後）

### 一鍵部署命令

安裝 Java 後，執行以下完整部署命令：

```bash
# 設定變數
APP_DIR="/home/ec2-user/kuji-admin"
LOG_DIR="/home/ec2-user/logs"
JAR_NAME="admin-1.0.0.jar"

# 建立目錄
mkdir -p $APP_DIR $LOG_DIR

# 停止舊服務（如果有）
PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "停止舊服務 PID: $PID"
    kill -15 $PID
    sleep 5
    # 強制終止（如果還在運行）
    PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
    if [ -n "$PID" ]; then
        kill -9 $PID
    fi
fi

# 備份舊版本
if [ -f "$APP_DIR/$JAR_NAME" ]; then
    BACKUP_NAME="$JAR_NAME.backup.$(date +%Y%m%d_%H%M%S)"
    mv "$APP_DIR/$JAR_NAME" "$APP_DIR/$BACKUP_NAME"
    echo "已備份: $BACKUP_NAME"
fi

# 部署新版本
cp $JAR_NAME $APP_DIR/
echo "✅ 部署完成！"

# 啟動服務
cd $APP_DIR
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    $JAR_NAME \
    > $LOG_DIR/app.log 2>&1 &

NEW_PID=$!
echo "🚀 服務已啟動，PID: $NEW_PID"

# 等待 30 秒檢查狀態
echo "⏳ 等待 30 秒檢查服務狀態..."
sleep 30

# 檢查服務
if ps -p $NEW_PID > /dev/null; then
    echo "✅ 服務運行正常！"
    echo ""
    echo "📋 最近日誌："
    tail -50 $LOG_DIR/app.log
else
    echo "❌ 服務啟動失敗，請檢查日誌："
    tail -100 $LOG_DIR/app.log
    exit 1
fi
```

---

## 🔍 驗證部署

### 1. 檢查 Java 版本

```bash
java -version
```

### 2. 檢查服務狀態

```bash
# 檢查進程
ps aux | grep admin-1.0.0.jar

# 檢查端口
netstat -tuln | grep 8080

# 或使用 ss
ss -tuln | grep 8080
```

### 3. 查看日誌

```bash
# 即時日誌
tail -f /home/ec2-user/logs/app.log

# 最近 100 行
tail -100 /home/ec2-user/logs/app.log

# 搜尋錯誤
grep -i "error" /home/ec2-user/logs/app.log
```

### 4. 測試 API

```bash
# 本機測試（在 EC2 上）
curl http://localhost:8080/api/actuator/health

# 外部測試（在本機執行）
curl http://18.179.187.129:8080/api/actuator/health

# 登入測試
curl -X POST http://18.179.187.129:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

---

## 🛠️ 其他安裝方式（替代方案）

### 方案 B：使用 OpenJDK 21

```bash
# 下載 OpenJDK 21
sudo yum install -y wget

cd /tmp
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz

# 解壓
sudo tar -xzf jdk-21_linux-x64_bin.tar.gz -C /usr/lib/jvm/

# 設置替代方案
sudo alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-21/bin/java 1
sudo alternatives --config java

# 驗證
java -version
```

### 方案 C：使用 SDKMAN（開發環境推薦）

```bash
# 安裝 SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 安裝 Java 21
sdk install java 21.0.1-open

# 驗證
java -version
```

---

## 📋 部署檢查清單

完整部署前，請確認：

- [ ] ✅ Java 21 已安裝並可執行
- [ ] ✅ `java -version` 顯示版本 21
- [ ] ✅ JAR 檔案在 `/home/ec2-user/` 目錄
- [ ] ✅ 目錄結構已建立（kuji-admin, logs）
- [ ] ✅ EC2 安全群組開放 8080 端口
- [ ] ✅ RDS 安全群組允許 EC2 存取
- [ ] ✅ S3 Bucket 已設定公開讀取權限
- [ ] ✅ IAM Role 已附加到 EC2（或設定 AWS Credentials）

---

## 🚨 常見問題

### Q1: 安裝後仍然找不到 Java？

```bash
# 檢查 Java 安裝位置
which java

# 手動添加到 PATH
export PATH=/usr/lib/jvm/java-21-amazon-corretto/bin:$PATH

# 永久添加
echo 'export PATH=/usr/lib/jvm/java-21-amazon-corretto/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

### Q2: 服務啟動但無法連線？

```bash
# 檢查防火牆（如果有）
sudo firewall-cmd --list-all

# 檢查 SELinux（如果啟用）
sudo setenforce 0
```

### Q3: 記憶體不足？

```bash
# 檢查記憶體
free -m

# 減少 JVM 記憶體使用
# 修改啟動命令：-Xms256m -Xmx1024m
```

### Q4: 如何停止服務？

```bash
# 找到 PID
ps aux | grep admin-1.0.0.jar

# 優雅停止
kill -15 <PID>

# 強制停止
kill -9 <PID>
```

---

## 📚 相關文件

- **完整部署指南**: `PRODUCTION_DEPLOYMENT_GUIDE.md`
- **快速修正指南**: `DEPLOY_FIX_INSTRUCTIONS.md`
- **S3 設定**: `AWS_S3_SETUP_GUIDE.md`
- **快速開始**: `QUICK_START_PRODUCTION.md`

---

## 🎯 下一步

安裝 Java 並部署成功後：

1. ✅ 測試 API 端點
2. ✅ 測試資料庫連線
3. ✅ 測試 S3 上傳
4. ✅ 設定 Systemd 服務（開機自動啟動）
5. ✅ 設定日誌輪替
6. ✅ 設定資料庫自動備份

---

*最後更新：2026-01-14*  
*適用於 Amazon Linux 2 / Amazon Linux 2023*
