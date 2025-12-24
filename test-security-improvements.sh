#!/bin/bash

echo "========================================"
echo "  API 安全性改進測試腳本"
echo "========================================"
echo ""

BASE_URL="http://localhost:8080"

echo "[1/5] 測試登入 API..."
echo ""
LOGIN_RESPONSE=$(curl -s -X POST ${BASE_URL}/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"Admin123"}')

echo "$LOGIN_RESPONSE" | jq '.' > login_response.json
echo "登入回應已儲存到 login_response.json"
echo ""

echo "[2/5] 從回應中提取 Token..."
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')
echo "Token: ${TOKEN:0:50}..."
echo ""

echo "[3/5] 測試選單 API (新版 - 不需要 userId)..."
echo ""
MENU_RESPONSE=$(curl -s -X GET ${BASE_URL}/admin/menus/accessible \
  -H "Authorization: Bearer $TOKEN")
echo "$MENU_RESPONSE" | jq '.' > menu_response.json
echo "✅ 選單回應已儲存到 menu_response.json"
echo ""

echo "[4/5] 測試權限檢查 API..."
echo ""
PERMISSION_RESPONSE=$(curl -s -X GET "${BASE_URL}/admin/permissions/check/PRODUCT_MANAGEMENT" \
  -H "Authorization: Bearer $TOKEN")
echo "$PERMISSION_RESPONSE" | jq '.' > permission_response.json
echo "✅ 權限檢查回應已儲存到 permission_response.json"
echo ""

echo "[5/5] 測試是否為 Admin..."
echo ""
ADMIN_CHECK=$(curl -s -X GET ${BASE_URL}/admin/permissions/is-admin \
  -H "Authorization: Bearer $TOKEN")
echo "$ADMIN_CHECK" | jq '.' > admin_check_response.json
echo "✅ Admin 檢查回應已儲存到 admin_check_response.json"
echo ""

echo "========================================"
echo "  測試完成！"
echo "========================================"
echo ""
echo "回應檔案："
echo "  - login_response.json"
echo "  - menu_response.json"
echo "  - permission_response.json"
echo "  - admin_check_response.json"
echo ""
echo "查看選單結果："
cat menu_response.json | jq '.data[].name'
echo ""
