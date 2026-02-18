@echo off
REM Test script for JWT Authentication Fix
REM Make sure all services are running before executing this script

echo.
echo ================================================
echo JWT Authentication Fix - Test Script
echo ================================================
echo.

REM Colors for output
setlocal enabledelayedexpansion

REM Test 1: Login as ADMIN
echo.
echo [TEST 1] Getting ADMIN token...
echo ---
for /f "tokens=*" %%A in ('curl -s -X POST http://localhost:8083/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}" ^
  ^| jq -r ".token"') do (
  set ADMIN_TOKEN=%%A
)

if "%ADMIN_TOKEN%"=="" (
  echo ERROR: Could not get ADMIN token
  goto ERROR
)
echo SUCCESS: Got ADMIN token
echo Token: %ADMIN_TOKEN%

REM Test 2: Login as USER
echo.
echo [TEST 2] Getting USER token...
echo ---
for /f "tokens=*" %%A in ('curl -s -X POST http://localhost:8083/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"user\",\"password\":\"user123\"}" ^
  ^| jq -r ".token"') do (
  set USER_TOKEN=%%A
)

if "%USER_TOKEN%"=="" (
  echo ERROR: Could not get USER token
  goto ERROR
)
echo SUCCESS: Got USER token

REM Test 3: Get products without token (should work - public)
echo.
echo [TEST 3] Get products without token (public endpoint)...
echo ---
curl -X GET http://localhost:8081/api/v1/products
echo.
echo Expected: 200 OK (no authentication needed)

REM Test 4: Get orders without token (should fail - requires auth)
echo.
echo [TEST 4] Get orders without token (requires authentication)...
echo ---
curl -X GET "http://localhost:8082/api/v1/orders?customerId=testuser"
echo.
echo Expected: 401 Unauthorized

REM Test 5: Get orders with USER token (should work)
echo.
echo [TEST 5] Get orders with USER token...
echo ---
curl -X GET "http://localhost:8082/api/v1/orders?customerId=testuser" ^
  -H "Authorization: Bearer %USER_TOKEN%"
echo.
echo Expected: 200 OK

REM Test 6: Create product with ADMIN token (should work)
echo.
echo [TEST 6] Create product with ADMIN token...
echo ---
curl -X POST http://localhost:8081/api/v1/products ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test Product\",\"sku\":\"TEST123\",\"price\":99.99,\"stockQuantity\":100,\"description\":\"Test\"}"
echo.
echo Expected: 201 Created

REM Test 7: Create product with USER token (should fail - ADMIN only)
echo.
echo [TEST 7] Create product with USER token (should fail - requires ADMIN)...
echo ---
curl -X POST http://localhost:8081/api/v1/products ^
  -H "Authorization: Bearer %USER_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test Product\",\"sku\":\"TEST456\",\"price\":99.99,\"stockQuantity\":100,\"description\":\"Test\"}"
echo.
echo Expected: 403 Forbidden

REM Test 8: Get orders with ADMIN token (should work)
echo.
echo [TEST 8] Get orders with ADMIN token...
echo ---
curl -X GET "http://localhost:8082/api/v1/orders?customerId=testuser" ^
  -H "Authorization: Bearer %ADMIN_TOKEN%"
echo.
echo Expected: 200 OK

echo.
echo ================================================
echo All tests completed!
echo ================================================
echo.
goto END

:ERROR
echo.
echo ERROR: Tests failed
echo.
exit /b 1

:END
exit /b 0
