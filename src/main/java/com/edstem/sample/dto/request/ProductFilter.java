package com.edstem.sample.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductFilter(
    @Size(max = 100, message = "Category must be at most 100 characters") String category,
    @PositiveOrZero(message = "Minimum price cannot be negative") Long minPriceCents,
    @PositiveOrZero(message = "Maximum price cannot be negative") Long maxPriceCents,
    Boolean inStock,
    @Size(max = 200, message = "Name search must be at most 200 characters") String name) {

  @AssertTrue(message = "Minimum price cannot exceed maximum price")
  public boolean isPriceRangeValid() {
    return minPriceCents == null || maxPriceCents == null || minPriceCents <= maxPriceCents;
  }
}
