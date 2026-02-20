package com.example.productcatalog;

import com.example.productcatalog.config.TestApplication;
import com.example.productcatalog.config.TestSecurityConfig;
import com.example.productcatalog.model.ProductDto;
import com.example.productcatalog.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class})
@WithMockUser(username = "test", roles = {"ADMIN"})
public class ProductCatalogIntegrationTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private ProductDto testProductDto;

	@BeforeEach
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		productRepository.deleteAll();

		testProductDto = new ProductDto(
				null,
				"Integration Test Product",
				"A product for integration testing",
				new BigDecimal("99.99"),
				50,
				"INT-TEST-SKU-001"
		);
	}

	@Test
	public void testCreateProduct() throws Exception {
		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value("Integration Test Product"))
				.andExpect(jsonPath("$.sku").value("INT-TEST-SKU-001"));
	}

	@Test
	public void testGetAllProducts() throws Exception {
		// Create a product first
		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated());

		// Get all products
		mockMvc.perform(get("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$[0].name").value("Integration Test Product"));
	}

	@Test
	public void testGetProductById() throws Exception {
		// Create a product
		var createResponse = mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andReturn();

		String responseBody = createResponse.getResponse().getContentAsString();
		Long productId = objectMapper.readTree(responseBody).get("id").asLong();

		// Get the product by ID
		mockMvc.perform(get("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(productId))
				.andExpect(jsonPath("$.name").value("Integration Test Product"));
	}

	@Test
	public void testGetProductBySku() throws Exception {
		// Create a product
		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated());

		// Get product by SKU
		mockMvc.perform(get("/api/v1/products/sku/INT-TEST-SKU-001")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Integration Test Product"))
				.andExpect(jsonPath("$.sku").value("INT-TEST-SKU-001"));
	}

	@Test
	public void testUpdateProduct() throws Exception {
		// Create a product
		var createResponse = mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andReturn();

		String responseBody = createResponse.getResponse().getContentAsString();
		Long productId = objectMapper.readTree(responseBody).get("id").asLong();

		// Update the product
		ProductDto updateDto = new ProductDto(
				productId,
				"Updated Integration Test Product",
				"Updated description",
				new BigDecimal("149.99"),
				100,
				"INT-TEST-SKU-001"
		);

		mockMvc.perform(put("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Integration Test Product"))
				.andExpect(jsonPath("$.price").value(149.99))
				.andExpect(jsonPath("$.quantity").value(100));
	}

	@Test
	public void testDeleteProduct() throws Exception {
		// Create a product
		var createResponse = mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andReturn();

		String responseBody = createResponse.getResponse().getContentAsString();
		Long productId = objectMapper.readTree(responseBody).get("id").asLong();

		// Delete the product
		mockMvc.perform(delete("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		// Verify product is deleted
		mockMvc.perform(get("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testAdjustInventoryIncrement() throws Exception {
		// Create a product
		var createResponse = mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andReturn();

		String responseBody = createResponse.getResponse().getContentAsString();
		String sku = objectMapper.readTree(responseBody).get("sku").asText();

		// Adjust inventory (add)
		mockMvc.perform(patch("/api/v1/products/" + sku + "/inventory")
				.param("delta", "10")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"))
				.andExpect(jsonPath("$.quantity").value(60))
				.andExpect(jsonPath("$.sku").value(sku));
	}

	@Test
	public void testAdjustInventoryDecrement() throws Exception {
		// Create a product
		var createResponse = mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andReturn();

		String responseBody = createResponse.getResponse().getContentAsString();
		String sku = objectMapper.readTree(responseBody).get("sku").asText();

		// Adjust inventory (remove)
		mockMvc.perform(patch("/api/v1/products/" + sku + "/inventory")
				.param("delta", "-20")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"))
				.andExpect(jsonPath("$.quantity").value(30));
	}

	@Test
	public void testAdjustInventoryBelowZero() throws Exception {
		// Create a product
		var createResponse = mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andReturn();

		String responseBody = createResponse.getResponse().getContentAsString();
		String sku = objectMapper.readTree(responseBody).get("sku").asText();

		// Adjust inventory below zero
		mockMvc.perform(patch("/api/v1/products/" + sku + "/inventory")
				.param("delta", "-100")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"))
				.andExpect(jsonPath("$.quantity").value(0));
	}

	@Test
	public void testCreateProductWithDuplicateSku() throws Exception {
		// Create first product
		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated());

		// Try to create second product with same SKU (should return 409 CONFLICT)
		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value("CONFLICT"));
	}

	@Test
	public void testCreateAndRetrieveMultipleProducts() throws Exception {
		// Create first product
		ProductDto product1 = new ProductDto(
				null,
				"Product 1",
				"Description 1",
				new BigDecimal("50.00"),
				10,
				"SKU-001"
		);

		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(product1)))
				.andExpect(status().isCreated());

		// Create second product
		ProductDto product2 = new ProductDto(
				null,
				"Product 2",
				"Description 2",
				new BigDecimal("75.00"),
				20,
				"SKU-002"
		);

		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(product2)))
				.andExpect(status().isCreated());

		// Get all products
		mockMvc.perform(get("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
	}

	@Test
	public void testFullProductLifecycle() throws Exception {
		// 1. Create product
		var createResponse = mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testProductDto)))
				.andExpect(status().isCreated())
				.andReturn();

		String responseBody = createResponse.getResponse().getContentAsString();
		Long productId = objectMapper.readTree(responseBody).get("id").asLong();
		String sku = objectMapper.readTree(responseBody).get("sku").asText();

		// 2. Get product by ID
		mockMvc.perform(get("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(productId));

		// 3. Update product
		ProductDto updateDto = new ProductDto(
				productId,
				"Lifecycle Product Updated",
				"Updated during lifecycle",
				new BigDecimal("199.99"),
				75,
				sku
		);

		mockMvc.perform(put("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk());

		// 4. Adjust inventory
		mockMvc.perform(patch("/api/v1/products/" + sku + "/inventory")
				.param("delta", "25")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(100));

		// 5. Get updated product
		mockMvc.perform(get("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Lifecycle Product Updated"));

		// 6. Delete product
		mockMvc.perform(delete("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		// 7. Verify product is deleted
		mockMvc.perform(get("/api/v1/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testInvalidProductCreation() throws Exception {
		// Try to create product with invalid data
		String invalidProductJson = "{\"name\":\"\", \"sku\":\"\"}";

		mockMvc.perform(post("/api/v1/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidProductJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testUpdateNonExistentProduct() throws Exception {
		ProductDto updateDto = new ProductDto(
				999L,
				"Non Existent Product",
				"This product does not exist",
				new BigDecimal("99.99"),
				10,
				"NON-EXISTENT"
		);

		mockMvc.perform(put("/api/v1/products/999")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testAdjustInventoryForNonExistentProduct() throws Exception {
		mockMvc.perform(patch("/api/v1/products/NON-EXISTENT-SKU/inventory")
				.param("delta", "5")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("INVENTORY_ERROR"));
	}
}
