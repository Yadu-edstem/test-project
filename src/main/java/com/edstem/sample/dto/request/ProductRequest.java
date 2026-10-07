package com.edstem.sample.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters")
        String name,
    @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Category must be at most 100 characters")
        String category,
    @NotNull(message = "Price is required") @PositiveOrZero(message = "Price cannot be negative")
        Long priceCents,
    @NotNull(message = "Stock is required") @PositiveOrZero(message = "Stock cannot be negative")
        Integer stock,
    @DecimalMin(value = "0.0", message = "Rating must be between 0 and 5")
        @DecimalMax(value = "5.0", message = "Rating must be between 0 and 5")
        Double rating) {}
