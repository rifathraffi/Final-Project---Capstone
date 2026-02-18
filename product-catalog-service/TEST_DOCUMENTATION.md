# JUnit Test Cases for Product Catalog Service

This document summarizes the comprehensive JUnit test suite created for the product-catalog-service.

## Test Files Created

### 1. ProductRepositoryTest.java
**Location:** `src/test/java/com/example/productcatalog/repository/ProductRepositoryTest.java`

**Annotation:** `@DataJpaTest`

**Test Coverage:**
- `testSaveProduct()` - Verifies product save operation and ID generation
- `testFindProductById()` - Tests retrieving a product by ID
- `testFindProductByIdNotFound()` - Tests handling of non-existent product ID
- `testFindAllProducts()` - Tests retrieving all products
- `testFindProductBySku()` - Tests finding product by SKU
- `testFindProductBySkuNotFound()` - Tests handling of non-existent SKU
- `testExistsBySku()` - Tests SKU existence checking
- `testDeleteProduct()` - Tests product deletion
- `testUpdateProduct()` - Tests updating product information
- `testSkuUniqueness()` - Tests SKU unique constraint enforcement

**Key Features:**
- Tests database persistence layer
- Uses H2 in-memory database for testing
- Validates JPA operations
- Tests repository custom methods (findBySku, existsBySku)
- Total: 10 test cases

---

### 2. ProductServiceTest.java
**Location:** `src/test/java/com/example/productcatalog/service/ProductServiceTest.java`

**Annotations:** `@ExtendWith(MockitoExtension.class)`

**Test Coverage:**
- `testFindAll()` - Tests retrieving all products
- `testFindAllEmpty()` - Tests empty list handling
- `testFindById()` - Tests finding product by ID
- `testFindByIdNotFound()` - Tests non-existent product handling
- `testFindBySku()` - Tests finding product by SKU
- `testFindBySkuNotFound()` - Tests non-existent SKU handling
- `testCreateProduct()` - Tests product creation
- `testCreateProductSkuAlreadyExists()` - Tests duplicate SKU validation
- `testCreateProductWithNullDescription()` - Tests null description handling
- `testUpdateProduct()` - Tests product update
- `testUpdateProductNotFound()` - Tests update on non-existent product
- `testAdjustInventorySuccess()` - Tests inventory increment
- `testAdjustInventoryDecrement()` - Tests inventory decrement
- `testAdjustInventoryBelowZero()` - Tests inventory floor (cannot go below 0)
- `testAdjustInventoryProductNotFound()` - Tests inventory adjustment on non-existent product
- `testDeleteById()` - Tests product deletion
- `testProductDtoMapping()` - Tests DTO mapping functionality

**Key Features:**
- Unit tests with mocked repository layer
- Uses Mockito for dependency mocking
- Tests business logic in isolation
- Tests InventoryResult sealed interface (InventorySuccess/InventoryError)
- Tests transactional operations
- Total: 17 test cases

---

### 3. ProductControllerTest.java
**Location:** `src/test/java/com/example/productcatalog/controller/ProductControllerTest.java`

**Annotations:** `@ExtendWith(MockitoExtension.class)`

**Test Coverage:**
- `testListAllProducts()` - Tests GET /api/v1/products endpoint
- `testListAllProductsEmpty()` - Tests empty products list response
- `testGetProductById()` - Tests GET /api/v1/products/{id} endpoint
- `testGetProductByIdNotFound()` - Tests 404 response for non-existent product
- `testGetProductBySku()` - Tests GET /api/v1/products/sku/{sku} endpoint
- `testGetProductBySkuNotFound()` - Tests 404 response for non-existent SKU
- `testCreateProduct()` - Tests POST /api/v1/products endpoint
- `testCreateProductDuplicateSku()` - Tests 400 response for duplicate SKU
- `testCreateProductValidationError()` - Tests validation error handling
- `testUpdateProduct()` - Tests PUT /api/v1/products/{id} endpoint
- `testUpdateProductNotFound()` - Tests 404 response on update
- `testAdjustInventorySuccess()` - Tests PATCH /api/v1/products/{sku}/inventory endpoint
- `testAdjustInventoryDecrement()` - Tests inventory decrease operation
- `testAdjustInventoryProductNotFound()` - Tests 404 response for inventory adjustment
- `testDeleteProduct()` - Tests DELETE /api/v1/products/{id} endpoint
- `testCreateProductWithMinimalFields()` - Tests product creation with minimal data

