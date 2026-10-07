package com.edstem.sample.service;

import com.edstem.sample.dto.request.PlaceOrderRequest;
import com.edstem.sample.dto.response.OrderResponse;
import com.edstem.sample.entity.CustomerOrder;
import com.edstem.sample.entity.OrderItem;
import com.edstem.sample.entity.OrderStatus;
import com.edstem.sample.entity.Product;
import com.edstem.sample.exception.BusinessRuleException;
import com.edstem.sample.exception.InsufficientStockException;
import com.edstem.sample.exception.ResourceNotFoundException;
import com.edstem.sample.mapper.OrderMapper;
import com.edstem.sample.repository.CustomerOrderRepository;
import com.edstem.sample.repository.ProductRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class OrderProcessor {

  private final CustomerOrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ProductService productService;
  private final OrderMapper orderMapper;

  public OrderResponse create(String idempotencyKey, PlaceOrderRequest request) {
    Map<UUID, Integer> quantities = request.quantitiesByProduct();
    Map<UUID, Product> products =
        productRepository.findAllById(quantities.keySet()).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

    CustomerOrder order = orderMapper.toEntity(idempotencyKey);
    order.addItems(
        quantities.entrySet().stream()
            .map(entry -> orderMapper.toItem(productOf(products, entry.getKey()), entry.getValue()))
            .toList());
    CustomerOrder saved = orderRepository.saveAndFlush(order);

    quantities.forEach((productId, quantity) -> reserve(products.get(productId), quantity));
    return orderMapper.toResponse(saved);
  }

  public OrderResponse cancel(UUID orderId) {
    CustomerOrder order =
        orderRepository
            .findWithItemsById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(CustomerOrder.class, orderId));
    if (orderRepository.markCancelled(orderId) == 0) {
      throw new BusinessRuleException("ORDER_NOT_CANCELLABLE", "Order is already cancelled");
    }
    order.getItems().forEach(this::release);
    order.setStatus(OrderStatus.CANCELLED);
    return orderMapper.toResponse(order);
  }

  private static Product productOf(Map<UUID, Product> products, UUID productId) {
    return Optional.ofNullable(products.get(productId))
        .orElseThrow(() -> new ResourceNotFoundException(Product.class, productId));
  }

  private void reserve(Product product, int quantity) {
    if (productRepository.reserveStock(product.getId(), quantity) == 0) {
      throw new InsufficientStockException(product.getId(), product.getName(), quantity);
    }
    productService.evict(product.getId());
  }

  private void release(OrderItem item) {
    productRepository.releaseStock(item.getProductId(), item.getQuantity());
    productService.evict(item.getProductId());
  }
}
