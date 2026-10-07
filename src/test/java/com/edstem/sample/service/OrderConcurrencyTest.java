package com.edstem.sample.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.sample.controller.OrderController;
import com.edstem.sample.dto.request.OrderItemRequest;
import com.edstem.sample.dto.request.PlaceOrderRequest;
import com.edstem.sample.dto.request.ProductRequest;
import com.edstem.sample.dto.response.CreationResult;
import com.edstem.sample.dto.response.OrderResponse;
import com.edstem.sample.entity.OrderStatus;
import com.edstem.sample.exception.BusinessRuleException;
import com.edstem.sample.exception.InsufficientStockException;
import com.edstem.sample.repository.CustomerOrderRepository;
import com.edstem.sample.support.Concurrently;
import com.edstem.sample.support.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class OrderConcurrencyTest {

  @Autowired private OrderService orderService;
  @Autowired private ProductService productService;
  @Autowired private CustomerOrderRepository orderRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void fiftySimultaneousOrdersForStockOfTenSellExactlyTen() throws Exception {
    UUID productId = productWithStock(10);

    var outcomes =
        Concurrently.run(
            50,
            index -> () -> orderService.place(UUID.randomUUID().toString(), order(productId, 1)));

    assertThat(outcomes.stream().filter(Concurrently.Outcome::succeeded)).hasSize(10);
    assertThat(outcomes.stream().filter(outcome -> !outcome.succeeded()))
        .hasSize(40)
        .allMatch(outcome -> outcome.error() instanceof InsufficientStockException);
    assertThat(productService.get(productId).stock()).isZero();
  }

  @Test
  void retryingWithTheSameKeyCreatesOneOrder() throws Exception {
    UUID productId = productWithStock(5);
    String key = UUID.randomUUID().toString();
    long ordersBefore = orderRepository.count();

    var outcomes =
        Concurrently.run(10, index -> () -> orderService.place(key, order(productId, 2)));

    List<CreationResult<OrderResponse>> results =
        outcomes.stream().map(Concurrently.Outcome::value).toList();
    assertThat(outcomes).allMatch(Concurrently.Outcome::succeeded);
    assertThat(results).filteredOn(CreationResult::newlyCreated).hasSize(1);
    assertThat(results)
        .extracting(result -> result.value().id())
        .containsOnly(results.get(0).value().id());
    assertThat(orderRepository.count()).isEqualTo(ordersBefore + 1);
    assertThat(productService.get(productId).stock()).isEqualTo(3);
  }

  @Test
  void orderIsAllOrNothingAndInsufficientStockReturns409() throws Exception {
    UUID plenty = productWithStock(10);
    UUID scarce = productWithStock(1);
    PlaceOrderRequest request =
        new PlaceOrderRequest(
            List.of(new OrderItemRequest(plenty, 3), new OrderItemRequest(scarce, 2)));

    mockMvc
        .perform(
            post("/api/v1/orders")
                .header(OrderController.IDEMPOTENCY_KEY_HEADER, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_STOCK"))
        .andExpect(jsonPath("$.error.message").exists());

    assertThat(productService.get(plenty).stock()).isEqualTo(10);
    assertThat(productService.get(scarce).stock()).isEqualTo(1);
  }

  @Test
  void missingIdempotencyKeyReturns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order(productWithStock(1), 1))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.details['Idempotency-Key'][0]").exists());
  }

  @Test
  void cancellingReturnsStockOnce() {
    UUID productId = productWithStock(4);
    OrderResponse placed =
        orderService.place(UUID.randomUUID().toString(), order(productId, 3)).value();

    OrderResponse cancelled = orderService.cancel(placed.id());

    assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
    assertThat(productService.get(productId).stock()).isEqualTo(4);
    assertThrows(BusinessRuleException.class, () -> orderService.cancel(placed.id()));
    assertThat(productService.get(productId).stock()).isEqualTo(4);
  }

  @Test
  void placingAnOrderEvictsTheCachedProduct() {
    UUID productId = productWithStock(6);
    assertThat(productService.get(productId).stock()).isEqualTo(6);

    orderService.place(UUID.randomUUID().toString(), order(productId, 2));

    assertThat(productService.get(productId).stock()).isEqualTo(4);
  }

  @Test
  void orderingAnUnknownProductReturns404() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/orders")
                .header(OrderController.IDEMPOTENCY_KEY_HEADER, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order(UUID.randomUUID(), 1))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
  }

  @Test
  void placedOrderCanBeFetched() throws Exception {
    UUID productId = productWithStock(3);
    OrderResponse placed =
        orderService.place(UUID.randomUUID().toString(), order(productId, 1)).value();

    mockMvc
        .perform(get("/api/v1/orders/" + placed.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("PLACED"))
        .andExpect(jsonPath("$.data.totalCents").value(1500))
        .andExpect(jsonPath("$.data.items[0].productId").value(productId.toString()));
    mockMvc.perform(get("/api/v1/orders/" + UUID.randomUUID())).andExpect(status().isNotFound());
  }

  private UUID productWithStock(int stock) {
    return productService
        .create(new ProductRequest("Limited item " + UUID.randomUUID(), "Toys", 1500L, stock, 4.5))
        .id();
  }

  private static PlaceOrderRequest order(UUID productId, int quantity) {
    return new PlaceOrderRequest(List.of(new OrderItemRequest(productId, quantity)));
  }
}
