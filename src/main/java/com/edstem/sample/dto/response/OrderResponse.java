package com.edstem.sample.dto.response;

import com.edstem.sample.entity.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String idempotencyKey,
    OrderStatus status,
    long totalCents,
    List<OrderItemResponse> items,
    Instant createdAt) {}
