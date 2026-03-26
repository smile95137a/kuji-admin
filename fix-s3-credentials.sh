#!/bin/bash
# EC2 S3 憑證快速修復腳本

echo "=========================================="
echo " KUJI Admin S3 憑證設定"
echo "=========================================="
echo ""

# 檢查是否有 IAM Role
echo "🔍 檢查 EC2 IAM Role..."
IAM_ROLE=$(curl -s http://169.254.169.254/latest/meta-data/iam/security-credentials/)

if [ -n "$IAM_ROLE" ]; then
    echo "✅ 檢測到 IAM Role: $IAM_ROLE"
    echo "   建議：繼續使用 IAM Role（最安全）"
    echo ""
    echo "請確認 IAM Role 有以下權限："
    echo "  - s3:PutObject"
    echo "  - s3:GetObject"
    echo "  - s3:DeleteObject"
    echo "  - s3:ListBucket"
    echo ""
else
    echo "⚠️  未檢測到 IAM Role"
    echo "   建議：在 AWS Console 為 EC2 附加 IAM Role"
    echo ""
fi

# 檢查環境變數
echo "🔍 檢查環境變數..."
if [ -n "$AWS_ACCESS_KEY_ID" ]; then
    echo "✅ AWS_ACCESS_KEY_ID 已設定"
else
    echo "❌ AWS_ACCESS_KEY_ID 未設定"
fi

if [ -n "$AWS_SECRET_ACCESS_KEY" ]; then
    echo "✅ AWS_SECRET_ACCESS_KEY 已設定"
else
    echo "❌ AWS_SECRET_ACCESS_KEY 未設定"
fi

echo ""

# 選擇修復方案
echo "=========================================="
echo " 選擇修復方案"
echo "=========================================="
echo "1. 設定環境變數（快速修復）"
echo "2. 檢視 systemd service 設定指南"
echo "3. 檢視 IAM Role 設定指南"
echo "4. 測試 S3 連線"
echo "5. 重啟服務"
echo "6. 退出"
echo ""
read -p "請選擇 (1-6): " choice

case $choice in
    1)
        echo ""
        echo "請輸入 AWS 憑證："
        read -p "AWS_ACCESS_KEY_ID: " access_key
        read -sp "AWS_SECRET_ACCESS_KEY: " secret_key
        echo ""
        
        # 寫入 /etc/environment
        echo "正在寫入環境變數..."
        sudo bash -c "cat >> /etc/environment << EOF
AWS_ACCESS_KEY_ID=\"$access_key\"
AWS_SECRET_ACCESS_KEY=\"$secret_key\"
EOF"
        
        # 寫入 systemd service
        SERVICE_FILE="/etc/systemd/system/kuji-admin.service"
        if [ -f "$SERVICE_FILE" ]; then
            echo "正在更新 systemd service..."
            sudo sed -i '/\[Service\]/a Environment="AWS_ACCESS_KEY_ID='$access_key'"\nEnvironment="AWS_SECRET_ACCESS_KEY='$secret_key'"' $SERVICE_FILE
            sudo systemctl daemon-reload
            echo "✅ 環境變數已設定"
            echo ""
            read -p "是否立即重啟服務？(y/n): " restart
            if [ "$restart" = "y" ]; then
                sudo systemctl restart kuji-admin
                echo "✅ 服務已重啟"
                sudo systemctl status kuji-admin
            fi
        else
            echo "⚠️  找不到 systemd service 檔案"
        fi
        ;;
    2)
        echo ""
        echo "=========================================="
        echo " systemd Service 設定指南"
        echo "=========================================="
        echo ""
        echo "1. 編輯 service 檔案："
        echo "   sudo nano /etc/systemd/system/kuji-admin.service"
        echo ""
        echo "2. 在 [Service] 區段加入："
        echo "   Environment=\"AWS_ACCESS_KEY_ID=你的Key\""
        echo "   Environment=\"AWS_SECRET_ACCESS_KEY=你的Secret\""
        echo ""
        echo "3. 重新載入並重啟："
        echo "   sudo systemctl daemon-reload"
        echo "   sudo systemctl restart kuji-admin"
        echo ""
        ;;
    3)
        echo ""
        echo "=========================================="
        echo " IAM Role 設定指南"
        echo "=========================================="
        echo ""
        echo "1. 建立 IAM Policy (JSON):"
        cat << 'EOF'
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::test-ourkuji",
                "arn:aws:s3:::test-ourkuji/*"
            ]
        }
    ]
}
EOF
        echo ""
        echo "2. 建立 IAM Role"
        echo "   - Trusted entity: AWS service → EC2"
        echo "   - 附加上述 Policy"
        echo ""
        echo "3. 附加 Role 到 EC2"
        echo "   - EC2 Console → Actions → Security → Modify IAM role"
        echo ""
        echo "4. 重啟應用程式"
        echo "   sudo systemctl restart kuji-admin"
        echo ""
        ;;
    4)
        echo ""
        echo "🧪 測試 S3 連線..."
        if command -v aws &> /dev/null; then
            aws s3 ls s3://test-ourkuji --region ap-northeast-1
            if [ $? -eq 0 ]; then
                echo "✅ S3 連線成功"
            else
                echo "❌ S3 連線失敗"
            fi
        else
            echo "⚠️  AWS CLI 未安裝"
            echo "   安裝指令: sudo apt install awscli -y"
        fi
        ;;
    5)
        echo ""
        echo "🔄 重啟服務..."
        sudo systemctl restart kuji-admin
        echo "✅ 服務已重啟"
        echo ""
        sudo systemctl status kuji-admin
        ;;
    6)
        echo "退出"
        exit 0
        ;;
    *)
        echo "無效的選擇"
        ;;
esac

echo ""
echo "=========================================="
echo " 完成！"
echo "=========================================="
echo ""
echo "後續步驟："
echo "1. 檢查應用程式日誌："
echo "   sudo journalctl -u kuji-admin -f"
echo ""
echo "2. 測試圖片上傳功能"
echo ""
echo "3. 如果還有問題，請查看詳細指南："
echo "   cat EC2_S3_CREDENTIALS_FIX.md"
echo ""
