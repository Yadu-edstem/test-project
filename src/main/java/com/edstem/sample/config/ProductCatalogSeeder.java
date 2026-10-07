package com.edstem.sample.config;

import com.edstem.sample.entity.Product;
import com.edstem.sample.repository.ProductRepository;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCatalogSeeder implements ApplicationRunner {

  private static final int SEED_COUNT = 100;
  private static final long RANDOM_SEED = 42L;
  private static final List<String> CATEGORIES =
      List.of("Electronics", "Books", "Home", "Toys", "Sports");
  private static final List<String> ADJECTIVES =
      List.of("Classic", "Smart", "Compact", "Deluxe", "Eco", "Pro", "Mini", "Ultra");

  private final ProductRepository productRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (productRepository.count() > 0) {
      return;
    }
    Random random = new Random(RANDOM_SEED);
    productRepository.saveAll(
        IntStream.rangeClosed(1, SEED_COUNT).mapToObj(index -> product(index, random)).toList());
    log.info("Seeded {} products", SEED_COUNT);
  }

  private static Product product(int index, Random random) {
    String category = CATEGORIES.get(index % CATEGORIES.size());
    return Product.builder()
        .name(ADJECTIVES.get(random.nextInt(ADJECTIVES.size())) + " " + category + " " + index)
        .category(category)
        .priceCents(100L + random.nextInt(99_900))
        .stock(random.nextInt(50))
        .rating(Math.round(random.nextDouble() * 50) / 10.0)
        .build();
  }
}
