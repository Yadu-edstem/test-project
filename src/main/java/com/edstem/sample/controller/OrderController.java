package com.edstem.sample.controller;

import com.edstem.sample.dto.ApiResponse;
import com.edstem.sample.dto.request.PlaceOrderRequest;
import com.edstem.sample.dto.response.CreationResult;
import com.edstem.sample.dto.response.OrderResponse;
import com.edstem.sample.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "All-or-nothing orders with idempotent retries")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

  private final OrderService orderService;

  @Operation(
      summary = "Place an order",
      description =
          "Send a client-generated Idempotency-Key (e.g. a UUID) per logical order. Retrying with"
              + " the same key returns the original order with 200 instead of creating a new one.")
  @PostMapping
  public ResponseEntity<ApiResponse<OrderResponse>> place(
      @RequestHeader(IDEMPOTENCY_KEY_HEADER)
          @NotBlank(message = "Idempotency-Key must not be blank")
          @Size(max = 100, message = "Idempotency-Key must be at most 100 characters")
          String idempotencyKey,
      @Valid @RequestBody PlaceOrderRequest request) {
    CreationResult<OrderResponse> result = orderService.place(idempotencyKey.trim(), request);
    return ResponseEntity.status(result.status()).body(ApiResponse.success(result.value()));
  }

  @Operation(summary = "Get an order")
  @GetMapping("/{id}")
  public ApiResponse<OrderResponse> get(@PathVariable UUID id) {
    return ApiResponse.success(orderService.get(id));
  }

  @Operation(summary = "Cancel an order and return its stock")
  @PostMapping("/{id}/cancel")
  public ApiResponse<OrderResponse> cancel(@PathVariable UUID id) {
    return ApiResponse.success(orderService.cancel(id));
  }
}
