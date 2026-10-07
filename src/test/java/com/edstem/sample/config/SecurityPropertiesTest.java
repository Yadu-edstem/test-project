package com.edstem.sample.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class SecurityPropertiesTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shortJwtSecretIsRejected() {
    SecurityProperties properties = withSecret("dummy-too-short");

    assertThat(validator.validate(properties))
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsExactly("jwtSecretStrongEnough");
  }

  @Test
  void missingOrLongEnoughSecretIsAccepted() {
    assertThat(validator.validate(withSecret(""))).isEmpty();
    assertThat(validator.validate(withSecret("dummy-".repeat(6)))).isEmpty();
  }

  private static SecurityProperties withSecret(String secret) {
    return new SecurityProperties(secret, Duration.ofMinutes(15), "", "");
  }
}
