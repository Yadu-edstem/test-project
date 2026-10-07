package com.edstem.sample.dto.response;

import java.time.Instant;

public record ShortLinkResponse(
    String code, String shortUrl, String originalUrl, Instant expiresAt, Instant createdAt) {}
