# 🚀 Quick Start - How to Use the Fix

## Problem You Had
```
URL: http://localhost:8020/api/v1/orders?customerId=testuser
Token: Bearer token from admin/admin123 credentials
Response: 401 Unauthorized ❌
Expected: 200 OK with order list ✅
```

## What Was Fixed
- ✅ Added JWT validation at the order-management-service level
- ✅ Added role-based authorization checks (@PreAuthorize annotations)
- ✅ Created security filters and configuration
- ✅ Compiled and verified with Maven

## How to Deploy

### Option 1: Using Maven Wrapper (Recommended)
```bash
# Navigate to project root
cd C:\Microservices\Microservices-Capstone-master

# Build only order-management-service
mvnw.cmd clean package -DskipTests -pl order-management-service

# OR build entire project
mvnw.cmd clean package -DskipTests
```

### Option 2: From IDE (Eclipse)
1. Right-click on `order-management-service` project
2. Select `Maven` → `Update Project` (F5)
3. Right-click again
4. Select `Run As` → `Maven build`
5. Enter goals: `clean package -DskipTests`
6. Click Run

## How to Test

### Test 1: Get ADMIN Token
```bash
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Save the `token` value from response.

### Test 2: Use ADMIN Token (Should Work ✅)
```bash
curl "http://localhost:8020/api/v1/orders?customerId=testuser" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN_HERE"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "orderId": "ORD-001",
    "customerId": "testuser",
    ...
  }
]
```

### Test 3: Get USER Token
```bash
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'
```

### Test 4: Use USER Token (Should Fail with 403 ❌)
```bash
curl "http://localhost:8020/api/v1/orders?customerId=testuser" \
  -H "Authorization: Bearer YOUR_USER_TOKEN_HERE"
```

**Expected Response:**
```
403 Forbidden
```

## Using Postman

### Step 1: Create Environment Variables
1. In Postman, click "Environments" → Create New
2. Add variables:
   - `admin_token` = (leave blank, we'll set it)
   - `user_token` = (leave blank, we'll set it)
   - `base_url` = `http://localhost:8020`

### Step 2: Get ADMIN Token
1. Create new request: `POST`
2. URL: `{{base_url}}/api/v1/auth/login`
3. Body (JSON):
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
4. In Tests tab, add:
   ```javascript
   var jsonData = pm.response.json();
   pm.environment.set("admin_token", jsonData.token);
   ```
5. Send and save the admin_token

### Step 3: List Orders with ADMIN
1. Create new request: `GET`
2. URL: `{{base_url}}/api/v1/orders?customerId=testuser`
3. Headers:
   - Key: `Authorization`
   - Value: `Bearer {{admin_token}}`
4. Send → Should get 200 OK ✅

### Step 4: Get USER Token
1. Repeat Step 2 but with `user123` password
2. Set it to `user_token` variable

### Step 5: List Orders with USER
1. Create request similar to Step 3
2. Use `{{user_token}}` instead
3. Send → Should get 403 Forbidden ✅

## What Changed - At a Glance

### New Files (3)
```
order-management-service/src/main/java/com/example/order/
├── config/
│   └── SecurityConfig.java          [NEW - Spring Security setup]
└── security/
    ├── JwtAuthenticationFilter.java  [NEW - JWT filter]
    └── JwtUtil.java                  [NEW - JWT utilities]
```

### Modified Files (4)
```
order-management-service/
├── pom.xml                           [Modified - Added Spring Security]
├── src/main/resources/
│   └── application.yml               [Modified - Added JWT secret]
└── src/main/java/com/example/order/
    ├── OrderManagementApplication.java [Modified - Added @EnableMethodSecurity]
    └── controller/
        └── OrderController.java      [Modified - Added @PreAuthorize]
```

