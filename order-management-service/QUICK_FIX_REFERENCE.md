# Quick Reference - Fix for 401 Unauthorized Error

## Problem Fixed ✅
Resolved **401 Unauthorized** error when hitting `http://localhost:8020/api/v1/orders?customerId=testuser`

## What Was Done

### Added JWT Authentication
1. ✅ Added Spring Security + JWT dependencies
2. ✅ Created JWT token provider (parses tokens)
3. ✅ Created JWT authentication filter (validates tokens)
4. ✅ Created security configuration

### Fixed Endpoint
1. ✅ Updated `/api/v1/orders` endpoint to support query parameter
2. ✅ Now accepts `?customerId=testuser` in addition to path parameter

## How to Test Now

```bash
# Step 1: Get token from auth-service
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# Copy the token from response

# Step 2: Call orders endpoint WITH token (should work now)
curl -X GET "http://localhost:8082/api/v1/orders?customerId=testuser" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Result**: ✅ **200 OK** with order list

## Files Changed

| File | What Changed |
|------|--------------|
| `pom.xml` | Added Spring Security + JWT |
| `config/SecurityConfig.java` | NEW - Security configuration |
| `config/JwtTokenProvider.java` | NEW - JWT parsing |
| `config/JwtAuthenticationFilter.java` | NEW - JWT filter |
| `application.yml` | Added jwt.secret config |
| `controller/OrderController.java` | Added query parameter support |

## Key Points

- ✅ All requests now require Bearer token
- ✅ Token is validated by JWT filter
- ✅ Query parameter endpoint now works: `?customerId=testuser`
- ✅ No compilation errors
- ✅ Same JWT secret as auth-service for seamless integration

## Endpoint Usage Examples

**Query Parameter (Fixed)**:
```
GET /api/v1/orders?customerId=testuser
Header: Authorization: Bearer <token>
```

**Path Parameter (Existing)**:
```
GET /api/v1/orders/customer/testuser
Header: Authorization: Bearer <token>
```

**Get All**:
```
GET /api/v1/orders
Header: Authorization: Bearer <token>
```

## Credentials for Testing
- **Username**: user
- **Password**: user123
- **Auth Service Port**: 8083
- **Order Service Port**: 8082
