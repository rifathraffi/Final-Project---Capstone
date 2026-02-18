package com.example.productcatalog.repository;

import com.example.productcatalog.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;

	private Product testProduct;

	@BeforeEach
	public void setUp() {
		testProduct = new Product(
				"Test Product",
				"A test product description",
				new BigDecimal("99.99"),
				10,
				"TEST-SKU-001"
		);
	}

	@Test
	public void testSaveProduct() {
		Product savedProduct = productRepository.save(testProduct);

		assertNotNull(savedProduct.getId());
		assertEquals("Test Product", savedProduct.getName());
		assertEquals("TEST-SKU-001", savedProduct.getSku());
		assertEquals(new BigDecimal("99.99"), savedProduct.getPrice());
		assertEquals(10, savedProduct.getQuantity());
	}

	@Test
	public void testFindProductById() {
		Product savedProduct = productRepository.save(testProduct);

		Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

		assertTrue(foundProduct.isPresent());
		assertEquals("Test Product", foundProduct.get().getName());
		assertEquals(savedProduct.getId(), foundProduct.get().getId());
	}

	@Test
	public void testFindProductByIdNotFound() {
		Optional<Product> foundProduct = productRepository.findById(999L);

		assertFalse(foundProduct.isPresent());
	}

	@Test
	public void testFindAllProducts() {
		Product product1 = new Product("Product 1", "Description 1", new BigDecimal("10.00"), 5, "SKU-001");
		Product product2 = new Product("Product 2", "Description 2", new BigDecimal("20.00"), 10, "SKU-002");

		productRepository.save(product1);
		productRepository.save(product2);

		assertEquals(2, productRepository.findAll().size());
	}

	@Test
	public void testFindProductBySku() {
		productRepository.save(testProduct);

		Optional<Product> foundProduct = productRepository.findBySku("TEST-SKU-001");

		assertTrue(foundProduct.isPresent());
		assertEquals("Test Product", foundProduct.get().getName());
		assertEquals("TEST-SKU-001", foundProduct.get().getSku());
	}

	@Test
	public void testFindProductBySkuNotFound() {
		Optional<Product> foundProduct = productRepository.findBySku("NON-EXISTENT-SKU");

		assertFalse(foundProduct.isPresent());
	}

	@Test
	public void testExistsBySku() {
		productRepository.save(testProduct);

		assertTrue(productRepository.existsBySku("TEST-SKU-001"));
		assertFalse(productRepository.existsBySku("NON-EXISTENT-SKU"));
	}

	@Test
	public void testDeleteProduct() {
		Product savedProduct = productRepository.save(testProduct);
		Long id = savedProduct.getId();

		productRepository.deleteById(id);

		Optional<Product> deletedProduct = productRepository.findById(id);
		assertFalse(deletedProduct.isPresent());
	}

	@Test
	public void testUpdateProduct() {
		Product savedProduct = productRepository.save(testProduct);
		savedProduct.setName("Updated Product");
		savedProduct.setPrice(new BigDecimal("149.99"));
		savedProduct.setQuantity(20);

		Product updatedProduct = productRepository.save(savedProduct);

		assertEquals("Updated Product", updatedProduct.getName());
		assertEquals(new BigDecimal("149.99"), updatedProduct.getPrice());
		assertEquals(20, updatedProduct.getQuantity());
	}

	@Test
	public void testSkuUniqueness() {
		Product product1 = new Product("Product 1", "Description 1", new BigDecimal("10.00"), 5, "UNIQUE-SKU");
		productRepository.save(product1);

		Product product2 = new Product("Product 2", "Description 2", new BigDecimal("20.00"), 10, "UNIQUE-SKU");

		assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
			productRepository.save(product2);
		});
	}
}
