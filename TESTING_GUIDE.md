# Quick Testing Guide for Authorization Fix

## Prerequisites
- All microservices should be running (Service Registry, Auth Service, Order Management Service, API Gateway)
- Database should be running (PostgreSQL)

## Test Cases

### Test 1: ADMIN User - Successfully List Orders by Customer ID
```bash
# Step 1: Login as admin
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Expected Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "username": "admin",
#   "role": "ADMIN"
# }

# Step 2: Copy the token and use it to list orders
curl http://localhost:8020/api/v1/orders?customerId=testuser \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Expected Response: 200 OK
# Returns list of orders (or empty array if no orders exist)
```

### Test 2: USER User - Should Get Forbidden (403) When Listing by Customer ID
```bash
# Step 1: Login as user
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# Expected Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "username": "user",
#   "role": "USER"
# }

# Step 2: Try to list orders by customer ID
curl http://localhost:8020/api/v1/orders?customerId=testuser \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." -v

# Expected Response: 403 FORBIDDEN
# Message: Access Denied (or similar 403 response from Spring Security)
```

### Test 3: ADMIN User - Successfully Create Order (Admin-only operation)
```bash
# Get ADMIN token
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Create order with admin token
curl -X POST http://localhost:8020/api/v1/orders \
  -H "Authorization: Bearer [ADMIN_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "testuser",
    "productIds": [1, 2],
    "totalAmount": 100.00
  }'

# Expected Response: 201 CREATED
# Returns created order with ID
```

### Test 4: USER User - Should Get Forbidden (403) When Creating Order
```bash
# Get USER token
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# Try to create order with user token
curl -X POST http://localhost:8020/api/v1/orders \
  -H "Authorization: Bearer [USER_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "testuser",
    "productIds": [1, 2],
    "totalAmount": 100.00
  }'

# Expected Response: 403 FORBIDDEN
```

### Test 5: Both Roles Can Get Specific Order by ID
```bash
# Get token (either admin or user)
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# Get order by ID (should work for both ADMIN and USER)
curl http://localhost:8020/api/v1/orders/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Expected Response: 200 OK (or 404 if order doesn't exist)
# Returns order details
```

### Test 6: Missing Authorization Header
```bash
# Try to access protected endpoint without token
curl http://localhost:8020/api/v1/orders?customerId=testuser

# Expected Response: 401 UNAUTHORIZED
```

### Test 7: Invalid Token Format
```bash
# Try with invalid Bearer format
curl http://localhost:8020/api/v1/orders?customerId=testuser \
  -H "Authorization: InvalidToken123"

# Expected Response: 401 UNAUTHORIZED
```

### Test 8: Expired or Invalid Token
```bash
# Try with a malformed JWT token
curl http://localhost:8020/api/v1/orders?customerId=testuser \
  -H "Authorization: Bearer invalid.token.here"

# Expected Response: 401 UNAUTHORIZED
```

## Using Postman

1. **Get ADMIN Token**
   - Method: POST
   - URL: `http://localhost:8020/api/v1/auth/login`
   - Body (raw JSON):
     ```json
     {
       "username": "admin",
       "password": "admin123"
     }
     ```
   - Send and copy the token value

2. **List Orders with ADMIN Token**
   - Method: GET
   - URL: `http://localhost:8020/api/v1/orders?customerId=testuser`
   - Headers:
     - Key: `Authorization`
     - Value: `Bearer [paste_token_here]`
   - Send
   - Expected: 200 OK with order list

3. **List Orders with USER Token**
   - Repeat the same steps but with user credentials
   - Expected: 403 Forbidden

## Logs to Check

When testing, check the logs of the order-management-service for:
- JWT token validation logs
- Security filter logs
- Authorization check logs
- Any security-related exceptions

Look for entries like:
```
JWT validation successful
Role extracted: ADMIN
@PreAuthorize check passed for method: listOrders
```

## Common Issues

### Issue: "JWT validation failed"
- **Cause**: JWT secret in order-management-service doesn't match auth-service
- **Fix**: Ensure both services have the same JWT_SECRET environment variable

### Issue: "Access Denied" for ADMIN user
- **Cause**: Spring Security not properly configured or role name mismatch
- **Fix**: Check that role claim in JWT is exactly "ADMIN" (case-sensitive)

### Issue: Token not being extracted from header
- **Cause**: Authorization header format incorrect
- **Fix**: Use exactly `Bearer [token]` format (case-sensitive "Bearer")

### Issue: 401 instead of 403 for USER
- **Cause**: JWT filter not properly setting authentication in SecurityContext
- **Fix**: Check JwtAuthenticationFilter is being applied before authorization check
