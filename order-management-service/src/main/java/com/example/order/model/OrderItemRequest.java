package com.example.order.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record OrderItemRequest(
	@NotBlank String sku,
	@Min(1) Integer quantity,
	@DecimalMin("0.01") BigDecimal unitPrice
) {}
