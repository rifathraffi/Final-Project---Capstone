package com.example.order.controller;

import com.example.order.exception.CustomerNotFoundException;
import com.example.order.exception.InvalidOrderDataException;
import com.example.order.model.Customer;
import com.example.order.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Get all customers - ADMIN only
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        try {
            logger.info("GET /api/v1/customers - Fetching all customers");
            List<Customer> customers = customerService.findAll();
            logger.info("Successfully fetched {} customers", customers.size());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            logger.error("Error fetching all customers", e);
            throw e;
        }
    }

    /**
     * Get customer by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        try {
            logger.info("GET /api/v1/customers/{} - Fetching customer by id", id);
            return customerService.findById(id)
                    .map(customer -> {
                        logger.info("Customer found with id: {}", id);
                        return ResponseEntity.ok(customer);
                    })
                    .orElseThrow(() -> new CustomerNotFoundException(id));
        } catch (CustomerNotFoundException e) {
            logger.warn("Customer not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching customer with id: {}", id, e);
            throw e;
        }
    }

    /**
     * Get customer by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        try {
            logger.info("GET /api/v1/customers/email/{} - Fetching customer by email", email);
            return customerService.findByEmail(email)
                    .map(customer -> {
                        logger.info("Customer found with email: {}", email);
                        return ResponseEntity.ok(customer);
                    })
                    .orElseThrow(() -> new CustomerNotFoundException("email", email));
        } catch (CustomerNotFoundException e) {
            logger.warn("Customer not found with email: {}", email);
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching customer with email: {}", email, e);
            throw e;
        }
    }

    /**
     * Create a new customer
     */
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        try {
            logger.info("POST /api/v1/customers - Creating new customer with email: {}", customer.getEmail());
            if (customer == null) {
                throw new InvalidOrderDataException("Customer data cannot be null");
            }
            Customer saved = customerService.create(customer);
            logger.info("Customer created successfully with id: {} and email: {}", saved.getId(), saved.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (InvalidOrderDataException e) {
            logger.warn("Invalid customer data provided: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating customer", e);
            throw e;
        }
    }

    /**
     * Update an existing customer - ADMIN only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer) {
        try {
            logger.info("PUT /api/v1/customers/{} - Updating customer", id);
            if (id == null || id <= 0) {
                throw new InvalidOrderDataException("Customer ID must be a positive number");
            }
            if (customer == null) {
                throw new InvalidOrderDataException("Customer data cannot be null");
            }

            return customerService.update(id, customer)
                    .map(updated -> {
                        logger.info("Customer updated successfully with id: {}", id);
                        return ResponseEntity.ok(updated);
                    })
                    .orElseThrow(() -> {
                        logger.warn("Customer not found with id: {} for update", id);
                        return new CustomerNotFoundException(id);
                    });
        } catch (InvalidOrderDataException | CustomerNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating customer with id: {}", id, e);
            throw e;
        }
    }

    /**
     * Delete a customer - ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            logger.info("DELETE /api/v1/customers/{} - Deleting customer", id);
            if (id == null || id <= 0) {
                throw new InvalidOrderDataException("Customer ID must be a positive number");
            }

            customerService.deleteById(id);
            logger.info("Customer deleted successfully with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (InvalidOrderDataException | CustomerNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting customer with id: {}", id, e);
            throw e;
        }
    }
}
