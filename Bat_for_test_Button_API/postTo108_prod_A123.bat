@echo off
setlocal

REM 設定 JSON payload
set payload={"userId":"A123456789"}

curl -X POST http://localhost:8080/NPA114-TRoad/prod/post-to-108 ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"A123456789\"}" ^
  --output mydata-output.zip
  
echo.
echo ✅ 測試完成，輸出檔案為 mydata-output.zip
pause
