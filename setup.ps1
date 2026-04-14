# 執行 Referral Signup 數據庫遷移和檢查 Lombok 編譯

param(
    [string]$Action = "help"
)

function Show-Help {
    Write-Host @"
使用: .\setup.ps1 -Action [action]

可用操作:
  migrations    - 執行數據庫遷移 (需要 MySQL 客戶端)
  init-db       - 初始化數據庫 (使用 MySQL 配置文件)
  compile       - 清理並編譯項目 (mvn clean compile)
  check-mysql   - 檢查 MySQL 是否在 PATH 中
  help          - 顯示此幫助信息

例如:
  .\setup.ps1 -Action migrations
  .\setup.ps1 -Action compile
"@
}

function Check-MySQL {
    Write-Host "🔍 檢查 MySQL 客戶端..."
    
    # 方法 1: 查找 PATH 中的 mysql
    $mysqlFromPath = where.exe mysql 2>$null
    if ($mysqlFromPath) {
        Write-Host "✅ 找到 MySQL (PATH): $mysqlFromPath"
        return $mysqlFromPath
    }
    
    # 方法 2: 查找常見安装位置
    $commonPaths = @(
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\MySQL\bin\mysql.exe"
    )
    
    foreach ($path in $commonPaths) {
        if (Test-Path $path) {
            Write-Host "✅ 找到 MySQL (已知位置): $path"
            return $path
        }
    }
    
    Write-Host "❌ 未找到 MySQL 客戶端"
    return $null
}

function Run-Migration {
    Write-Host "🚀 開始執行 Referral Signup 遷移..."
    
    $sqlFile = "sql\V_2026_04_14__add_referral_signup_integration.sql"
    
    if (-not (Test-Path $sqlFile)) {
        Write-Host "❌ SQL 檔案不存在: $sqlFile"
        return $false
    }
    
    Write-Host "📄 SQL 檔案: $((Resolve-Path $sqlFile).Path)"
    
    # 檢查 MySQL
    $mysqlPath = Check-MySQL
    if (-not $mysqlPath) {
        Write-Host ""
        Write-Host "⚠️  無法找到 MySQL 客戶端。請嘗試以下方法:"
        Write-Host "   1. 安装 MySQL Server (包含客戶端)"
        Write-Host "   2. 將 MySQL 安装目錄的 bin 資料夾加入 PATH"
        Write-Host "   3. 手動在 MySQL Workbench 或 HeidiSQL 中執行 SQL 檔案"
        Write-Host ""
        Write-Host "📝 SQL 檔案已準備好，位置: $(Resolve-Path $sqlFile)"
        return $false
    }
    
    Write-Host ""
    Write-Host "📌 數據庫配置:"
    Write-Host "   主機: 127.0.0.1:3306"
    Write-Host "   用戶: root"
    Write-Host "   數據庫: kuji"
    Write-Host ""
    
    try {
        Write-Host "⏳ 執行 SQL 遷移..."
        $sqlContent = Get-Content $sqlFile -Raw
        
        # 透過管道執行 SQL
        $sqlContent | & $mysqlPath -h 127.0.0.1 -u root -proot kuji
        
        Write-Host "✅ Referral Signup 遷移完成"
        
        # 驗證
        Write-Host ""
        Write-Host "驗證結果:"
        Write-Host "SELECT COUNT(*) as 'referral_code 欄位數' FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'user' AND COLUMN_NAME = 'referral_code' AND TABLE_SCHEMA = 'kuji';" | & $mysqlPath -h 127.0.0.1 -u root -proot kuji
        
        return $true
    }
    catch {
        Write-Host "❌ 遷移失敗: $_"
        return $false
    }
}

function Clean-Compile {
    Write-Host "🧹 清理並編譯項目..."
    Write-Host "⏳ 這可能需要幾分鐘..."
    
    & mvn clean compile -q
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 編譯成功"
        return $true
    }
    else {
        Write-Host "❌ 編譯失敗，請檢查錯誤信息"
        & mvn clean compile  # 再執行一次以顯示完整錯誤
        return $false
    }
}

# 執行操作
switch ($Action) {
    "migrations" { Run-Migration }
    "check-mysql" { Check-MySQL | Out-Null }
    "compile" { Clean-Compile | Out-Null }
    "help" { Show-Help }
    default { 
        Write-Host "❌ 未知操作: $Action"
        Write-Host ""
        Show-Help
    }
}
