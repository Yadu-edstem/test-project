package com.edstem.sample.repository;

import com.edstem.sample.entity.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository
    extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      "update Product p set p.stock = p.stock - :quantity"
          + " where p.id = :id and p.stock >= :quantity")
  int reserveStock(@Param("id") UUID id, @Param("quantity") int quantity);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("update Product p set p.stock = p.stock + :quantity where p.id = :id")
  int releaseStock(@Param("id") UUID id, @Param("quantity") int quantity);
}
