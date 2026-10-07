package com.edstem.sample.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.short-link")
public record ShortLinkProperties(@NotBlank String baseUrl) {}
