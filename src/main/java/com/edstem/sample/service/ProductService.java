package com.edstem.sample.service;

import com.edstem.sample.config.CacheConfig;
import com.edstem.sample.dto.request.ProductFilter;
import com.edstem.sample.dto.request.ProductRequest;
import com.edstem.sample.dto.response.ProductResponse;
import com.edstem.sample.entity.Product;
import com.edstem.sample.exception.ResourceNotFoundException;
import com.edstem.sample.mapper.ProductMapper;
import com.edstem.sample.repository.ProductRepository;
import com.edstem.sample.repository.ProductSpecifications;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  public Page<ProductResponse> list(ProductFilter filter, Pageable pageable) {
    return productRepository
        .findAll(ProductSpecifications.matching(filter), pageable)
        .map(productMapper::toResponse);
  }

  @Cacheable(cacheNames = CacheConfig.PRODUCTS, key = "#id")
  public ProductResponse get(UUID id) {
    return productMapper.toResponse(findOrThrow(id));
  }

  @Transactional
  public ProductResponse create(ProductRequest request) {
    return productMapper.toResponse(
        productRepository.saveAndFlush(productMapper.toEntity(request)));
  }

  @Transactional
  @CacheEvict(cacheNames = CacheConfig.PRODUCTS, key = "#id")
  public ProductResponse update(UUID id, ProductRequest request) {
    return productMapper.toResponse(
        productRepository.saveAndFlush(productMapper.apply(request, findOrThrow(id))));
  }

  @Transactional
  @CacheEvict(cacheNames = CacheConfig.PRODUCTS, key = "#id")
  public void delete(UUID id) {
    productRepository.delete(findOrThrow(id));
  }

  private Product findOrThrow(UUID id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(Product.class, id));
  }
}
