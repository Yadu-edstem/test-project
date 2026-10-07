package com.edstem.sample.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.edstem.sample.dto.request.ProductRequest;
import com.edstem.sample.dto.response.ProductResponse;
import com.edstem.sample.exception.ResourceNotFoundException;
import com.edstem.sample.repository.ProductRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
class ProductCacheTest {

  @Autowired private ProductService productService;
  @MockitoSpyBean private ProductRepository productRepository;

  private UUID productId;

  @BeforeEach
  void createProduct() {
    productId = productService.create(request("Cached lamp", 2500L, 5)).id();
    clearInvocations(productRepository);
  }

  @Test
  void repeatedLookupsHitTheDatabaseOnce() {
    ProductResponse first = productService.get(productId);
    ProductResponse second = productService.get(productId);
    ProductResponse third = productService.get(productId);

    verify(productRepository, times(1)).findById(productId);
    assertThat(second).isEqualTo(first).isEqualTo(third);
  }

  @Test
  void updateEvictsSoTheNextLookupIsFresh() {
    productService.get(productId);

    productService.update(productId, request("Cached lamp v2", 3000L, 7));
    ProductResponse afterUpdate = productService.get(productId);

    assertThat(afterUpdate.name()).isEqualTo("Cached lamp v2");
    assertThat(afterUpdate.priceCents()).isEqualTo(3000L);
  }

  @Test
  void deleteEvictsSoTheNextLookupIsNotFound() {
    productService.get(productId);

    productService.delete(productId);

    assertThrows(ResourceNotFoundException.class, () -> productService.get(productId));
  }

  private static ProductRequest request(String name, long priceCents, int stock) {
    return new ProductRequest(name, "Home", priceCents, stock, 4.0);
  }
}
