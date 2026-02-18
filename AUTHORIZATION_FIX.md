# Authorization Fix Summary

## Problem
When accessing `http://localhost:8020/api/v1/orders?customerId=testuser` with an ADMIN bearer token, the request was returning 401 Unauthorized instead of allowing it through.

## Root Cause
The order-management-service was not validating JWT tokens independently. It was relying on the API Gateway to do all the validation and pass the X-Auth-User and X-Auth-Role headers. However:

1. The gateway had logic to block USER role from listing orders by customer
2. But the service itself had no way to enforce authorization rules
3. The endpoints were not protected with Spring Security, so any request could theoretically reach them

## Solution Implemented

### 1. Added Spring Security Dependencies
Updated `pom.xml` to include:
- `spring-boot-starter-security` - For Spring Security framework
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - For JWT token parsing and validation

### 2. Created JWT Utilities
- **JwtUtil.java** - Utility class to parse and validate JWT tokens, extract username and role claims
- **JwtAuthenticationFilter.java** - Servlet filter that:
  - Extracts the Bearer token from Authorization header
  - Validates the token using JwtUtil
  - Extracts username and role
  - Creates a Spring Security Authentication with granted authorities (ROLE_ADMIN or ROLE_USER)
  - Sets this in SecurityContext for method-level security

### 3. Created Security Configuration
- **SecurityConfig.java** - Configures Spring Security to:
  - Disable CSRF (not needed for stateless REST APIs)
  - Use STATELESS session policy (JWT tokens, no sessions)
  - Require authentication for all requests
  - Add the JwtAuthenticationFilter before the UsernamePasswordAuthenticationFilter
  - Enable method-level security with @PreAuthorize annotations

### 4. Added Authorization Rules to OrderController
Added `@PreAuthorize` annotations:
- `@PreAuthorize("hasRole('ADMIN')")` on:
  - `POST /api/v1/orders` (create order)
  - `GET /api/v1/orders?customerId=...` (list orders - requires ADMIN)
  - `PATCH /api/v1/orders/{id}/status/{status}` (update status)

- Endpoints without restrictions allow both ADMIN and USER:
  - `GET /api/v1/orders/{id}` (get specific order)
  - `GET /api/v1/orders/number/{orderNumber}` (get by order number)
  - `GET /api/v1/orders/customer/{customerId}` (get customer orders - alternative path)
  - `POST /api/v1/orders/{id}/cancel` (cancel order)

### 5. Updated Configuration
- Added `jwt.secret` property to `application.yml` in order-management-service
- Enabled `@EnableMethodSecurity` on the main application class

## How It Works Now

1. **ADMIN User (credentials: admin/admin123)**
   - Logs in through `/api/v1/auth/login`
   - Receives JWT token with role="ADMIN"
   - Uses token as Bearer token in Authorization header
   - Calls `GET /api/v1/orders?customerId=testuser`
   - API Gateway validates token, passes request to order-management-service
   - Order Management Service validates token again, extracts role
   - @PreAuthorize checks if role is ADMIN ✅
   - Request proceeds, returns order list

2. **USER User (credentials: user/user123)**
   - Logs in and receives JWT token with role="USER"
   - Tries to call `GET /api/v1/orders?customerId=testuser`
   - API Gateway: Blocks request with 403 FORBIDDEN (gateway-level check)
   - OR if gateway check is bypassed, Order Service: Blocks request with 403 FORBIDDEN (@PreAuthorize check)

## Files Modified/Created

### Created Files:
- `order-management-service/src/main/java/com/example/order/config/SecurityConfig.java`
- `order-management-service/src/main/java/com/example/order/security/JwtUtil.java`
- `order-management-service/src/main/java/com/example/order/security/JwtAuthenticationFilter.java`

### Modified Files:
- `order-management-service/pom.xml` - Added security dependencies
- `order-management-service/src/main/resources/application.yml` - Added jwt.secret property
- `order-management-service/src/main/java/com/example/order/OrderManagementApplication.java` - Added @EnableMethodSecurity
- `order-management-service/src/main/java/com/example/order/controller/OrderController.java` - Added @PreAuthorize annotations

## Testing the Fix

```bash
# 1. Get ADMIN token
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Response: {"token":"eyJ...", "username":"admin", "role":"ADMIN"}

# 2. Use ADMIN token to list orders by customer (should succeed)
curl http://localhost:8020/api/v1/orders?customerId=testuser \
  -H "Authorization: Bearer eyJ..."

# Response: [order list]

# 3. Get USER token
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# 4. Use USER token to list orders by customer (should be forbidden)
curl http://localhost:8020/api/v1/orders?customerId=testuser \
  -H "Authorization: Bearer eyJ..."

# Response: 403 Forbidden
```

## Security Benefits

1. **Defense in Depth** - Authorization is checked at both API Gateway and Service level
2. **Service Independence** - Order service can be called directly without going through the gateway and still enforce authorization
3. **Spring Security Integration** - Uses standard Spring Security patterns for maintainability
4. **Role-Based Access Control** - Clear distinction between ADMIN and USER permissions
