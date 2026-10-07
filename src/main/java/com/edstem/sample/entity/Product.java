package com.edstem.sample.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Column(name = "category", nullable = false, length = 100)
  private String category;

  @Column(name = "price_cents", nullable = false)
  private Long priceCents;

  @Column(name = "stock", nullable = false)
  private Integer stock;

  @Column(name = "rating")
  private Double rating;
}
