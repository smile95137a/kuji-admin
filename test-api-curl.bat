@echo off
echo Testing API...
curl -X POST http://localhost:8080/api/admin/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@kuji.com\",\"password\":\"admin123\"}" -o login_result.json
echo.
echo Login result:
type login_result.json
