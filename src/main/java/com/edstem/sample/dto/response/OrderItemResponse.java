package com.edstem.sample.dto.response;

import java.util.UUID;

public record OrderItemResponse(UUID productId, int quantity, long unitPriceCents) {}
