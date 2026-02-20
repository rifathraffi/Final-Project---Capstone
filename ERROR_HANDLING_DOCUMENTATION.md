# Comprehensive Error Handling Implementation

## Overview
This document summarizes the comprehensive error handling implemented for both **product-catalog-service** and **order-management-service**. All methods now return meaningful error messages and HTTP status codes.

---

## Product-Catalog-Service Error Handling

### Exception Hierarchy

#### 1. **ProductException** (Base Exception)
- Base class for all product-related exceptions
- Customizable HTTP status codes
- Provides meaningful error messages

#### 2. **ProductNotFoundException**
- Thrown when a product is not found by ID or SKU
- HTTP Status: **404 NOT FOUND**
- Example: Product not found with id: 5

#### 3. **ProductAlreadyExistsException**
- Thrown when attempting to create a product with duplicate SKU
- HTTP Status: **409 CONFLICT**
- Example: Product with SKU 'SKU-001' already exists

#### 4. **InvalidProductDataException**
- Thrown when product data validation fails
- HTTP Status: **400 BAD REQUEST**
- Example: Product price must be greater than 0

#### 5. **InventoryException**
- Thrown when inventory operations fail
- HTTP Status: **400 BAD REQUEST** (customizable)
- Example: Product not found with SKU: INVALID

### ProductService Methods with Error Handling

1. **findAll()**
   - Logs all fetch operations
   - Throws ProductException on database errors
   - Returns list of all products with proper error handling

2. **findById(Long id)**
   - Validates ID is positive number
   - Returns Optional<ProductDto>
   - Throws InvalidProductDataException for invalid IDs

3. **findBySku(String sku)**
   - Validates SKU is not blank
   - Returns Optional<ProductDto>
   - Throws InvalidProductDataException for blank SKUs

4. **create(ProductDto dto)**
   - Validates: DTO not null, SKU not blank, name not blank, price > 0, quantity >= 0
   - Checks for duplicate SKU (throws ProductAlreadyExistsException)
   - Logs creation with ID and SKU
   - Throws ProductAlreadyExistsException (409) for duplicates

5. **update(Long id, ProductDto dto)**
   - Validates ID and DTO data
   - Validates all product fields
   - Logs update operations
   - Returns Optional<ProductDto>

6. **adjustInventory(String sku, int delta)**
   - Validates SKU not blank
   - Throws InventoryException if product not found
   - Returns InventoryResult (Success or Error)
   - Logs all inventory adjustments

7. **deleteById(Long id)**
   - Validates ID is positive
   - Throws ProductNotFoundException if not found
   - Logs deletion operations

### ProductController Methods with Error Handling

1. **list()** - GET /api/v1/products
   - Returns ResponseEntity with proper status codes
   - Logs all operations
   - Throws exceptions on errors

2. **getById(Long id)** - GET /api/v1/products/{id}
   - Throws ProductNotFoundException if not found
   - Logs retrieval operations
   - Returns 404 or 200 status

3. **getBySku(String sku)** - GET /api/v1/products/sku/{sku}
   - Throws ProductNotFoundException if not found
   - Validates SKU parameter
   - Returns 404 or 200 status

4. **create(ProductDto dto)** - POST /api/v1/products
   - HTTP Status: **201 CREATED** on success
   - HTTP Status: **400 BAD REQUEST** for invalid data
   - HTTP Status: **409 CONFLICT** for duplicate SKU
   - Detailed logging of creation process

5. **update(Long id, ProductDto dto)** - PUT /api/v1/products/{id}
   - Throws ProductNotFoundException if product not found
   - HTTP Status: **200 OK** on success
   - HTTP Status: **404 NOT FOUND** if product doesn't exist
   - HTTP Status: **400 BAD REQUEST** for invalid data

6. **adjustInventory(String sku, int delta)** - PATCH /api/v1/products/{sku}/inventory
   - Returns JSON response with success/error status
   - HTTP Status: **200 OK** on success
   - HTTP Status: **404 NOT FOUND** if product not found
   - Detailed logging of inventory operations

7. **delete(Long id)** - DELETE /api/v1/products/{id}
   - Throws ProductNotFoundException if not found
   - Returns success message with product ID
   - HTTP Status: **200 OK** on success
   - HTTP Status: **404 NOT FOUND** if product doesn't exist

### GlobalExceptionHandler (ProductController)

