# Product Catalog Service - Test Quick Reference

## Quick Test Execution Guide

### 1. Run All Tests
```bash
cd C:\Microservices\Microservices-Capstone-master\product-catalog-service
mvn clean test
```

### 2. Run Individual Test Classes

**Repository Tests:**
```bash
mvn test -Dtest=ProductRepositoryTest
```

**Service Tests:**
```bash
mvn test -Dtest=ProductServiceTest
```

**Controller Tests:**
```bash
mvn test -Dtest=ProductControllerTest
```

**Integration Tests:**
```bash
mvn test -Dtest=ProductCatalogIntegrationTest
```

### 3. Run Specific Test Methods
```bash
mvn test -Dtest=ProductServiceTest#testCreateProduct
mvn test -Dtest=ProductControllerTest#testGetProductById
```

### 4. Generate Test Coverage Report
```bash
mvn clean test jacoco:report
# Report available at: target/site/jacoco/index.html
```

---

## Test Layers Explained

### Layer 1: Repository Tests (ProductRepositoryTest.java)
**What it tests:** Database operations
- Save, Read, Update, Delete operations
- Custom query methods (findBySku)
- Database constraints (unique SKU)

**Environment:** 
- H2 in-memory database
- Isolated JPA context
- Auto-rollback after each test

**When to use:** 
- Testing query logic
- Validating ORM mappings
- Testing database constraints

---

### Layer 2: Service Tests (ProductServiceTest.java)
**What it tests:** Business logic
- Product creation with validation
- Inventory adjustments
- DTO transformations
- Error handling

**Environment:**
- Mocked repository
- No database access
- No Spring context overhead
- Fast execution

**When to use:**
- Testing business rules
- Testing calculations
- Testing conditional logic
- Testing error scenarios

---

### Layer 3: Controller Tests (ProductControllerTest.java)
**What it tests:** REST API endpoints
- HTTP methods (GET, POST, PUT, DELETE, PATCH)
- Request/response validation
- HTTP status codes (200, 201, 404, 400)
- Error responses

**Environment:**
- Mocked service layer
- MockMvc for HTTP testing
- No real database access
- Lightweight execution

**When to use:**
- Testing endpoint contracts
- Testing request/response formats
- Testing error responses
- Testing HTTP semantics

---

### Layer 4: Integration Tests (ProductCatalogIntegrationTest.java)
**What it tests:** End-to-end workflows
- Complete CRUD operations
- Multi-step operations
- Real component interaction
- Full application lifecycle

**Environment:**
- Full Spring Boot context
- Real database (H2)
- Full HTTP stack
- Transaction support

**When to use:**
- Testing complete features
- Testing component interactions
- Testing data persistence
- Testing business workflows

---

## Test Method Naming Convention

```
test[OperationName][Condition][Result]

Examples:
- testCreateProduct() - Tests successful creation
- testCreateProductSkuAlreadyExists() - Tests duplicate SKU error
- testFindProductByIdNotFound() - Tests 404 scenario
- testAdjustInventoryBelowZero() - Tests edge case
```

---

## Common Assertions Used

```java
// Verify object state
assertEquals(expected, actual)
assertNotNull(object)
assertTrue(condition)
assertFalse(condition)

// Verify collection state
assertEquals(size, collection.size())
assertTrue(collection.isEmpty())

// Verify exceptions
assertThrows(ExceptionType.class, () -> { code })

// Verify HTTP responses
mockMvc.perform(get("/api/v1/products"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$[0].name").value("Expected Name"))

// Verify mock interactions
verify(mock, times(1)).method()
verify(mock, never()).method()
```

---

## Test Data Setup

### Product Test Fixture
```java
ProductDto testProductDto = new ProductDto(
    1L,                                    // id
    "Test Product",                       // name
    "A test product description",         // description
    new BigDecimal("99.99"),             // price
    10,                                   // quantity
    "TEST-SKU-001"                       // sku
);
```

---

## Coverage Statistics

| Layer | Coverage |
|-------|----------|
| Repository | 10 test methods |
| Service | 17 test methods |
| Controller | 16 test methods |
| Integration | 15 test methods |
| **Total** | **58 test methods** |

---

## Environment Configuration

### Test Database (H2)
```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:productcatalog_test
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
```

### Eureka Disabled for Tests
```yaml
eureka:
  client:
    enabled: false
```

---

## Troubleshooting Tests

### Tests fail to compile
```bash
# Update IDE index
mvn clean compile

# Reimport Maven project in IDE
Right-click project > Maven > Update Project
```

### Database locks during tests
```bash
# Clear test database
mvn clean test

# Run with fresh context
mvn test -Dtest=ProductRepositoryTest -X
```

### Mocking issues
```bash
# Verify Mockito is in classpath
mvn dependency:tree | grep mockito

# Reinstall dependencies
mvn clean install -DskipTests
```

### Performance issues
```bash
# Run only service layer (fastest)
mvn test -Dtest=ProductServiceTest

# Skip integration tests
mvn test -DskipITs
```

---

## Best Practices Applied

✓ **Single Responsibility** - Each test tests one thing
✓ **Clear Names** - Descriptive test method names
✓ **AAA Pattern** - Arrange, Act, Assert structure
✓ **Isolation** - Mocked dependencies where needed
✓ **Edge Cases** - Null checks, empty lists, negative values
✓ **Error Handling** - Tests for exceptions and error paths
✓ **Fast Execution** - Mocks reduce execution time
✓ **Repeatable** - No test order dependencies
✓ **Self-Documenting** - Tests serve as usage examples

---

## Test Execution Flow

```
1. ProductRepositoryTest
   └─ Tests database layer directly
   └─ Uses H2 in-memory DB
   └─ Validates ORM mappings

2. ProductServiceTest
   └─ Tests business logic
   └─ Mocks repository
   └─ Fast execution

3. ProductControllerTest
   └─ Tests REST endpoints
   └─ Mocks service layer
   └─ Validates HTTP contract

4. ProductCatalogIntegrationTest
   └─ End-to-end testing
   └─ Full Spring context
   └─ Real database interactions
```

---

## Next Steps

1. **Run Tests:** Execute `mvn clean test` to verify all tests pass
2. **Review Coverage:** Check target/site/jacoco/index.html for coverage report
3. **Add Custom Tests:** Extend tests for custom business requirements
4. **CI/CD Integration:** Add tests to your build pipeline
5. **Monitor:** Track test execution times and coverage over time

