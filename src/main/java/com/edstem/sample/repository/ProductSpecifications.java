package com.edstem.sample.repository;

import com.edstem.sample.dto.request.ProductFilter;
import com.edstem.sample.entity.Product;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProductSpecifications {

  private static final char LIKE_ESCAPE = '\\';

  private ProductSpecifications() {}

  public static Specification<Product> matching(ProductFilter filter) {
    return Specification.allOf(
        categoryIs(filter.category()),
        priceAtLeast(filter.minPriceCents()),
        priceAtMost(filter.maxPriceCents()),
        inStockOnly(filter.inStock()),
        nameContains(filter.name()));
  }

  private static Specification<Product> categoryIs(String category) {
    return StringUtils.hasText(category)
        ? (root, query, cb) ->
            cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase(Locale.ROOT))
        : null;
  }

  private static Specification<Product> priceAtLeast(Long minPriceCents) {
    return minPriceCents == null
        ? null
        : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceCents"), minPriceCents);
  }

  private static Specification<Product> priceAtMost(Long maxPriceCents) {
    return maxPriceCents == null
        ? null
        : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceCents"), maxPriceCents);
  }

  private static Specification<Product> inStockOnly(Boolean inStock) {
    return Boolean.TRUE.equals(inStock)
        ? (root, query, cb) -> cb.greaterThan(root.get("stock"), 0)
        : null;
  }

  private static Specification<Product> nameContains(String name) {
    return StringUtils.hasText(name)
        ? (root, query, cb) -> cb.like(cb.lower(root.get("name")), likePattern(name), LIKE_ESCAPE)
        : null;
  }

  private static String likePattern(String term) {
    String escaped =
        term.trim()
            .toLowerCase(Locale.ROOT)
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    return "%" + escaped + "%";
  }
}