**Key Features:**
- MockMvc for testing REST endpoints
- Tests HTTP status codes and response bodies
- JSON response validation using jsonPath
- Tests error handling and validation
- Total: 16 test cases

---

### 4. ProductCatalogIntegrationTest.java
**Location:** `src/test/java/com/example/productcatalog/ProductCatalogIntegrationTest.java`

**Annotations:** 
- `@SpringBootTest` - Full Spring context
- `@AutoConfigureMockMvc` - Auto-configured MockMvc
- `@ActiveProfiles("test")` - Uses test profile

**Test Coverage:**
- `testCreateProduct()` - End-to-end product creation
- `testGetAllProducts()` - End-to-end get all products
- `testGetProductById()` - End-to-end get product by ID
- `testGetProductBySku()` - End-to-end get product by SKU
- `testUpdateProduct()` - End-to-end product update
- `testDeleteProduct()` - End-to-end product deletion with verification
- `testAdjustInventoryIncrement()` - End-to-end inventory increment
- `testAdjustInventoryDecrement()` - End-to-end inventory decrement
- `testAdjustInventoryBelowZero()` - End-to-end inventory floor enforcement
- `testCreateProductWithDuplicateSku()` - End-to-end duplicate SKU validation
- `testCreateAndRetrieveMultipleProducts()` - End-to-end multi-product operations
- `testFullProductLifecycle()` - Complete lifecycle: create → get → update → adjust inventory → delete
- `testInvalidProductCreation()` - End-to-end validation testing
- `testUpdateNonExistentProduct()` - End-to-end 404 handling
- `testAdjustInventoryForNonExistentProduct()` - End-to-end error handling

**Key Features:**
- Full application context testing
- Real database interactions (using H2 test database)
- End-to-end workflow testing
- Tests complete CRUD operations
- Tests complex business workflows
- Total: 15 test cases

---

## Test Statistics

| Layer | Test Class | Count | Type |
|-------|-----------|-------|------|
| Repository | ProductRepositoryTest | 10 | Integration (Database) |
| Service | ProductServiceTest | 17 | Unit (Mocked) |
| Controller | ProductControllerTest | 16 | Unit (MockMvc) |
| Integration | ProductCatalogIntegrationTest | 15 | End-to-End |
| **TOTAL** | **4 Classes** | **58** | **Mixed** |

---

## Test Configuration

### Test Profile
- **File:** `src/test/resources/application-test.yml`
- **Database:** H2 in-memory
- **Hibernate DDL:** create-drop
- **Eureka:** Disabled for tests

### Dependencies Added
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Running the Tests

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ProductRepositoryTest
mvn test -Dtest=ProductServiceTest
mvn test -Dtest=ProductControllerTest
mvn test -Dtest=ProductCatalogIntegrationTest
```

### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

---

## Test Coverage Overview

### Repository Layer
- CRUD operations (Create, Read, Update, Delete)
- Custom query methods (findBySku, existsBySku)
- Constraint validation (SKU uniqueness)

### Service Layer
- Business logic validation
- DTO mapping
- Inventory adjustment logic
- Error handling and edge cases
- Transactional behavior

### Controller Layer
- HTTP endpoint testing
- Request/response validation
- HTTP status code verification
- Error response handling
- Validation error handling

### Integration Layer
- Complete workflow testing
- Multi-step operations
- Database persistence
- Real component interaction
- End-to-end scenarios

---

## Key Testing Patterns Used

1. **@DataJpaTest** - Repository layer testing with isolated database context
2. **@ExtendWith(MockitoExtension.class)** - Unit testing with Mockito mocks
3. **@SpringBootTest + @AutoConfigureMockMvc** - Integration testing with full Spring context
4. **MockMvc** - REST endpoint testing
5. **Mockito.when()** - Behavior stubbing
6. **verify()** - Method invocation verification
7. **Optional handling** - Testing optional return types
8. **Exception testing** - @Test expected exceptions

---

## Notes

- All tests use JUnit 5 (Jupiter)
- Tests follow AAA pattern (Arrange, Act, Assert)
- Mock objects used where appropriate
- Clear, descriptive test method names
- Each test method focuses on a single behavior
- Edge cases and error conditions covered
- Comprehensive validation of API contracts

