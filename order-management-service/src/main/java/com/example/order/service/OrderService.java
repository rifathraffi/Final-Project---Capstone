package com.example.order.service;

import com.example.order.client.ProductCatalogClient;
import com.example.order.exception.CustomerNotFoundException;
import com.example.order.exception.ExternalServiceException;
import com.example.order.exception.InvalidOrderDataException;
import com.example.order.exception.InvalidOrderStateException;
import com.example.order.exception.OrderException;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.model.OrderEntity;
import com.example.order.model.OrderEntity.OrderStatusEnum;
import com.example.order.model.OrderItem;
import com.example.order.model.CreateOrderRequest;
import com.example.order.model.OrderDto;
import com.example.order.model.OrderItemDto;
import com.example.order.model.Customer;
import com.example.order.repository.OrderRepository;
import com.example.order.repository.CustomerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	private static final String ORDER_ASYNC_LOG = """
		[ASYNC] Order lifecycle event:
		Order: %s | Status: %s | Total: %s | Customer: %s
		""";

	private final OrderRepository orderRepository;
	private final CustomerRepository customerRepository;
	private final ProductCatalogClient productCatalogClient;

	public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository, ProductCatalogClient productCatalogClient) {
		this.orderRepository = orderRepository;
		this.customerRepository = customerRepository;
		this.productCatalogClient = productCatalogClient;
	}

	@Transactional
	public OrderDto createOrder(CreateOrderRequest request) {
		try {
			if (request == null) {
				throw new InvalidOrderDataException("Order request cannot be null");
			}
			if (request.customerId() == null || request.customerId() <= 0) {
				throw new InvalidOrderDataException("Customer ID cannot be null or must be a positive number");
			}
			if (request.items() == null || request.items().isEmpty()) {
				throw new InvalidOrderDataException("Order must contain at least one item");
			}

			log.info("Creating order for customer: {}", request.customerId());

			// Fetch the customer from the database
			Customer customer = customerRepository.findById(request.customerId())
					.orElseThrow(() -> {
						log.warn("Customer not found with ID: {}", request.customerId());
						return new CustomerNotFoundException(request.customerId());
					});
			
			String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
			OrderEntity order = new OrderEntity(orderNumber, customer);

			BigDecimal total = BigDecimal.ZERO;
			for (var itemReq : request.items()) {
				if (itemReq.sku() == null || itemReq.sku().isBlank()) {
					throw new InvalidOrderDataException("Order item SKU cannot be null or empty");
				}
				if (itemReq.quantity() <= 0) {
					throw new InvalidOrderDataException("Order item quantity must be greater than 0");
				}
				if (itemReq.unitPrice() == null || itemReq.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
					throw new InvalidOrderDataException("Order item unit price must be greater than 0");
				}

				OrderItem item = new OrderItem(itemReq.sku(), itemReq.quantity(), itemReq.unitPrice());
				order.addItem(item);
				total = total.add(item.getSubtotal());
			}
			order.setTotalAmount(total);

			OrderEntity saved = orderRepository.save(order);
			log.info("Order created successfully with order number: {} and id: {}", orderNumber, saved.getId());
			processOrderAsync(saved.getId());
			return toDto(saved);
		} catch (InvalidOrderDataException | CustomerNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creating order", e);
			throw new OrderException("Failed to create order: " + e.getMessage(), 500, e);
		}
	}

	@Async
	public CompletableFuture<Void> processOrderAsync(Long orderId) {
		return CompletableFuture.runAsync(() -> {
			try {
				if (orderId == null || orderId <= 0) {
					log.warn("Invalid order ID for async processing: {}", orderId);
					return;
				}

				Optional<OrderEntity> opt = orderRepository.findById(orderId);
				if (opt.isEmpty()) {
					log.warn("Order not found with id: {} for async processing", orderId);
					return;
				}

				OrderEntity order = opt.get();
				log.info(ORDER_ASYNC_LOG.formatted(
						order.getOrderNumber(),
						order.getStatus(),
						order.getTotalAmount(),
						order.getCustomerId()));

				for (OrderItem item : order.getItems()) {
					try {
						productCatalogClient.getProductBySku(item.getSku())
								.doOnNext(p -> log.info("Product verified: {} qty={}", p.sku(), p.quantity()))
								.doOnError(e -> log.warn("Product check failed for SKU {}: {}", item.getSku(), e.getMessage()))
								.subscribe();
					} catch (Exception e) {
						log.warn("Failed to verify product with SKU: {} during async processing", item.getSku(), e);
					}
				}

				orderRepository.findById(orderId).ifPresent(o -> {
					o.setStatus(OrderStatusEnum.CONFIRMED);
					orderRepository.save(o);
					log.info("Order {} confirmed asynchronously", o.getOrderNumber());
				});
			} catch (Exception e) {
				log.error("Async order processing failed for orderId={}", orderId, e);
			}
		});
	}

	@Transactional(readOnly = true)
	public List<OrderDto> findAll() {
		try {
			log.info("Fetching all orders");
			List<OrderDto> orders = orderRepository.findAll().stream()
					.map(this::toDto)
					.collect(Collectors.toList());
			log.info("Successfully fetched {} orders", orders.size());
			return orders;
		} catch (Exception e) {
			log.error("Error fetching all orders", e);
			throw new OrderException("Failed to fetch orders: " + e.getMessage(), 500, e);
		}
	}

	@Transactional(readOnly = true)
	public List<OrderDto> findByCustomer(String customerId) {
		try {
			if (customerId == null || customerId.isBlank()) {
				throw new InvalidOrderDataException("Customer ID cannot be null or empty");
			}

			log.info("Fetching orders for customer: {}", customerId);

			List<OrderDto> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
					.map(this::toDto)
					.collect(Collectors.toList());
			log.info("Successfully fetched {} orders for customer: {}", orders.size(), customerId);
			return orders;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching orders for customer: {}", customerId, e);
			throw new OrderException("Failed to fetch orders for customer: " + e.getMessage(), 500, e);
		}
	}

	@Transactional(readOnly = true)
	public Optional<OrderDto> findByOrderNumber(String orderNumber) {
		try {
			if (orderNumber == null || orderNumber.isBlank()) {
				throw new InvalidOrderDataException("Order number cannot be null or empty");
			}

			log.info("Fetching order with order number: {}", orderNumber);

			Optional<OrderDto> order = orderRepository.findByOrderNumber(orderNumber).map(this::toDto);
			if (order.isPresent()) {
				log.info("Order found with order number: {}", orderNumber);
			} else {
				log.warn("Order not found with order number: {}", orderNumber);
			}
			return order;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching order with order number: {}", orderNumber, e);
			throw new OrderException("Failed to fetch order: " + e.getMessage(), 500, e);
		}
	}

	@Transactional(readOnly = true)
	public Optional<OrderDto> findById(Long id) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidOrderDataException("Order ID must be a positive number");
			}

			log.info("Fetching order with id: {}", id);

			Optional<OrderDto> order = orderRepository.findById(id).map(this::toDto);
			if (order.isPresent()) {
				log.info("Order found with id: {}", id);
			} else {
				log.warn("Order not found with id: {}", id);
			}
			return order;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching order with id: {}", id, e);
			throw new OrderException("Failed to fetch order: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public Optional<OrderDto> updateStatus(Long id, OrderStatusEnum status) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidOrderDataException("Order ID must be a positive number");
			}
			if (status == null) {
				throw new InvalidOrderDataException("Order status cannot be null");
			}

			log.info("Updating order status for id: {} to: {}", id, status);

			Optional<OrderDto> result = orderRepository.findById(id)
					.map(o -> {
						OrderStatusEnum currentStatus = o.getStatus();
						
						// Validate state transition
						if (currentStatus == OrderStatusEnum.CANCELLED) {
							log.warn("Cannot change status of cancelled order");
							throw new InvalidOrderStateException(currentStatus.name(), status.name());
						}

						o.setStatus(status);
						OrderEntity saved = orderRepository.save(o);
						log.info("Order status updated successfully for id: {} from: {} to: {}", id, currentStatus, status);
						return toDto(saved);
					});

			if (result.isEmpty()) {
				log.warn("Order not found with id: {} for status update", id);
			}

			return result;
		} catch (InvalidOrderDataException | InvalidOrderStateException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error updating order status for id: {}", id, e);
			throw new OrderException("Failed to update order status: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public Optional<OrderDto> cancel(Long id) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidOrderDataException("Order ID must be a positive number");
			}

			log.info("Cancelling order with id: {}", id);

			Optional<OrderDto> result = orderRepository.findById(id)
					.map(o -> {
						OrderStatusEnum currentStatus = o.getStatus();
						
						// Cannot cancel if already in terminal state (except CONFIRMED which can be cancelled)
						if (currentStatus == OrderStatusEnum.CANCELLED) {
							log.warn("Order is already cancelled");
							throw new InvalidOrderStateException(currentStatus.name(), OrderStatusEnum.CANCELLED.name());
						}

						o.setStatus(OrderStatusEnum.CANCELLED);
						OrderEntity saved = orderRepository.save(o);
						log.info("Order cancelled successfully with id: {}", id);
						return toDto(saved);
					});

			if (result.isEmpty()) {
				log.warn("Order not found with id: {} for cancellation", id);
			}

			return result;
		} catch (InvalidOrderStateException e) {
			throw e;
		} catch (InvalidOrderDataException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error cancelling order with id: {}", id, e);
			throw new OrderException("Failed to cancel order: " + e.getMessage(), 500, e);
		}
	}

	private OrderDto toDto(OrderEntity o) {
		List<OrderItemDto> itemDtos = o.getItems().stream()
				.map(i -> new OrderItemDto(i.getId(), i.getSku(), i.getQuantity(), i.getUnitPrice()))
				.collect(Collectors.toList());
		return new OrderDto(
				o.getId(),
				o.getOrderNumber(),
				o.getCustomerId(),
				o.getStatus().name(),
				o.getTotalAmount(),
				o.getCreatedAt(),
				o.getUpdatedAt(),
				itemDtos
		);
	}
}
