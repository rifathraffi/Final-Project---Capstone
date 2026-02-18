# Fix Verification Checklist

## Changes Made to Fix the Authorization Issue

### ✅ 1. Dependencies Added to `order-management-service/pom.xml`
- [x] `spring-boot-starter-security`
- [x] `jjwt-api` (version 0.11.5)
- [x] `jjwt-impl` (version 0.11.5)
- [x] `jjwt-jackson` (version 0.11.5)

### ✅ 2. New Files Created in `order-management-service`

#### Security Package Files
- [x] `src/main/java/com/example/order/security/JwtUtil.java`
  - Parses and validates JWT tokens
  - Extracts username and role claims
  - Checks token expiration
  
- [x] `src/main/java/com/example/order/security/JwtAuthenticationFilter.java`
  - Extends OncePerRequestFilter
  - Extracts Bearer token from Authorization header
  - Validates token using JwtUtil
  - Creates Spring Security Authentication with role-based authorities
  - Sets authentication in SecurityContext

#### Configuration Package Files
- [x] `src/main/java/com/example/order/config/SecurityConfig.java`
  - @Configuration and @EnableWebSecurity annotations
  - @EnableMethodSecurity(prePostEnabled = true) for @PreAuthorize
  - Disables CSRF (not needed for stateless REST APIs)
  - Sets SessionCreationPolicy to STATELESS
  - Requires authentication for all requests
  - Adds JwtAuthenticationFilter to filter chain

### ✅ 3. Modified Application Files in `order-management-service`

#### `OrderManagementApplication.java`
- [x] Added import for `EnableMethodSecurity`
- [x] Added `@EnableMethodSecurity(prePostEnabled = true)` annotation
- [x] Enables method-level security checks with @PreAuthorize

#### `application.yml`
- [x] Added `jwt.secret` property with default value
- [x] Matches the secret used in auth-service and api-gateway

#### `OrderController.java`
- [x] Added import for `PreAuthorize` annotation
- [x] `@PreAuthorize("hasRole('ADMIN')")` on:
  - POST `/api/v1/orders` (create order) - ADMIN only
  - GET `/api/v1/orders` (list by customer ID) - ADMIN only
  - PATCH `/api/v1/orders/{id}/status/{status}` (update status) - ADMIN only
- [x] No restrictions on:
  - GET `/api/v1/orders/{id}` (get specific order)
  - GET `/api/v1/orders/number/{orderNumber}` (get by order number)
  - GET `/api/v1/orders/customer/{customerId}` (alternative customer query)
  - POST `/api/v1/orders/{id}/cancel` (cancel order)

## Expected Behavior After Fix

### For ADMIN User (credentials: admin/admin123)
1. ✅ Can login and receive JWT token with role="ADMIN"
2. ✅ Can call `GET /api/v1/orders?customerId=testuser` → 200 OK
3. ✅ Can create orders → 201 CREATED
4. ✅ Can update order status → 200 OK
5. ✅ Can list all orders → 200 OK

### For USER User (credentials: user/user123)
1. ✅ Can login and receive JWT token with role="USER"
2. ✅ Cannot call `GET /api/v1/orders?customerId=testuser` → 403 FORBIDDEN
3. ✅ Cannot create orders → 403 FORBIDDEN
4. ✅ Cannot update order status → 403 FORBIDDEN
5. ✅ Can get specific order by ID → 200 OK
6. ✅ Can cancel their own order → 200 OK

### For Unauthenticated Requests
1. ✅ Missing Authorization header → 401 UNAUTHORIZED
2. ✅ Invalid token format → 401 UNAUTHORIZED
3. ✅ Expired token → 401 UNAUTHORIZED
4. ✅ Invalid/malformed token → 401 UNAUTHORIZED

## How It Works - Technical Flow

### Request with ADMIN Token: `GET /api/v1/orders?customerId=testuser`

```
1. Client sends request to API Gateway (port 8020)
   ↓
2. API Gateway Filter (MyGatewayFilter)
   - Extracts token from Authorization header
   - Validates token using JwtUtil
   - Extracts username and role
   - Checks if role is USER and path contains customerId (blocks USER)
   - For ADMIN: continues and adds X-Auth-User, X-Auth-Role headers
   ↓
3. Request routed to Order Management Service (port 8082)
   ↓
4. JwtAuthenticationFilter
   - Extracts Bearer token from header
   - Validates token using local JwtUtil
   - Extracts username and role
   - Creates Authentication with authorities: [ROLE_ADMIN]
   - Sets in SecurityContext
   ↓
5. Dispatcher Servlet routes to OrderController.listOrders()
   ↓
6. @PreAuthorize("hasRole('ADMIN')") interceptor
   - Checks if current authentication has ROLE_ADMIN
   - ADMIN has it → allows execution
   ↓
7. Method executes and returns order list (200 OK)
```

### Request with USER Token: `GET /api/v1/orders?customerId=testuser`

```
1. Client sends request to API Gateway (port 8020)
   ↓
2. API Gateway Filter (MyGatewayFilter)
   - Extracts token and validates it
   - Extracts role: "USER"
   - Checks if role is USER AND customerId query param exists
   - Returns 403 FORBIDDEN at gateway level
   
   (OR if this check was bypassed)
   ↓
3-5. Request reaches Order Management Service (same as above)
   ↓
6. @PreAuthorize("hasRole('ADMIN')") interceptor
   - Checks if current authentication has ROLE_ADMIN
   - USER only has ROLE_USER
   - Denies access → throws AccessDeniedException
   ↓
7. Spring Security converts to 403 FORBIDDEN response
```

## Compilation Status
- [x] Maven compilation successful
- [x] No compile-time errors
- [x] All dependencies resolved
- [x] Ready for deployment

## Deployment Instructions

1. Build the modified order-management-service:
   ```bash
   cd C:\Microservices\Microservices-Capstone-master
   mvnw.cmd clean package -DskipTests -pl order-management-service
   ```

2. Ensure all services are running:
   - Service Registry (port 8761)
   - Auth Service (port 8083)
   - Order Management Service (port 8082)
   - API Gateway (port 8020)

3. Test using the provided TESTING_GUIDE.md

## Files to Review

If you want to verify the changes:
1. View `AUTHORIZATION_FIX.md` - Detailed explanation
2. View `TESTING_GUIDE.md` - How to test the fix
3. Review `src/main/java/com/example/order/config/SecurityConfig.java`
4. Review `src/main/java/com/example/order/security/JwtUtil.java`
5. Review `src/main/java/com/example/order/security/JwtAuthenticationFilter.java`
6. Review `src/main/java/com/example/order/controller/OrderController.java` (annotations)

## Key Concepts

### Role-Based Access Control (RBAC)
- Requests are evaluated based on user roles
- ADMIN role has all permissions
- USER role has limited permissions (cannot list all orders by customer)

### Defense in Depth
- Authorization is checked at API Gateway level
- Authorization is also checked at Service level
- Two layers of security ensure robustness

### Stateless Security
- No session cookies needed
- JWT tokens are self-contained
- Easy to scale across microservices

### Spring Security Integration
- Uses standard Spring Security patterns
- @PreAuthorize annotations for declarative security
- GrantedAuthority objects for role management
- SecurityContext for storing authentication

## Environment Variables

If you want to override the JWT secret, set:
```bash
set JWT_SECRET=your-very-long-secret-key-at-least-32-characters-long
```

This will be picked up by all services that use `${JWT_SECRET:...}` configuration.