Handles all exceptions with standardized ErrorResponse:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Product not found with id: 5",
  "timestamp": "2026-02-20T15:30:00",
  "path": "/api/v1/products/5",
  "validationErrors": null
}
```

**Handled Exceptions:**
- ProductNotFoundException → 404
- ProductAlreadyExistsException → 409
- InvalidProductDataException → 400
- InventoryException → 400-500 (configurable)
- MethodArgumentNotValidException → 400 with field-level errors
- IllegalArgumentException → 400
- All other exceptions → 500 INTERNAL_SERVER_ERROR

---

## Order-Management-Service Error Handling

### Exception Hierarchy

#### 1. **OrderException** (Base Exception)
- Base class for all order-related exceptions
- Customizable HTTP status codes

#### 2. **OrderNotFoundException**
- Thrown when an order is not found by ID or order number
- HTTP Status: **404 NOT FOUND**

#### 3. **CustomerNotFoundException**
- Thrown when a customer is not found
- HTTP Status: **404 NOT FOUND**

#### 4. **InvalidOrderDataException**
- Thrown when order/customer data validation fails
- HTTP Status: **400 BAD REQUEST**

#### 5. **InvalidOrderStateException**
- Thrown when attempting invalid order state transitions
- HTTP Status: **400 BAD REQUEST**
- Example: Cannot transition order from CANCELLED to CONFIRMED

#### 6. **ExternalServiceException**
- Thrown when external service calls fail
- HTTP Status: **503 SERVICE UNAVAILABLE** (default)

### OrderService Methods with Error Handling

1. **createOrder(CreateOrderRequest request)**
   - Validates: request not null, customerId valid, items not empty
   - Validates each item: SKU not blank, quantity > 0, unitPrice > 0
   - Throws CustomerNotFoundException if customer not found
   - Logs order creation with order number
   - Returns OrderDto with generated order number
   - Triggers async processing

2. **processOrderAsync(Long orderId)**
   - Safely processes orders asynchronously
   - Validates orderId is positive
   - Logs async processing events
   - Handles external service calls gracefully
   - Updates order status to CONFIRMED

3. **findAll()**
   - Returns all orders with error handling
   - Logs fetch operations
   - Throws OrderException on database errors

4. **findByCustomer(String customerId)**
   - Validates customerId not blank
   - Returns orders sorted by creation date descending
   - Logs retrieval operations

5. **findByOrderNumber(String orderNumber)**
   - Validates order number not blank
   - Returns Optional<OrderDto>
   - Logs retrieval operations

6. **findById(Long id)**
   - Validates ID is positive
   - Returns Optional<OrderDto>
   - Logs retrieval operations

7. **updateStatus(Long id, OrderStatusEnum status)**
   - Validates ID and status not null
   - Validates state transitions (e.g., cannot change CANCELLED orders)
   - Logs status changes
   - Throws InvalidOrderStateException for invalid transitions
   - Returns Optional<OrderDto>

8. **cancel(Long id)**
   - Validates ID is positive
   - Prevents canceling already-cancelled orders
   - Logs cancellation
   - Throws InvalidOrderStateException for invalid states
   - Returns Optional<OrderDto>

### OrderController Methods with Error Handling

1. **create(CreateOrderRequest request)** - POST /api/v1/orders
   - HTTP Status: **201 CREATED** on success
   - HTTP Status: **400 BAD REQUEST** for invalid data
   - HTTP Status: **404 NOT FOUND** if customer not found
   - Detailed logging of creation

2. **getByOrderNumber(String orderNumber)** - GET /api/v1/orders/number/{orderNumber}
   - Throws OrderNotFoundException if not found
   - HTTP Status: **404 NOT FOUND** if order doesn't exist
   - HTTP Status: **200 OK** on success

3. **getById(Long id)** - GET /api/v1/orders/{id}
   - Throws OrderNotFoundException if not found
   - HTTP Status: **404 NOT FOUND** if order doesn't exist
   - HTTP Status: **200 OK** on success

4. **listOrders(String customerId)** - GET /api/v1/orders
   - Returns all orders or filters by customerId
   - HTTP Status: **200 OK** on success
   - HTTP Status: **400 BAD REQUEST** for invalid customer ID
   - Requires ADMIN role

5. **updateStatus(Long id, OrderStatusEnum status)** - PATCH /api/v1/orders/{id}/status/{status}
   - Throws OrderNotFoundException if not found
   - Throws InvalidOrderStateException for invalid transitions
   - HTTP Status: **200 OK** on success
   - HTTP Status: **404 NOT FOUND** if order doesn't exist
   - HTTP Status: **400 BAD REQUEST** for invalid state
   - Requires ADMIN role

6. **cancel(Long id)** - POST /api/v1/orders/{id}/cancel
   - Throws OrderNotFoundException if not found
   - Throws InvalidOrderStateException if already cancelled
   - HTTP Status: **200 OK** on success
   - HTTP Status: **404 NOT FOUND** if order doesn't exist
   - HTTP Status: **400 BAD REQUEST** if already cancelled

### CustomerService Methods with Error Handling

1. **findAll()**
   - Returns all customers with error handling
   - Logs fetch operations
   - Throws OrderException on errors

2. **findById(Long id)**
   - Validates ID is positive
   - Returns Optional<Customer>
   - Logs retrieval

3. **findByEmail(String email)**
   - Validates email not blank
   - Returns Optional<Customer>
   - Logs retrieval

4. **create(Customer customer)**
   - Validates: customer not null, email not blank, firstName not blank, lastName not blank
   - Logs creation
   - Throws InvalidOrderDataException for validation failures
   - Returns created Customer

5. **update(Long id, Customer customer)**
   - Validates ID and all customer fields
   - Logs updates
   - Returns Optional<Customer>

6. **deleteById(Long id)**
   - Validates ID is positive
   - Throws CustomerNotFoundException if not found
   - Logs deletion

### CustomerController Methods with Error Handling

1. **getAllCustomers()** - GET /api/v1/customers
   - Requires ADMIN role
   - HTTP Status: **200 OK** on success
   - HTTP Status: **500** on error

2. **getCustomerById(Long id)** - GET /api/v1/customers/{id}
   - Throws CustomerNotFoundException if not found
   - HTTP Status: **200 OK** on success
   - HTTP Status: **404 NOT FOUND** if not found

3. **getCustomerByEmail(String email)** - GET /api/v1/customers/email/{email}
   - Throws CustomerNotFoundException if not found
   - HTTP Status: **200 OK** on success
   - HTTP Status: **404 NOT FOUND** if not found

4. **createCustomer(Customer customer)** - POST /api/v1/customers
   - HTTP Status: **201 CREATED** on success
   - HTTP Status: **400 BAD REQUEST** for invalid data
   - Detailed validation logging

5. **updateCustomer(Long id, Customer customer)** - PUT /api/v1/customers/{id}
   - Throws CustomerNotFoundException if not found
   - HTTP Status: **200 OK** on success
   - HTTP Status: **404 NOT FOUND** if not found
   - Requires ADMIN role

6. **deleteCustomer(Long id)** - DELETE /api/v1/customers/{id}
   - Throws CustomerNotFoundException if not found
   - HTTP Status: **204 NO CONTENT** on success
   - HTTP Status: **404 NOT FOUND** if not found
   - Requires ADMIN role

### GlobalExceptionHandler (OrderController)

Handles all exceptions with standardized ErrorResponse:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Order not found with id: 10",
  "timestamp": "2026-02-20T15:30:00",
  "path": "/api/v1/orders/10",
  "validationErrors": null
}
```

