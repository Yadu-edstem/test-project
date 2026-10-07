package com.edstem.sample.mapper;

import com.edstem.sample.dto.request.ProductRequest;
import com.edstem.sample.dto.response.ProductResponse;
import com.edstem.sample.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  public Product toEntity(ProductRequest request) {
    return apply(request, new Product());
  }

  public Product apply(ProductRequest request, Product product) {
    product.setName(request.name().trim());
    product.setCategory(request.category().trim());
    product.setPriceCents(request.priceCents());
    product.setStock(request.stock());
    product.setRating(request.rating());
    return product;
  }

  public ProductResponse toResponse(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getCategory(),
        product.getPriceCents(),
        product.getStock(),
        product.getRating(),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }
}
