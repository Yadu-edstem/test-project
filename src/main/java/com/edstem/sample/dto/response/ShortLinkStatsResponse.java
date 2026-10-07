package com.edstem.sample.dto.response;

import java.time.Instant;

public record ShortLinkStatsResponse(
    String code, String originalUrl, long visitCount, Instant createdAt, Instant expiresAt) {}
