package com.edstem.sample.controller;

import com.edstem.sample.dto.ApiResponse;
import com.edstem.sample.dto.request.ProductFilter;
import com.edstem.sample.dto.request.ProductRequest;
import com.edstem.sample.dto.response.ProductResponse;
import com.edstem.sample.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products", description = "Product catalog with filtering, paging and cached lookups")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @Operation(
      summary = "List products",
      description =
          "Filters combine freely: category, minPriceCents, maxPriceCents, inStock, name. Sort by"
              + " any field, e.g. sort=priceCents,desc. Page size is capped at 100.")
  @GetMapping
  public ApiResponse<Page<ProductResponse>> list(
      @Valid @ModelAttribute ProductFilter filter,
      @PageableDefault(sort = "name") Pageable pageable) {
    return ApiResponse.success(productService.list(filter, pageable));
  }

  @Operation(summary = "Get a product (cached, evicted on change)")
  @GetMapping("/{id}")
  public ApiResponse<ProductResponse> get(@PathVariable UUID id) {
    return ApiResponse.success(productService.get(id));
  }

  @Operation(summary = "Create a product")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    return ApiResponse.success(productService.create(request));
  }

  @Operation(summary = "Replace a product")
  @PutMapping("/{id}")
  public ApiResponse<ProductResponse> update(
      @PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
    return ApiResponse.success(productService.update(id, request));
  }

  @Operation(summary = "Delete a product")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    productService.delete(id);
  }
}
