package com.example.order.service;

import com.example.order.exception.CustomerNotFoundException;
import com.example.order.exception.InvalidOrderDataException;
import com.example.order.exception.OrderException;
import com.example.order.model.Customer;
import com.example.order.repository.CustomerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing customers with comprehensive error handling
 */
@Service
public class CustomerService {

	private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public List<Customer> findAll() {
		try {
			log.info("Fetching all customers");
			List<Customer> customers = customerRepository.findAll();
			log.info("Successfully fetched {} customers", customers.size());
			return customers;
		} catch (Exception e) {
			log.error("Error fetching all customers", e);
			throw new OrderException("Failed to fetch customers: " + e.getMessage(), 500, e);
		}
	}

	@Transactional(readOnly = true)
	public Optional<Customer> findById(Long id) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidOrderDataException("Customer ID must be a positive number");
			}

			log.info("Fetching customer with id: {}", id);

			Optional<Customer> customer = customerRepository.findById(id);
			if (customer.isPresent()) {
				log.info("Customer found with id: {}", id);
			} else {
				log.warn("Customer not found with id: {}", id);
			}
			return customer;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching customer with id: {}", id, e);
			throw new OrderException("Failed to fetch customer: " + e.getMessage(), 500, e);
		}
	}

	@Transactional(readOnly = true)
	public Optional<Customer> findByEmail(String email) {
		try {
			if (email == null || email.isBlank()) {
				throw new InvalidOrderDataException("Email cannot be null or empty");
			}

			log.info("Fetching customer with email: {}", email);

			Optional<Customer> customer = customerRepository.findByEmail(email);
			if (customer.isPresent()) {
				log.info("Customer found with email: {}", email);
			} else {
				log.warn("Customer not found with email: {}", email);
			}
			return customer;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching customer with email: {}", email, e);
			throw new OrderException("Failed to fetch customer: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public Customer create(Customer customer) {
		try {
			if (customer == null) {
				throw new InvalidOrderDataException("Customer data cannot be null");
			}
			if (customer.getEmail() == null || customer.getEmail().isBlank()) {
				throw new InvalidOrderDataException("Customer email cannot be null or empty");
			}
			if (customer.getFirstName() == null || customer.getFirstName().isBlank()) {
				throw new InvalidOrderDataException("Customer first name cannot be null or empty");
			}
			if (customer.getLastName() == null || customer.getLastName().isBlank()) {
				throw new InvalidOrderDataException("Customer last name cannot be null or empty");
			}

			log.info("Creating customer with email: {}", customer.getEmail());

			Customer saved = customerRepository.save(customer);
			log.info("Customer created successfully with id: {} and email: {}", saved.getId(), saved.getEmail());
			return saved;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creating customer", e);
			throw new OrderException("Failed to create customer: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public Optional<Customer> update(Long id, Customer customer) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidOrderDataException("Customer ID must be a positive number");
			}
			if (customer == null) {
				throw new InvalidOrderDataException("Customer data cannot be null");
			}
			if (customer.getEmail() == null || customer.getEmail().isBlank()) {
				throw new InvalidOrderDataException("Customer email cannot be null or empty");
			}
			if (customer.getFirstName() == null || customer.getFirstName().isBlank()) {
				throw new InvalidOrderDataException("Customer first name cannot be null or empty");
			}
			if (customer.getLastName() == null || customer.getLastName().isBlank()) {
				throw new InvalidOrderDataException("Customer last name cannot be null or empty");
			}

			log.info("Updating customer with id: {}", id);

			Optional<Customer> result = customerRepository.findById(id)
					.map(existing -> {
						customer.setId(id);
						Customer saved = customerRepository.save(customer);
						log.info("Customer updated successfully with id: {}", id);
						return saved;
					});

			if (result.isEmpty()) {
				log.warn("Customer not found with id: {} for update", id);
			}

			return result;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error updating customer with id: {}", id, e);
			throw new OrderException("Failed to update customer: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public void deleteById(Long id) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidOrderDataException("Customer ID must be a positive number");
			}

			log.info("Deleting customer with id: {}", id);

			if (!customerRepository.existsById(id)) {
				log.warn("Customer not found with id: {}", id);
				throw new CustomerNotFoundException(id);
			}

			customerRepository.deleteById(id);
			log.info("Customer deleted successfully with id: {}", id);
		} catch (CustomerNotFoundException e) {
			throw e;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error deleting customer with id: {}", id, e);
			throw new OrderException("Failed to delete customer: " + e.getMessage(), 500, e);
		}
	}
}