**Handled Exceptions:**
- OrderNotFoundException → 404
- CustomerNotFoundException → 404
- InvalidOrderDataException → 400
- InvalidOrderStateException → 400
- ExternalServiceException → 503
- MethodArgumentNotValidException → 400 with field-level errors
- IllegalArgumentException → 400
- All other exceptions → 500 INTERNAL_SERVER_ERROR

---

## Error Response Format

Both services use a standardized ErrorResponse record:

```java
public record ErrorResponse(
    int status,
    String error,
    String message,
    String timestamp,
    String path,
    Map<String, String> validationErrors
)
```

### Example Responses

**404 Not Found:**
```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Product not found with id: 999",
  "timestamp": "2026-02-20T15:35:22.123456",
  "path": "/api/v1/products/999",
  "validationErrors": null
}
```

**400 Bad Request with Validation Errors:**
```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2026-02-20T15:35:22.123456",
  "path": "/api/v1/products",
  "validationErrors": {
    "price": "must be greater than 0",
    "name": "must not be blank"
  }
}
```

**409 Conflict:**
```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Product with SKU 'SKU-001' already exists",
  "timestamp": "2026-02-20T15:35:22.123456",
  "path": "/api/v1/products",
  "validationErrors": null
}
```

**503 Service Unavailable:**
```json
{
  "status": 503,
  "error": "SERVICE_UNAVAILABLE",
  "message": "Failed to connect to Product Catalog service",
  "timestamp": "2026-02-20T15:35:22.123456",
  "path": "/api/v1/orders",
  "validationErrors": null
}
```

---

## Logging Strategy

### Log Levels Used:
- **INFO**: Successful operations (GET, POST, PUT, DELETE, PATCH)
- **WARN**: Business logic failures (not found, validation failures, state transitions)
- **ERROR**: System errors and exceptions

