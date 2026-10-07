package com.edstem.sample.exception;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class InsufficientStockException extends ApplicationException {
  private static final String ERROR_CODE = "INSUFFICIENT_STOCK";

  private final UUID productId;
  private final int requested;

  public InsufficientStockException(UUID productId, String productName, int requested) {
    super(
        ERROR_CODE,
        "Insufficient stock for product '" + productName + "': requested " + requested,
        HttpStatus.CONFLICT);
    this.productId = productId;
    this.requested = requested;
  }

  @Override
  public Map<String, Object> getErrorData() {
    return Map.of("productId", productId, "requested", requested);
  }
}