### Authorization Rules
| Endpoint | ADMIN | USER | Note |
|----------|-------|------|------|
| GET `/api/v1/orders?customerId=...` | ✅ | ❌ | This is what you were testing |
| GET `/api/v1/orders/{id}` | ✅ | ✅ | Both can get specific order |
| GET `/api/v1/orders/customer/{id}` | ✅ | ✅ | Alternative endpoint |
| POST `/api/v1/orders` | ✅ | ❌ | Only ADMIN can create |
| PATCH `/api/v1/orders/{id}/status` | ✅ | ❌ | Only ADMIN can update |
| POST `/api/v1/orders/{id}/cancel` | ✅ | ✅ | Both can cancel |

## Troubleshooting

### Issue: "Maven not found"
**Solution:** Use `mvnw.cmd` instead of `mvn`

### Issue: "JWT secret mismatch"
**Solution:** Ensure all services use the same JWT_SECRET environment variable
```bash
set JWT_SECRET=MySecretKeyForJwtTokenGenerationThatIsAtLeast256BitsLongForHs256AlgorithmSecurity
```

### Issue: "401 Unauthorized despite valid token"
**Solutions:**
- Check if token is expired (24 hour validity)
- Verify Bearer format: `Bearer eyJ...` (with space)
- Check if all services are running

### Issue: "403 Forbidden instead of 401"
**Explanation:** This is correct - token is valid, but authorization failed
- You likely have USER role trying to access ADMIN-only endpoint
- This is the expected behavior!

### Issue: Compilation fails
**Solution:** Make sure dependencies downloaded:
```bash
mvnw.cmd dependency:resolve -pl order-management-service
mvnw.cmd clean compile -pl order-management-service
```

## Important Notes ⚠️

1. **JWT Secret**: Must be at least 32 characters and consistent across all services
   - Default: `MySecretKeyForJwtTokenGenerationThatIsAtLeast256BitsLongForHs256AlgorithmSecurity`
   - Can override with `JWT_SECRET` environment variable

2. **Token Validity**: Tokens expire after 24 hours
   - If tests fail with 401, get a fresh token

3. **Role Names**: Case-sensitive
   - ADMIN (not Admin or admin)
   - USER (not User or user)

4. **Authorization Header**: Exact format required
   - Correct: `Authorization: Bearer eyJ...`
   - Wrong: `Authorization: eyJ...` (missing "Bearer ")
   - Wrong: `Authorization: bearer eyJ...` (wrong case)

## Security Best Practices Implemented ✅

✅ **JWT Token Validation** - Tokens verified at service level
✅ **Role-Based Access Control** - Different permissions for different roles
✅ **Defense in Depth** - Multiple layers of security
✅ **Stateless Authentication** - No sessions, scales easily
✅ **Spring Security** - Industry-standard framework
✅ **@PreAuthorize** - Declarative security rules

## Files to Review

1. **SOLUTION_SUMMARY.md** - This complete solution
2. **AUTHORIZATION_FIX.md** - Detailed technical explanation
3. **TESTING_GUIDE.md** - Comprehensive test scenarios
4. **DETAILED_CODE_CHANGES.md** - Exact code changes
5. **VERIFICATION_CHECKLIST.md** - What was changed

## Next Steps

1. ✅ Build the service: `mvnw.cmd clean package -pl order-management-service`
2. ✅ Start the service
3. ✅ Run test cases from "How to Test" section
4. ✅ Verify ADMIN can access `/api/v1/orders?customerId=testuser`
5. ✅ Verify USER gets 403 Forbidden
6. 🔄 Consider applying similar security to other services

## Questions?

Refer to the documentation files:
- **Problem?** → AUTHORIZATION_FIX.md
- **How to test?** → TESTING_GUIDE.md
- **Code changes?** → DETAILED_CODE_CHANGES.md
- **Verify setup?** → VERIFICATION_CHECKLIST.md

---

**Status**: ✅ Ready to Deploy
**Tested**: ✅ Compiled Successfully  
**Documentation**: ✅ Complete
