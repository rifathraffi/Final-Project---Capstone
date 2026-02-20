package com.example.productcatalog.service;

import com.example.productcatalog.exception.InventoryException;
import com.example.productcatalog.exception.ProductAlreadyExistsException;
import com.example.productcatalog.exception.ProductNotFoundException;
import com.example.productcatalog.model.InventoryError;
import com.example.productcatalog.model.InventoryResult;
import com.example.productcatalog.model.InventorySuccess;
import com.example.productcatalog.model.Product;
import com.example.productcatalog.model.ProductDto;
import com.example.productcatalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
//import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	private Product testProduct;
	private ProductDto testProductDto;

	@BeforeEach
	public void setUp() {
		testProduct = new Product(
				"Test Product",
				"A test product description",
				new BigDecimal("99.99"),
				10,
				"TEST-SKU-001"
		);
		testProduct.setId(1L);

		testProductDto = new ProductDto(
				1L,
				"Test Product",
				"A test product description",
				new BigDecimal("99.99"),
				10,
				"TEST-SKU-001"
		);
	}

	@Test
	public void testFindAll() {
		List<Product> products = Arrays.asList(testProduct);
		when(productRepository.findAll()).thenReturn(products);

		List<ProductDto> result = productService.findAll();

		assertEquals(1, result.size());
		assertEquals("Test Product", result.get(0).name());
		assertEquals("TEST-SKU-001", result.get(0).sku());
		verify(productRepository, times(1)).findAll();
	}

	@Test
	public void testFindAllEmpty() {
		when(productRepository.findAll()).thenReturn(Arrays.asList());

		List<ProductDto> result = productService.findAll();

		assertTrue(result.isEmpty());
		verify(productRepository, times(1)).findAll();
	}

	@Test
	public void testFindById() {
		when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

		Optional<ProductDto> result = productService.findById(1L);

		assertTrue(result.isPresent());
		assertEquals("Test Product", result.get().name());
		assertEquals(new BigDecimal("99.99"), result.get().price());
		verify(productRepository, times(1)).findById(1L);
	}

	@Test
	public void testFindByIdNotFound() {
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		Optional<ProductDto> result = productService.findById(999L);

		assertFalse(result.isPresent());
		verify(productRepository, times(1)).findById(999L);
	}

	@Test
	public void testFindBySku() {
		when(productRepository.findBySku("TEST-SKU-001")).thenReturn(Optional.of(testProduct));

		Optional<ProductDto> result = productService.findBySku("TEST-SKU-001");

		assertTrue(result.isPresent());
		assertEquals("Test Product", result.get().name());
		assertEquals("TEST-SKU-001", result.get().sku());
		verify(productRepository, times(1)).findBySku("TEST-SKU-001");
	}

	@Test
	public void testFindBySkuNotFound() {
		when(productRepository.findBySku("NON-EXISTENT-SKU")).thenReturn(Optional.empty());

		Optional<ProductDto> result = productService.findBySku("NON-EXISTENT-SKU");

		assertFalse(result.isPresent());
		verify(productRepository, times(1)).findBySku("NON-EXISTENT-SKU");
	}

	@Test
	public void testCreateProduct() {
		when(productRepository.existsBySku("TEST-SKU-001")).thenReturn(false);
		when(productRepository.save(any(Product.class))).thenReturn(testProduct);

		ProductDto result = productService.create(testProductDto);

		assertNotNull(result);
		assertEquals("Test Product", result.name());
		assertEquals("TEST-SKU-001", result.sku());
		verify(productRepository, times(1)).existsBySku("TEST-SKU-001");
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	public void testCreateProductSkuAlreadyExists() {
		when(productRepository.existsBySku("TEST-SKU-001")).thenReturn(true);

		assertThrows(ProductAlreadyExistsException.class, () -> {
			productService.create(testProductDto);
		});

		verify(productRepository, times(1)).existsBySku("TEST-SKU-001");
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	public void testCreateProductWithNullDescription() {
		ProductDto dtoWithoutDescription = new ProductDto(
				null,
				"Test Product",
				null,
				new BigDecimal("99.99"),
				10,
				"NEW-SKU-001"
		);

		when(productRepository.existsBySku("NEW-SKU-001")).thenReturn(false);
		when(productRepository.save(any(Product.class))).thenReturn(testProduct);

		ProductDto result = productService.create(dtoWithoutDescription);

		assertNotNull(result);
		verify(productRepository, times(1)).existsBySku("NEW-SKU-001");
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	public void testUpdateProduct() {
		ProductDto updateDto = new ProductDto(
				1L,
				"Updated Product",
				"Updated Description",
				new BigDecimal("149.99"),
				20,
				"TEST-SKU-001"
		);

		Product updatedProduct = new Product(
				"Updated Product",
				"Updated Description",
				new BigDecimal("149.99"),
				20,
				"TEST-SKU-001"
		);
		updatedProduct.setId(1L);

		when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
		when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

		Optional<ProductDto> result = productService.update(1L, updateDto);

		assertTrue(result.isPresent());
		assertEquals("Updated Product", result.get().name());
		assertEquals(new BigDecimal("149.99"), result.get().price());
		verify(productRepository, times(1)).findById(1L);
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	public void testUpdateProductNotFound() {
		ProductDto updateDto = new ProductDto(
				1L,
				"Updated Product",
				"Updated Description",
				new BigDecimal("149.99"),
				20,
				"TEST-SKU-001"
		);

		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		Optional<ProductDto> result = productService.update(999L, updateDto);

		assertFalse(result.isPresent());
		verify(productRepository, times(1)).findById(999L);
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	public void testAdjustInventorySuccess() {
		when(productRepository.findBySku("TEST-SKU-001")).thenReturn(Optional.of(testProduct));
		when(productRepository.save(any(Product.class))).thenReturn(testProduct);

		InventoryResult result = productService.adjustInventory("TEST-SKU-001", 5);

		assertInstanceOf(InventorySuccess.class, result);
		InventorySuccess success = (InventorySuccess) result;
		assertEquals("TEST-SKU-001", success.sku());
		assertEquals(15, success.newQuantity());
		verify(productRepository, times(1)).findBySku("TEST-SKU-001");
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	public void testAdjustInventoryDecrement() {
		testProduct.setQuantity(10);
		when(productRepository.findBySku("TEST-SKU-001")).thenReturn(Optional.of(testProduct));
		when(productRepository.save(any(Product.class))).thenReturn(testProduct);

		InventoryResult result = productService.adjustInventory("TEST-SKU-001", -3);

		assertInstanceOf(InventorySuccess.class, result);
		InventorySuccess success = (InventorySuccess) result;
		assertEquals("TEST-SKU-001", success.sku());
		assertEquals(7, success.newQuantity());
		verify(productRepository, times(1)).findBySku("TEST-SKU-001");
	}

	@Test
	public void testAdjustInventoryBelowZero() {
		testProduct.setQuantity(5);
		when(productRepository.findBySku("TEST-SKU-001")).thenReturn(Optional.of(testProduct));
		when(productRepository.save(any(Product.class))).thenReturn(testProduct);

		InventoryResult result = productService.adjustInventory("TEST-SKU-001", -10);

		assertInstanceOf(InventorySuccess.class, result);
		InventorySuccess success = (InventorySuccess) result;
		assertEquals("TEST-SKU-001", success.sku());
		assertEquals(0, success.newQuantity());
		verify(productRepository, times(1)).findBySku("TEST-SKU-001");
	}

	@Test
	public void testAdjustInventoryProductNotFound() {
		when(productRepository.findBySku("NON-EXISTENT-SKU")).thenReturn(Optional.empty());

		assertThrows(InventoryException.class, () -> {
			productService.adjustInventory("NON-EXISTENT-SKU", 5);
		});

		verify(productRepository, times(1)).findBySku("NON-EXISTENT-SKU");
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	public void testDeleteById() {
		when(productRepository.existsById(1L)).thenReturn(true);
		doNothing().when(productRepository).deleteById(1L);

		productService.deleteById(1L);

		verify(productRepository, times(1)).existsById(1L);
		verify(productRepository, times(1)).deleteById(1L);
	}

	@Test
	public void testDeleteByIdNotFound() {
		when(productRepository.existsById(999L)).thenReturn(false);

		assertThrows(ProductNotFoundException.class, () -> {
			productService.deleteById(999L);
		});

		verify(productRepository, times(1)).existsById(999L);
		verify(productRepository, never()).deleteById(999L);
	}

	@Test
	public void testProductDtoMapping() {
		ProductDto dto = productService.findById(1L).orElse(null);

		// This is to verify that the mapping works correctly
		// The actual test is done in other tests that verify the conversion
		verify(productRepository, times(1)).findById(1L);
	}
}
