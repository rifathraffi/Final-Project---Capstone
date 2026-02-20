package com.example.order.controller;

import com.example.order.exception.InvalidOrderDataException;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.model.CreateOrderRequest;
import com.example.order.model.OrderDto;
import com.example.order.model.OrderEntity.OrderStatusEnum;
import com.example.order.service.OrderService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
		try {
			logger.info("POST /api/v1/orders - Creating new order for customer: {}", request.customerId());
			OrderDto created = orderService.createOrder(request);
			logger.info("Order created successfully with order number: {}", created.orderNumber());
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (InvalidOrderDataException e) {
			logger.warn("Invalid order data provided: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error creating order", e);
			throw e;
		}
	}

	@GetMapping("/number/{orderNumber}")
	public ResponseEntity<OrderDto> getByOrderNumber(@PathVariable String orderNumber) {
		try {
			logger.info("GET /api/v1/orders/number/{} - Fetching order by order number", orderNumber);
			return orderService.findByOrderNumber(orderNumber)
					.map(order -> {
						logger.info("Order found with order number: {}", orderNumber);
						return ResponseEntity.ok(order);
					})
					.orElseThrow(() -> new OrderNotFoundException("order number", orderNumber));
		} catch (OrderNotFoundException e) {
			logger.warn("Order not found with order number: {}", orderNumber);
			throw e;
		} catch (Exception e) {
			logger.error("Error fetching order with order number: {}", orderNumber, e);
			throw e;
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
		try {
			logger.info("GET /api/v1/orders/{} - Fetching order by id", id);
			return orderService.findById(id)
					.map(order -> {
						logger.info("Order found with id: {}", id);
						return ResponseEntity.ok(order);
					})
					.orElseThrow(() -> new OrderNotFoundException(id));
		} catch (OrderNotFoundException e) {
			logger.warn("Order not found with id: {}", id);
			throw e;
		} catch (Exception e) {
			logger.error("Error fetching order with id: {}", id, e);
			throw e;
		}
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<OrderDto>> listOrders(@RequestParam(required = false) String customerId) {
		try {
			if (customerId != null && !customerId.isEmpty()) {
				logger.info("GET /api/v1/orders - Fetching orders for customer: {}", customerId);
				List<OrderDto> orders = orderService.findByCustomer(customerId);
				logger.info("Successfully fetched {} orders for customer: {}", orders.size(), customerId);
				return ResponseEntity.ok(orders);
			}
			logger.info("GET /api/v1/orders - Fetching all orders");
			List<OrderDto> orders = orderService.findAll();
			logger.info("Successfully fetched {} orders", orders.size());
			return ResponseEntity.ok(orders);
		} catch (InvalidOrderDataException e) {
			logger.warn("Invalid order data provided: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error listing orders", e);
			throw e;
		}
	}

	@PatchMapping("/{id}/status/{status}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<OrderDto> updateStatus(
			@PathVariable Long id,
			@PathVariable OrderStatusEnum status) {
		try {
			logger.info("PATCH /api/v1/orders/{}/status/{} - Updating order status", id, status);
			return orderService.updateStatus(id, status)
					.map(order -> {
						logger.info("Order status updated successfully for id: {} to: {}", id, status);
						return ResponseEntity.ok(order);
					})
					.orElseThrow(() -> new OrderNotFoundException(id));
		} catch (OrderNotFoundException e) {
			logger.warn("Order not found with id: {}", id);
			throw e;
		} catch (Exception e) {
			logger.error("Error updating order status for id: {}", id, e);
			throw e;
		}
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<OrderDto> cancel(@PathVariable Long id) {
		try {
			logger.info("POST /api/v1/orders/{}/cancel - Cancelling order", id);
			return orderService.cancel(id)
					.map(order -> {
						logger.info("Order cancelled successfully with id: {}", id);
						return ResponseEntity.ok(order);
					})
					.orElseThrow(() -> new OrderNotFoundException(id));
		} catch (OrderNotFoundException e) {
			logger.warn("Order not found with id: {}", id);
			throw e;
		} catch (Exception e) {
			logger.error("Error cancelling order with id: {}", id, e);
			throw e;
		}
	}
}
