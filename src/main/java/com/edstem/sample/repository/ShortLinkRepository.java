package com.edstem.sample.repository;

import com.edstem.sample.entity.ShortLink;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortLinkRepository extends JpaRepository<ShortLink, UUID> {

  Optional<ShortLink> findByCode(String code);

  boolean existsByCode(String code);

  Optional<ShortLink> findFirstByOriginalUrlAndExpiresAt(String originalUrl, Instant expiresAt);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("update ShortLink s set s.visitCount = s.visitCount + 1 where s.id = :id")
  int incrementVisitCount(@Param("id") UUID id);
}
