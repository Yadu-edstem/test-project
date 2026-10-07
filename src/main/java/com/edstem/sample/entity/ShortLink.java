package com.edstem.sample.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "short_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortLink extends BaseEntity {

  @Column(name = "code", nullable = false, unique = true, length = 8)
  private String code;

  @Column(name = "original_url", nullable = false, length = 2048)
  private String originalUrl;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Builder.Default
  @Column(name = "visit_count", nullable = false)
  private long visitCount = 0L;

  public boolean isExpired(Instant now) {
    return expiresAt != null && !expiresAt.isAfter(now);
  }
}
