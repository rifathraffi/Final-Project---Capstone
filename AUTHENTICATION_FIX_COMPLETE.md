# 401 Unauthorized Fix - Comprehensive Solution

## Root Cause Analysis

The 401 Unauthorized error was caused by **3 critical issues**:

### Issue 1: Incorrect Authorization Rule
**Before**:
```java
.authorizeHttpRequests(authz -> authz
    .anyRequest().permitAll()  // ❌ WRONG: Allows ALL requests without authentication
)
```
This allowed requests to bypass Spring Security entirely, so the JWT filter never got a chance to validate tokens.

**After**:
```java
.authorizeHttpRequests(authz -> authz
    .anyRequest().authenticated()  // ✅ CORRECT: Requires authentication
)
```

### Issue 2: Field Injection in Filter
**Before**:
```java
@Autowired
private JwtAuthenticationFilter jwtAuthenticationFilter;  // ❌ Lazy initialization
```
Using `@Autowired` field injection can cause timing issues with filter bean initialization.

**After**:
```java
private final JwtAuthenticationFilter jwtAuthenticationFilter;
private final JwtTokenProvider jwtTokenProvider;

public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, 
                      JwtTokenProvider jwtTokenProvider) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtTokenProvider = jwtTokenProvider;
}
// ✅ CORRECT: Constructor injection ensures proper initialization
```

### Issue 3: Filter Dependencies
**Before**:
```java
@Autowired
private JwtTokenProvider jwtTokenProvider;  // ❌ Lazy initialization
```

**After**:
```java
private final JwtTokenProvider jwtTokenProvider;

public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
}
// ✅ CORRECT: Constructor injection
```

## Changes Made

### 1. Order Management Service - SecurityConfig.java
**Key Changes**:
- ✅ Changed `permitAll()` to `authenticated()`
- ✅ Changed `@Autowired` to constructor injection
- ✅ Added exception handling for 401 responses
- ✅ Requires valid JWT token for all endpoints

```java
.authorizeHttpRequests(authz -> authz
    .anyRequest().authenticated()
)
```

### 2. Order Management Service - JwtAuthenticationFilter.java
**Key Changes**:
- ✅ Changed `@Autowired` to constructor injection
- ✅ Added debug logging for troubleshooting
- ✅ Improved token extraction and validation logic

### 3. Product Catalog Service - SecurityConfig.java
**Key Changes**:
- ✅ Allow public GET requests (product catalog is public)
- ✅ Require authentication for POST/PUT/PATCH/DELETE
- ✅ Changed to constructor injection
- ✅ Added exception handling

```java
.authorizeHttpRequests(authz -> authz
    .requestMatchers("GET", "/api/v1/products/**").permitAll()
    .anyRequest().authenticated()
)
```

### 4. Product Catalog Service - JwtAuthenticationFilter.java
**Key Changes**:
- ✅ Changed `@Autowired` to constructor injection
- ✅ Added debug logging

## How It Now Works

```
User sends request with Bearer token
        ↓
HTTP request arrives at security filter chain
        ↓
JwtAuthenticationFilter runs BEFORE other authentication filters
        ↓
extracts token from Authorization header: "Bearer <token>"
        ↓
JwtTokenProvider validates token signature and expiration
        ↓
If valid → Extract username & role → Set in SecurityContext
If invalid → Token remains null
        ↓
SecurityFilterChain checks .authorizeHttpRequests()
        ↓
If endpoint requires .authenticated()
    ├→ SecurityContext has authentication → ✅ Request proceeds
    └→ SecurityContext empty (no token) → ❌ 401 Unauthorized
```

## Test Cases

### Test 1: Create Product with ADMIN Token (POST)
```bash
# Get ADMIN token
TOKEN=$(curl -s -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# Create product (requires @PreAuthorize("hasAuthority('ADMIN')") + valid token)
curl -X POST http://localhost:8081/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","sku":"TEST123","price":99.99,"stockQuantity":100}'

# Expected: ✅ 201 Created
```

### Test 2: Get Products without Token (GET)
```bash
# Get products without token (public endpoint)
curl -X GET http://localhost:8081/api/v1/products

# Expected: ✅ 200 OK (no token needed for GET)
```

### Test 3: Get Orders with USER Token (GET)
```bash
# Get USER token
TOKEN=$(curl -s -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}' | jq -r '.token')

# Get orders with token (requires authentication)
curl -X GET "http://localhost:8082/api/v1/orders?customerId=testuser" \
  -H "Authorization: Bearer $TOKEN"

# Expected: ✅ 200 OK
```

### Test 4: Get Orders without Token (GET)
```bash
# Get orders without token (authentication required)
curl -X GET "http://localhost:8082/api/v1/orders?customerId=testuser"

# Expected: ❌ 401 Unauthorized
```

### Test 5: Create Order with USER Token (POST)
```bash
# Create order with USER token
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"testuser","items":[...]}'

# Expected: ✅ 201 Created (token-based auth works)
```

## Files Modified

| Service | File | Changes |
|---------|------|---------|
| order-management | SecurityConfig.java | Constructor injection + authenticated() + exception handling |
| order-management | JwtAuthenticationFilter.java | Constructor injection + debug logging |
| product-catalog | SecurityConfig.java | Constructor injection + GET permitAll() + exception handling |
| product-catalog | JwtAuthenticationFilter.java | Constructor injection + debug logging |

## Key Points

1. **Constructor Injection**: Replaced `@Autowired` with constructor injection for better initialization order
2. **Authorization Rules**: 
   - Order Management: ALL endpoints require authentication
   - Product Catalog: GET requests are public, other methods require authentication
3. **Exception Handling**: Proper 401 response with "Unauthorized" message
4. **Debug Logging**: Added logging to help troubleshoot token issues
5. **Stateless**: No session management - pure JWT-based authentication

## Troubleshooting

If you still get 401:

1. **Verify token is valid**:
   ```bash
   # Should return valid token (not error)
   curl -X POST http://localhost:8083/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

2. **Check Authorization header format**:
   ```bash
   # Must be: "Bearer <token>" with space after Bearer
   curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8082/api/v1/orders
   ```

3. **Verify token expiration**:
   - Tokens are valid for 24 hours
   - If older than 24 hours, generate a new token

4. **Check logs** for debug messages:
   ```
   DEBUG - Extracted token: Token found
   DEBUG - Token is valid
   DEBUG - Set authentication for user: admin with role: ADMIN
   ```

## Summary

✅ **Fixed Issues**:
- Incorrect authorization rules (permitAll → authenticated)
- Field injection timing issues
- Missing exception handling
- Improper dependency injection in filters

✅ **Result**:
- JWT tokens are now properly validated
- 401 Unauthorized only returned when token is invalid/missing
- Valid tokens grant access to protected endpoints
- @PreAuthorize annotations work correctly for role-based access
