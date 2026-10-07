package com.edstem.sample.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
    String jwtSecret, @NotNull Duration accessTokenTtl, String adminEmail, String adminPassword) {

  public static final int MIN_JWT_SECRET_BYTES = 32;

  public boolean hasJwtSecret() {
    return StringUtils.hasText(jwtSecret);
  }

  public boolean hasAdminAccount() {
    return StringUtils.hasText(adminEmail) && StringUtils.hasText(adminPassword);
  }

  @AssertTrue(message = "JWT_SECRET must be at least 32 bytes for HS256")
  public boolean isJwtSecretStrongEnough() {
    return !hasJwtSecret()
        || jwtSecret.getBytes(StandardCharsets.UTF_8).length >= MIN_JWT_SECRET_BYTES;
  }
}
