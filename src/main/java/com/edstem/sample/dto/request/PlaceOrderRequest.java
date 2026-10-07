package com.edstem.sample.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public record PlaceOrderRequest(
    @NotEmpty(message = "An order needs at least one item")
        @Size(max = 50, message = "An order can have at most 50 items")
        List<@Valid OrderItemRequest> items) {

  public Map<UUID, Integer> quantitiesByProduct() {
    return items.stream()
        .collect(
            Collectors.toMap(
                OrderItemRequest::productId,
                OrderItemRequest::quantity,
                Integer::sum,
                TreeMap::new));
  }
}
