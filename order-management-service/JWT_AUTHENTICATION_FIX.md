# Order Management Service - JWT Authentication Fix

## Problem
Getting **401 Unauthorized** when hitting `http://localhost:8020/api/v1/orders?customerId=testuser`

## Root Causes
1. **No JWT Authentication**: The order-management-service lacked JWT token validation
2. **Missing Spring Security**: No security configuration or authentication mechanism
3. **Query Parameter Support**: The endpoint only supported path parameter `/customer/{customerId}`, not query parameter `?customerId=testuser`

## Solution Implemented

### 1. Added Dependencies to pom.xml
- `org.springframework.boot:spring-boot-starter-security`
- `io.jsonwebtoken:jjwt-api:0.11.5`
- `io.jsonwebtoken:jjwt-impl:0.11.5`
- `io.jsonwebtoken:jjwt-jackson:0.11.5`

### 2. Created JwtTokenProvider.java
- Parses JWT tokens using the same secret as auth-service
- Extracts username and role claims
- Validates token signatures and expiration

### 3. Created JwtAuthenticationFilter.java
- Intercepts requests and extracts Bearer token from Authorization header
- Validates token and sets SecurityContext with authorities
- Enables downstream role-based authorization checks

### 4. Created SecurityConfig.java
- Enables Spring Security and method-level security
- Configures stateless session management (JWT)
- Integrates JWT filter before Spring's default authentication filters
- Permits all HTTP requests at URL level

### 5. Updated application.yml
- Added `jwt.secret` configuration to match auth-service

### 6. Updated OrderController.java
- Modified `/api/v1/orders` GET endpoint to support optional `customerId` query parameter
- Falls back to `findAll()` when query parameter is not provided

## Files Modified/Created

| File | Type | Changes |
|------|------|---------|
| `pom.xml` | Modified | Added Spring Security + JWT dependencies |
| `config/SecurityConfig.java` | Created | Spring Security configuration |
| `config/JwtTokenProvider.java` | Created | JWT token parsing utility |
| `config/JwtAuthenticationFilter.java` | Created | JWT authentication filter |
| `resources/application.yml` | Modified | Added jwt.secret config |
| `controller/OrderController.java` | Modified | Added query parameter support |

## How to Use

### Step 1: Get Authentication Token
```bash
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'
```

Response will include a Bearer token.

### Step 2: Use Token to Call Order Endpoint

**Option A - Query Parameter (Fixed)**:
```bash
curl -X GET "http://localhost:8082/api/v1/orders?customerId=testuser" \
  -H "Authorization: Bearer <token>"
```

**Option B - Path Parameter (Already Supported)**:
```bash
curl -X GET "http://localhost:8082/api/v1/orders/customer/testuser" \
  -H "Authorization: Bearer <token>"
```

**Option C - Get All Orders**:
```bash
curl -X GET "http://localhost:8082/api/v1/orders" \
  -H "Authorization: Bearer <token>"
```

## Expected Results

✅ **With Valid Bearer Token**: 
- Status: 200 OK
- Returns list of orders for the customer or all orders

❌ **Without Bearer Token**:
- Status: 401 Unauthorized
- Error message

❌ **With Invalid Token**:
- Status: 401 Unauthorized
- Error message

## Key Features

1. **JWT Validation**: All requests require a valid Bearer token from auth-service
2. **Role Support**: The token includes user role information for future authorization
3. **Query Parameters**: Flexible endpoint supports both query params and path variables
4. **Stateless**: No session management - each request is independently authenticated
5. **Consistent**: Same JWT implementation across all microservices

## Testing the Fix

```bash
# 1. Login to get token
TOKEN=$(curl -s -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}' | jq -r '.token')

# 2. Test query parameter endpoint (previously failing)
curl -X GET "http://localhost:8082/api/v1/orders?customerId=testuser" \
  -H "Authorization: Bearer $TOKEN"

# Should now return 200 OK with order list instead of 401 Unauthorized
```

## Notes

- The JWT secret is shared with auth-service for seamless token validation
- Tokens are valid for 24 hours
- All endpoints require authentication (any valid token works)
- Future authorization rules can be added using `@PreAuthorize` annotation
