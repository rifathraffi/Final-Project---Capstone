package com.example.productcatalog.service;

import com.example.productcatalog.exception.InvalidProductDataException;
import com.example.productcatalog.exception.InventoryException;
import com.example.productcatalog.exception.ProductAlreadyExistsException;
import com.example.productcatalog.exception.ProductException;
import com.example.productcatalog.exception.ProductNotFoundException;
import com.example.productcatalog.model.InventoryError;
import com.example.productcatalog.model.InventoryResult;
import com.example.productcatalog.model.InventorySuccess;
import com.example.productcatalog.model.Product;
import com.example.productcatalog.model.ProductDto;
import com.example.productcatalog.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

	private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

	private static final String INVENTORY_LOG_TEMPLATE = """
		Inventory update for SKU: %s | Previous: %d | New: %d | Operation: %s
		""";

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<ProductDto> findAll() {
		try {
			logger.info("Fetching all products");
			List<ProductDto> products = productRepository.findAll().stream()
					.map(this::toDto)
					.collect(Collectors.toList());
			logger.info("Successfully fetched {} products", products.size());
			return products;
		} catch (Exception e) {
			logger.error("Error fetching all products", e);
			throw new ProductException("Failed to fetch products: " + e.getMessage(), 500, e);
		}
	}

	public Optional<ProductDto> findById(Long id) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidProductDataException("Product ID must be a positive number");
			}
			logger.info("Fetching product with id: {}", id);
			Optional<ProductDto> product = productRepository.findById(id).map(this::toDto);
			if (product.isPresent()) {
				logger.info("Product found with id: {}", id);
			} else {
				logger.warn("Product not found with id: {}", id);
			}
			return product;
		} catch (InvalidProductDataException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error fetching product with id: {}", id, e);
			throw new ProductException("Failed to fetch product: " + e.getMessage(), 500, e);
		}
	}

	public Optional<ProductDto> findBySku(String sku) {
		try {
			if (sku == null || sku.isBlank()) {
				throw new InvalidProductDataException("SKU cannot be null or empty");
			}
			logger.info("Fetching product with sku: {}", sku);
			Optional<ProductDto> product = productRepository.findBySku(sku).map(this::toDto);
			if (product.isPresent()) {
				logger.info("Product found with sku: {}", sku);
			} else {
				logger.warn("Product not found with sku: {}", sku);
			}
			return product;
		} catch (InvalidProductDataException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error fetching product with sku: {}", sku, e);
			throw new ProductException("Failed to fetch product: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public ProductDto create(ProductDto dto) {
		try {
			if (dto == null) {
				throw new InvalidProductDataException("Product data cannot be null");
			}
			if (dto.sku() == null || dto.sku().isBlank()) {
				throw new InvalidProductDataException("SKU cannot be null or empty");
			}
			if (dto.name() == null || dto.name().isBlank()) {
				throw new InvalidProductDataException("Product name cannot be null or empty");
			}
			if (dto.price() == null || dto.price().compareTo(java.math.BigDecimal.ZERO) <= 0) {
				throw new InvalidProductDataException("Product price must be greater than 0");
			}
			if (dto.quantity() < 0) {
				throw new InvalidProductDataException("Product quantity cannot be negative");
			}

			logger.info("Creating product with SKU: {}", dto.sku());

			if (productRepository.existsBySku(dto.sku())) {
				logger.warn("Product with SKU {} already exists", dto.sku());
				throw new ProductAlreadyExistsException(dto.sku());
			}

			Product product = new Product(
					dto.name(),
					dto.description() != null ? dto.description() : "",
					dto.price(),
					dto.quantity(),
					dto.sku()
			);
			Product saved = productRepository.save(product);
			logger.info("Product created successfully with id: {} and SKU: {}", saved.getId(), dto.sku());
			return toDto(saved);
		} catch (ProductAlreadyExistsException e) {
			throw e;
		} catch (InvalidProductDataException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error creating product with SKU: {}", dto != null ? dto.sku() : "unknown", e);
			throw new ProductException("Failed to create product: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public Optional<ProductDto> update(Long id, ProductDto dto) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidProductDataException("Product ID must be a positive number");
			}
			if (dto == null) {
				throw new InvalidProductDataException("Product data cannot be null");
			}
			if (dto.name() == null || dto.name().isBlank()) {
				throw new InvalidProductDataException("Product name cannot be null or empty");
			}
			if (dto.price() == null || dto.price().compareTo(java.math.BigDecimal.ZERO) <= 0) {
				throw new InvalidProductDataException("Product price must be greater than 0");
			}
			if (dto.quantity() < 0) {
				throw new InvalidProductDataException("Product quantity cannot be negative");
			}

			logger.info("Updating product with id: {}", id);

			Optional<ProductDto> result = productRepository.findById(id)
					.map(p -> {
						p.setName(dto.name());
						p.setDescription(dto.description() != null ? dto.description() : "");
						p.setPrice(dto.price());
						p.setQuantity(dto.quantity());
						Product saved = productRepository.save(p);
						logger.info("Product updated successfully with id: {}", id);
						return toDto(saved);
					});

			if (result.isEmpty()) {
				logger.warn("Product not found with id: {}", id);
			}

			return result;
		} catch (InvalidProductDataException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error updating product with id: {}", id, e);
			throw new ProductException("Failed to update product: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public InventoryResult adjustInventory(String sku, int delta) {
		try {
			if (sku == null || sku.isBlank()) {
				throw new InventoryException("SKU cannot be null or empty");
			}

			logger.info("Adjusting inventory for SKU: {} with delta: {}", sku, delta);

			return productRepository.findBySku(sku)
					.map(p -> {
						int prev = p.getQuantity();
						int next = Math.max(0, prev + delta);
						p.setQuantity(next);
						Product saved = productRepository.save(p);
						logger.info(INVENTORY_LOG_TEMPLATE.formatted(sku, prev, next, delta >= 0 ? "ADD" : "REMOVE"));
						return (InventoryResult) new InventorySuccess(sku, next);
					})
					.orElseThrow(() -> {
						logger.warn("Product not found with SKU: {} for inventory adjustment", sku);
						return new InventoryException("Product not found with SKU: " + sku, 404);
					});
		} catch (InventoryException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error adjusting inventory for SKU: {}", sku, e);
			throw new InventoryException("Failed to adjust inventory: " + e.getMessage(), 500, e);
		}
	}

	@Transactional
	public void deleteById(Long id) {
		try {
			if (id == null || id <= 0) {
				throw new InvalidProductDataException("Product ID must be a positive number");
			}

			logger.info("Deleting product with id: {}", id);

			if (!productRepository.existsById(id)) {
				logger.warn("Product not found with id: {}", id);
				throw new ProductNotFoundException(id);
			}

			productRepository.deleteById(id);
			logger.info("Product deleted successfully with id: {}", id);
		} catch (ProductNotFoundException e) {
			throw e;
		} catch (InvalidProductDataException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error deleting product with id: {}", id, e);
			throw new ProductException("Failed to delete product: " + e.getMessage(), 500, e);
		}
	}

	private ProductDto toDto(Product p) {
		return new ProductDto(
				p.getId(),
				p.getName(),
				p.getDescription(),
				p.getPrice(),
				p.getQuantity(),
				p.getSku()
		);
	}
}