### Log Messages Include:
- HTTP method and endpoint
- Operation details (ID, SKU, order number, email, etc.)
- Success/failure status
- Error reasons
- Stack traces for system errors

---

## HTTP Status Codes Summary

| Status | Code | Usage |
|--------|------|-------|
| **200** | OK | Successful GET, PUT, PATCH, DELETE |
| **201** | CREATED | Successful POST (new resource) |
| **204** | NO CONTENT | Successful DELETE (no response body) |
| **400** | BAD REQUEST | Invalid input data, validation failures |
| **404** | NOT FOUND | Resource not found |
| **409** | CONFLICT | Duplicate resource (e.g., duplicate SKU) |
| **500** | INTERNAL SERVER ERROR | Unexpected system errors |
| **503** | SERVICE UNAVAILABLE | External service failures |

---

## Input Validation

### Product-Catalog-Service Validations:
- **SKU**: Not null/blank, unique across products
- **Name**: Not null/blank
- **Price**: Must be > 0
- **Quantity**: Must be >= 0
- **Inventory Delta**: Can be negative (deduction) or positive (addition)

### Order-Management-Service Validations:
- **Customer ID**: Must be positive, customer must exist
- **Order Items**: Must have at least one item per order
- **Item SKU**: Not null/blank
- **Item Quantity**: Must be > 0
- **Item Unit Price**: Must be > 0
- **Order Status**: Valid enum value
- **State Transitions**: Cannot change cancelled orders
- **Customer Fields**: Email unique, first/last names required

---

## Files Modified/Created

### Product-Catalog-Service
**Exception Classes:**
- `ProductException.java` - Base exception
- `ProductNotFoundException.java` - 404 errors
- `ProductAlreadyExistsException.java` - 409 conflicts
- `InvalidProductDataException.java` - 400 validation errors
- `InventoryException.java` - Inventory operation failures
- `ErrorResponse.java` - Standard error response DTO
- `GlobalExceptionHandler.java` - Global exception handler

**Updated Service Classes:**
- `ProductService.java` - Added comprehensive error handling to all 7 methods

**Updated Controller Classes:**
- `ProductController.java` - Added error handling to all 7 endpoints

### Order-Management-Service
**Exception Classes:**
- `OrderException.java` - Base exception
- `OrderNotFoundException.java` - 404 errors
- `CustomerNotFoundException.java` - 404 errors
- `InvalidOrderDataException.java` - 400 validation errors
- `InvalidOrderStateException.java` - Invalid state transitions
- `ExternalServiceException.java` - External service failures
- `ErrorResponse.java` - Standard error response DTO
- `GlobalExceptionHandler.java` - Global exception handler

**New Service Class:**
- `CustomerService.java` - Service layer with error handling for customer operations

**Updated Service Classes:**
- `OrderService.java` - Added comprehensive error handling to all 8 methods

**Updated Controller Classes:**
- `OrderController.java` - Added error handling to all 6 endpoints
- `CustomerController.java` - Updated to use CustomerService with error handling

---

## Testing Recommendations

### Test Scenarios to Cover:

**Product-Catalog-Service:**
1. Create product with valid data → 201 CREATED
2. Create product with duplicate SKU → 409 CONFLICT
3. Create product with invalid price → 400 BAD REQUEST
4. Get product by valid ID → 200 OK
5. Get product by invalid ID → 404 NOT FOUND
6. Update non-existent product → 404 NOT FOUND
7. Adjust inventory for non-existent SKU → 404 NOT FOUND
8. Delete non-existent product → 404 NOT FOUND

**Order-Management-Service:**
1. Create order with valid data → 201 CREATED
2. Create order with non-existent customer → 404 NOT FOUND
3. Create order with empty items → 400 BAD REQUEST
4. Get order by valid ID → 200 OK
5. Get order by invalid ID → 404 NOT FOUND
6. Cancel non-existent order → 404 NOT FOUND
7. Cancel already-cancelled order → 400 BAD REQUEST (INVALID_STATE)
8. Update order status to invalid state → 400 BAD REQUEST (INVALID_STATE)
9. Create customer with valid data → 201 CREATED
10. Update non-existent customer → 404 NOT FOUND
11. Delete non-existent customer → 404 NOT FOUND

---

## Conclusion

All methods in both product-catalog-service and order-management-service now have comprehensive error handling with:
- ✅ Meaningful error messages
- ✅ Appropriate HTTP status codes
- ✅ Detailed logging
- ✅ Input validation
- ✅ Standardized error responses
- ✅ Exception hierarchy for different error types
- ✅ Global exception handlers
- ✅ Async error handling

The implementation ensures consistent error handling across all APIs and provides clear feedback to API consumers.
