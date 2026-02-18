# Role-Based Access Control Implementation Summary

## Problem Statement
The create product endpoint (POST `/api/v1/products`) was not enforcing role-based access control. Users with the USER role could create products, which should only be allowed for ADMIN role users.

## Solution Overview
Implemented JWT token-based authentication and authorization in the product-catalog-service using Spring Security's `@PreAuthorize` annotation combined with a custom JWT authentication filter.

## Changes Made

### 1. **Updated pom.xml**
- **Location**: `product-catalog-service/pom.xml`
- **Added Dependencies**:
  - `io.jsonwebtoken:jjwt-api:0.11.5`
  - `io.jsonwebtoken:jjwt-impl:0.11.5`
  - `io.jsonwebtoken:jjwt-jackson:0.11.5`

These dependencies enable JWT token parsing and validation.

### 2. **Created JwtTokenProvider.java**
- **Location**: `product-catalog-service/src/main/java/com/example/productcatalog/config/JwtTokenProvider.java`
- **Purpose**: Utility class to parse and extract information from JWT tokens
- **Key Methods**:
  - `validateToken(String token)` - Validates JWT signature and structure
  - `getUsernameFromToken(String token)` - Extracts username from token
  - `getRoleFromToken(String token)` - Extracts role claim from token
  - `isTokenExpired(String token)` - Checks token expiration
  - `getClaimsFromToken(String token)` - Private method to extract all claims

**How it works**:
- Reads the `jwt.secret` configuration from `application.yml`
- Uses HMAC-SHA algorithm to sign tokens (matches auth-service implementation)
- Extracts the "role" claim which is set by auth-service during login/registration

### 3. **Created JwtAuthenticationFilter.java**
- **Location**: `product-catalog-service/src/main/java/com/example/productcatalog/config/JwtAuthenticationFilter.java`
- **Purpose**: Intercepts incoming requests and extracts JWT token to set authentication context
- **Key Features**:
  - Extends `OncePerRequestFilter` to execute once per request
  - Extracts Bearer token from Authorization header
  - Validates token using `JwtTokenProvider`
  - Creates `UsernamePasswordAuthenticationToken` with role as authority
  - Sets authentication in `SecurityContextHolder` for downstream authorization checks

**Flow**:
1. Request arrives with `Authorization: Bearer <token>`
2. Filter extracts token from header
3. Token is validated (signature + expiration)
4. Username and role are extracted from token claims
5. Authority is set in security context as `SimpleGrantedAuthority(role)`
6. `@PreAuthorize` annotations now work correctly

### 4. **Updated SecurityConfig.java**
- **Location**: `product-catalog-service/src/main/java/com/example/productcatalog/config/SecurityConfig.java`
- **Key Changes**:
  - `@EnableMethodSecurity(prePostEnabled = true)` - Enables `@PreAuthorize` annotation processing
  - `@EnableWebSecurity` - Enables Spring Security
  - Configured `SessionCreationPolicy.STATELESS` - No session storage (stateless JWT)
  - Added JWT filter before `UsernamePasswordAuthenticationFilter` in filter chain
  - Permits all HTTP requests at URL level (actual authorization at method level)

**Configuration Details**:
```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```
This ensures that authentication is purely token-based with no session state.

### 5. **Updated ProductController.java**
- **Location**: `product-catalog-service/src/main/java/com/example/productcatalog/controller/ProductController.java`
- **Changes**:
  - Added import: `import org.springframework.security.access.prepost.PreAuthorize;`
  - Added annotation to `create()` method: `@PreAuthorize("hasAuthority('ADMIN')")`

**Effect**: 
- Only requests with ADMIN authority in SecurityContext can call this method
- All other requests receive HTTP 403 Forbidden response

### 6. **Updated application.yml**
- **Location**: `product-catalog-service/src/main/resources/application.yml`
- **Added Configuration**:
```yaml
jwt:
  secret: "${JWT_SECRET:MySecretKeyForJwtTokenGenerationThatIsAtLeast256BitsLongForHs256AlgorithmSecurity}"
```
- Uses the same default secret as auth-service
- Can be overridden via `JWT_SECRET` environment variable

## How It Works End-to-End

### Scenario 1: Admin User Creates Product (✅ Success)

1. **Login Request** (auth-service):
   ```
   POST /api/v1/auth/login
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
   Response includes JWT token with `"role": "ADMIN"`

2. **Create Product Request** (product-catalog-service):
   ```
   POST /api/v1/products
   Authorization: Bearer <token>
   {
     "name": "Product",
     "sku": "SKU123",
     ...
   }
   ```

3. **Request Processing**:
   - `JwtAuthenticationFilter` extracts token
   - `JwtTokenProvider` validates and parses token
   - Username and role ("ADMIN") extracted from token
   - `SecurityContextHolder` set with authority "ADMIN"
   - `@PreAuthorize("hasAuthority('ADMIN')")` check passes
   - Product is created ✅

### Scenario 2: Regular User Attempts to Create Product (❌ Forbidden)

1. **Login Request** (auth-service):
   ```
   POST /api/v1/auth/login
   {
     "username": "user",
     "password": "user123"
   }
   ```
   Response includes JWT token with `"role": "USER"`

2. **Create Product Request** (product-catalog-service):
   ```
   POST /api/v1/products
   Authorization: Bearer <token>
   {...}
   ```

3. **Request Processing**:
   - `JwtAuthenticationFilter` extracts token
   - `JwtTokenProvider` validates and parses token
   - Username and role ("USER") extracted from token
   - `SecurityContextHolder` set with authority "USER"
   - `@PreAuthorize("hasAuthority('ADMIN')")` check **FAILS**
   - HTTP 403 Forbidden response returned ❌

## Key Points

1. **Token Format**: JWT tokens are generated by auth-service with role as a claim
2. **Signature Validation**: Uses the same secret key as auth-service (HMAC-SHA)
3. **Stateless**: No session management - each request is independently authenticated
4. **Filter Order**: JWT filter runs before Spring's default authentication filters
5. **Authority Format**: Role from token (e.g., "ADMIN") is directly used as authority

## Testing

To test this implementation:

1. **Get ADMIN token**:
   ```bash
   curl -X POST http://localhost:8083/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

2. **Create product with ADMIN token** (should succeed):
   ```bash
   curl -X POST http://localhost:8081/api/v1/products \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"name":"Product","sku":"SKU123",...}'
   ```
   Response: **201 Created**

3. **Get USER token**:
   ```bash
   curl -X POST http://localhost:8083/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"user","password":"user123"}'
   ```

4. **Try to create product with USER token** (should fail):
   ```bash
   curl -X POST http://localhost:8081/api/v1/products \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"name":"Product","sku":"SKU123",...}'
   ```
   Response: **403 Forbidden** with error message

## Files Modified/Created

| File | Type | Status |
|------|------|--------|
| `pom.xml` | Modified | Added JWT dependencies |
| `config/SecurityConfig.java` | Modified | Integrated JWT filter |
| `config/JwtTokenProvider.java` | Created | JWT parsing utility |
| `config/JwtAuthenticationFilter.java` | Created | JWT authentication filter |
| `controller/ProductController.java` | Modified | Added @PreAuthorize |
| `resources/application.yml` | Modified | Added jwt.secret config |

## Notes

- Other endpoints (GET, UPDATE, DELETE) do NOT have role restrictions and remain accessible to all authenticated users
- The solution is based on Spring Security 6.x (compatible with Spring Boot 4.0.2)
- The JWT token structure matches the auth-service implementation for seamless integration
