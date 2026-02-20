package com.example.productcatalog.controller;

import com.example.productcatalog.exception.InvalidProductDataException;
import com.example.productcatalog.exception.InventoryException;
import com.example.productcatalog.exception.ProductNotFoundException;
import com.example.productcatalog.model.InventoryError;
import com.example.productcatalog.model.InventoryResult;
import com.example.productcatalog.model.InventorySuccess;
import com.example.productcatalog.model.ProductDto;
import com.example.productcatalog.service.ProductService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<List<ProductDto>> list() {
		try {
			logger.info("GET /api/v1/products - Fetching all products");
			List<ProductDto> products = productService.findAll();
			logger.info("Successfully fetched {} products", products.size());
			return ResponseEntity.ok(products);
		} catch (Exception e) {
			logger.error("Error fetching all products", e);
			throw e;
		}
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
		try {
			logger.info("GET /api/v1/products/{} - Fetching product by id", id);
			return productService.findById(id)
					.map(product -> {
						logger.info("Product found with id: {}", id);
						return ResponseEntity.ok(product);
					})
					.orElseThrow(() -> new ProductNotFoundException(id));
		} catch (ProductNotFoundException e) {
			logger.warn("Product not found with id: {}", id);
			throw e;
		} catch (Exception e) {
			logger.error("Error fetching product with id: {}", id, e);
			throw e;
		}
	}

	@GetMapping("/sku/{sku}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ProductDto> getBySku(@PathVariable String sku) {
		try {
			logger.info("GET /api/v1/products/sku/{} - Fetching product by sku", sku);
			return productService.findBySku(sku)
					.map(product -> {
						logger.info("Product found with sku: {}", sku);
						return ResponseEntity.ok(product);
					})
					.orElseThrow(() -> new ProductNotFoundException("sku", sku));
		} catch (ProductNotFoundException e) {
			logger.warn("Product not found with sku: {}", sku);
			throw e;
		} catch (Exception e) {
			logger.error("Error fetching product with sku: {}", sku, e);
			throw e;
		}
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
		try {
			logger.info("POST /api/v1/products - Creating new product with SKU: {}", dto.sku());
			ProductDto created = productService.create(dto);
			logger.info("Product created successfully with id: {} and SKU: {}", created.id(), created.sku());
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (InvalidProductDataException | IllegalArgumentException e) {
			logger.warn("Invalid product data provided: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error creating product", e);
			throw e;
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
		try {
			logger.info("PUT /api/v1/products/{} - Updating product", id);
			return productService.update(id, dto)
					.map(updated -> {
						logger.info("Product updated successfully with id: {}", id);
						return ResponseEntity.ok(updated);
					})
					.orElseThrow(() -> new ProductNotFoundException(id));
		} catch (ProductNotFoundException e) {
			logger.warn("Product not found for update with id: {}", id);
			throw e;
		} catch (InvalidProductDataException e) {
			logger.warn("Invalid product data provided: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error updating product with id: {}", id, e);
			throw e;
		}
	}

	@PatchMapping("/{sku}/inventory")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> adjustInventory(
			@PathVariable String sku,
			@RequestParam int delta
	) {
		try {
			logger.info("PATCH /api/v1/products/{}/inventory - Adjusting inventory with delta: {}", sku, delta);
			InventoryResult result = productService.adjustInventory(sku, delta);

			if (result instanceof InventorySuccess s) {
				logger.info("Inventory adjusted successfully for SKU: {} with new quantity: {}", sku, s.newQuantity());
				return ResponseEntity.ok(Map.of(
						"sku", s.sku(),
						"quantity", s.newQuantity(),
						"status", "success"
				));
			}

			InventoryError e = (InventoryError) result;
			logger.warn("Inventory adjustment failed for SKU: {}", sku);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
					"sku", e.sku(),
					"message", e.message(),
					"status", "error"
			));
		} catch (InventoryException e) {
			logger.warn("Inventory exception for SKU: {}", sku);
			throw e;
		} catch (Exception e) {
			logger.error("Error adjusting inventory for SKU: {}", sku, e);
			throw e;
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
		try {
			logger.info("DELETE /api/v1/products/{} - Deleting product", id);
			productService.deleteById(id);
			logger.info("Product deleted successfully with id: {}", id);
			return ResponseEntity.ok(Map.of(
					"id", id,
					"message", "Product deleted successfully",
					"status", "success"
			));
		} catch (ProductNotFoundException e) {
			logger.warn("Product not found for deletion with id: {}", id);
			throw e;
		} catch (Exception e) {
			logger.error("Error deleting product with id: {}", id, e);
			throw e;
		}
	}
}
