package com.edstem.sample.mapper;

import com.edstem.sample.dto.response.OrderItemResponse;
import com.edstem.sample.dto.response.OrderResponse;
import com.edstem.sample.entity.CustomerOrder;
import com.edstem.sample.entity.OrderItem;
import com.edstem.sample.entity.OrderStatus;
import com.edstem.sample.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

  public CustomerOrder toEntity(String idempotencyKey) {
    return CustomerOrder.builder()
        .idempotencyKey(idempotencyKey)
        .status(OrderStatus.PLACED)
        .totalCents(0L)
        .build();
  }

  public OrderItem toItem(Product product, int quantity) {
    return OrderItem.builder()
        .productId(product.getId())
        .quantity(quantity)
        .unitPriceCents(product.getPriceCents())
        .build();
  }

  public OrderResponse toResponse(CustomerOrder order) {
    return new OrderResponse(
        order.getId(),
        order.getIdempotencyKey(),
        order.getStatus(),
        order.getTotalCents(),
        order.getItems().stream().map(this::toItemResponse).toList(),
        order.getCreatedAt());
  }

  private OrderItemResponse toItemResponse(OrderItem item) {
    return new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getUnitPriceCents());
  }
}
