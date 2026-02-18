package com.example.productcatalog.controller;
import com.example.productcatalog.model.InventoryError;
import com.example.productcatalog.model.InventorySuccess;
import com.example.productcatalog.model.ProductDto;
import com.example.productcatalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

	@Mock
	private ProductService productService;

	@InjectMocks
	private ProductController productController;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;
	private ProductDto testProductDto;

	@BeforeEach
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
		objectMapper = new ObjectMapper();

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
	public void testListAllProducts() throws Exception {
		List<ProductDto> products = Arrays.asList(testProductDto);
		when(productService.findAll()).thenReturn(products);

		mockMvc.perform(get("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name").value("Test Product"))
				.andExpect(jsonPath("$[0].sku").value("TEST-SKU-001"))
				.andExpect(jsonPath("$[0].price").value(99.99));

		verify(productService, times(1)).findAll();
	}

	@Test
	public void testListAllProductsEmpty() throws Exception {
		when(productService.findAll()).thenReturn(Arrays.asList());

		mockMvc.perform(get("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		verify(productService, times(1)).findAll();
	}

	@Test
	public void testGetProductById() throws Exception {
		when(productService.findById(1L)).thenReturn(Optional.of(testProductDto));

		mockMvc.perform(get("/api/v1/products/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Test Product"))
				.andExpect(jsonPath("$.sku").value("TEST-SKU-001"));

		verify(productService, times(1)).findById(1L);
	}

	@Test
	public void testGetProductByIdNotFound() throws Exception {
		when(productService.findById(999L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/v1/products/999")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(productService, times(1)).findById(999L);
	}

	@Test
	public void testGetProductBySku() throws Exception {
		when(productService.findBySku("TEST-SKU-001")).thenReturn(Optional.of(testProductDto));

		mockMvc.perform(get("/api/v1/products/sku/TEST-SKU-001")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Test Product"))
				.andExpect(jsonPath("$.sku").value("TEST-SKU-001"));

		verify(productService, times(1)).findBySku("TEST-SKU-001");
	}

	@Test
	public void testGetProductBySkuNotFound() throws Exception {
		when(productService.findBySku("NON-EXISTENT-SKU")).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/v1/products/sku/NON-EXISTENT-SKU")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(productService, times(1)).findBySku("NON-EXISTENT-SKU");
	}

	@Test
	public void testCreateProduct() throws Exception {
		when(productService.create(any(ProductDto.class))).thenReturn(testProductDto);

		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Test Product"));

		verify(productService, times(1)).create(any(ProductDto.class));
	}

	@Test
	public void testCreateProductDuplicateSku() throws Exception {
		when(productService.create(any(ProductDto.class)))
				.thenThrow(new IllegalArgumentException("Product with SKU TEST-SKU-001 already exists"));

		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isBadRequest());

		verify(productService, times(1)).create(any(ProductDto.class));
	}

	@Test
	public void testCreateProductValidationError() throws Exception {
		ProductDto invalidDto = new ProductDto(
				null,
				"",  // Empty name, should fail validation
				"Description",
				new BigDecimal("99.99"),
				10,
				"SKU-001"
		);

		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());

		verify(productService, never()).create(any(ProductDto.class));
	}

	@Test
	public void testUpdateProduct() throws Exception {
		ProductDto updateDto = new ProductDto(
				1L,
				"Updated Product",
				"Updated Description",
				new BigDecimal("149.99"),
				20,
				"TEST-SKU-001"
		);

		when(productService.update(1L, updateDto)).thenReturn(Optional.of(updateDto));

		mockMvc.perform(put("/api/v1/products/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Product"))
				.andExpect(jsonPath("$.price").value(149.99));

		verify(productService, times(1)).update(1L, updateDto);
	}

	@Test
	public void testUpdateProductNotFound() throws Exception {
		ProductDto updateDto = new ProductDto(
				1L,
				"Updated Product",
				"Updated Description",
				new BigDecimal("149.99"),
				20,
				"TEST-SKU-001"
		);

		when(productService.update(999L, updateDto)).thenReturn(Optional.empty());

		mockMvc.perform(put("/api/v1/products/999")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isNotFound());

		verify(productService, times(1)).update(999L, updateDto);
	}

	@Test
	public void testAdjustInventorySuccess() throws Exception {
		InventorySuccess success = new InventorySuccess("TEST-SKU-001", 15);
		when(productService.adjustInventory("TEST-SKU-001", 5)).thenReturn(success);

		mockMvc.perform(patch("/api/v1/products/TEST-SKU-001/inventory")
				.param("delta", "5")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
				.andExpect(jsonPath("$.quantity").value(15))
				.andExpect(jsonPath("$.status").value("success"));

		verify(productService, times(1)).adjustInventory("TEST-SKU-001", 5);
	}

	@Test
	public void testAdjustInventoryDecrement() throws Exception {
		InventorySuccess success = new InventorySuccess("TEST-SKU-001", 7);
		when(productService.adjustInventory("TEST-SKU-001", -3)).thenReturn(success);

		mockMvc.perform(patch("/api/v1/products/TEST-SKU-001/inventory")
				.param("delta", "-3")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(7));

		verify(productService, times(1)).adjustInventory("TEST-SKU-001", -3);
	}

	@Test
	public void testAdjustInventoryProductNotFound() throws Exception {
		InventoryError error = new InventoryError("NON-EXISTENT-SKU", "Product not found");
		when(productService.adjustInventory("NON-EXISTENT-SKU", 5)).thenReturn(error);

		mockMvc.perform(patch("/api/v1/products/NON-EXISTENT-SKU/inventory")
				.param("delta", "5")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.sku").value("NON-EXISTENT-SKU"))
				.andExpect(jsonPath("$.message").value("Product not found"))
				.andExpect(jsonPath("$.status").value("error"));

		verify(productService, times(1)).adjustInventory("NON-EXISTENT-SKU", 5);
	}

	@Test
	public void testDeleteProduct() throws Exception {
		doNothing().when(productService).deleteById(1L);

		mockMvc.perform(delete("/api/v1/products/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(productService, times(1)).deleteById(1L);
	}

	@Test
	public void testCreateProductWithMinimalFields() throws Exception {
		ProductDto minimalDto = new ProductDto(
				null,
				"Minimal Product",
				null,
				new BigDecimal("10.00"),
				1,
				"MIN-SKU-001"
		);

		when(productService.create(any(ProductDto.class))).thenReturn(
				new ProductDto(
						2L,
						"Minimal Product",
						null,
						new BigDecimal("10.00"),
						1,
						"MIN-SKU-001"
				)
		);

		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(minimalDto)))
				.andExpect(status().isCreated());

		verify(productService, times(1)).create(any(ProductDto.class));
	}
}
