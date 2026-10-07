package com.edstem.sample;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

  @Test
  void moduleBoundariesHold() {
    ApplicationModules.of(Application.class).verify();
  }
}
