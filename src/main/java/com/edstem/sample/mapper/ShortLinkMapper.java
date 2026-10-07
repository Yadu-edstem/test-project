package com.edstem.sample.mapper;

import com.edstem.sample.config.ShortLinkProperties;
import com.edstem.sample.dto.request.ShortenRequest;
import com.edstem.sample.dto.response.ShortLinkResponse;
import com.edstem.sample.dto.response.ShortLinkStatsResponse;
import com.edstem.sample.entity.ShortLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShortLinkMapper {

  public static final String REDIRECT_PATH = "/s/";

  private final ShortLinkProperties properties;

  public ShortLink toEntity(ShortenRequest request, String code) {
    return ShortLink.builder()
        .code(code)
        .originalUrl(request.url())
        .expiresAt(request.expiresAt())
        .build();
  }

  public ShortLinkResponse toResponse(ShortLink link) {
    return new ShortLinkResponse(
        link.getCode(),
        shortUrl(link.getCode()),
        link.getOriginalUrl(),
        link.getExpiresAt(),
        link.getCreatedAt());
  }

  public ShortLinkStatsResponse toStats(ShortLink link) {
    return new ShortLinkStatsResponse(
        link.getCode(),
        link.getOriginalUrl(),
        link.getVisitCount(),
        link.getCreatedAt(),
        link.getExpiresAt());
  }

  private String shortUrl(String code) {
    return properties.baseUrl().replaceAll("/+$", "") + REDIRECT_PATH + code;
  }
}
