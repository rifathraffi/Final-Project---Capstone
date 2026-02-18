# Quick Start Guide - Role-Based Access Control for Create Product

## Problem Fixed ✅
The POST `/api/v1/products` endpoint now **only allows ADMIN role** users to create products. USER role users will receive a **403 Forbidden** response.

## Files Changed

### 1. New Configuration Files
- ✅ `config/JwtTokenProvider.java` - Parses JWT tokens and extracts roles
- ✅ `config/JwtAuthenticationFilter.java` - Validates tokens and sets authentication
- ✅ `config/SecurityConfig.java` - Configures Spring Security with JWT filter

### 2. Modified Files
- ✅ `pom.xml` - Added JWT dependencies (jjwt-api, jjwt-impl, jjwt-jackson v0.11.5)
- ✅ `controller/ProductController.java` - Added `@PreAuthorize("hasAuthority('ADMIN')")` to create method
- ✅ `resources/application.yml` - Added jwt.secret configuration

## How to Test

### Test Case 1: ADMIN User (Should Succeed ✅)
```bash
# Step 1: Login as admin
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Copy the token from response

# Step 2: Create product with token
curl -X POST http://localhost:8081/api/v1/products \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "sku": "TEST-SKU-001",
    "price": 99.99,
    "stockQuantity": 100,
    "description": "Test product"
  }'

# Expected Response: 201 Created
```

### Test Case 2: USER User (Should Fail ❌)
```bash
# Step 1: Login as user
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# Copy the token from response

# Step 2: Try to create product with token
curl -X POST http://localhost:8081/api/v1/products \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "sku": "TEST-SKU-001",
    "price": 99.99,
    "stockQuantity": 100,
    "description": "Test product"
  }'

# Expected Response: 403 Forbidden
```

## Architecture

```
Request with Bearer Token
        ↓
JwtAuthenticationFilter (extracts token)
        ↓
JwtTokenProvider (validates & parses)
        ↓
Extract username + role from JWT claims
        ↓
Set SecurityContext with ADMIN/USER authority
        ↓
@PreAuthorize("hasAuthority('ADMIN')") check
        ├→ ✅ ADMIN role: Proceed to create product (201 Created)
        └→ ❌ USER role: Return 403 Forbidden
```

## Credentials

- **Admin User**: username=`admin`, password=`admin123`, role=`ADMIN`
- **Regular User**: username=`user`, password=`user123`, role=`USER`

## Important Notes

1. **Same JWT Secret**: Both auth-service and product-catalog-service use the same JWT secret for signature validation
2. **Stateless**: No session management - each request is independently authenticated
3. **Token Format**: `Authorization: Bearer <jwt-token>`
4. **Other Methods**: GET, PUT, PATCH, DELETE endpoints remain accessible to all authenticated users (no role restriction)
5. **Token Expiration**: Tokens are valid for 24 hours

## Troubleshooting

**Still getting 403?**
- Verify the Bearer token is correctly formatted: `Authorization: Bearer <token>`
- Verify the token is from auth-service login endpoint
- Verify you're using the ADMIN user credentials
- Check that jwt.secret matches in application.yml

**Token validation fails?**
- Ensure the JWT_SECRET environment variable matches between services
- Check token hasn't expired (24-hour validity)
- Verify token format hasn't been modified
