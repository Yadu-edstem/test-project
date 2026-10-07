package com.edstem.sample.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String PRODUCTS = "products";

  @Bean
  CacheManager cacheManager(@Value("${spring.cache.caffeine.spec}") String caffeineSpec) {
    CaffeineCacheManager caffeine = new CaffeineCacheManager(PRODUCTS);
    caffeine.setCacheSpecification(caffeineSpec);
    return new TransactionAwareCacheManagerProxy(caffeine);
  }
}
