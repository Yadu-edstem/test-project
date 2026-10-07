package com.edstem.sample.config;

import com.edstem.sample.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

  private final SecurityProperties properties;
  private final UserService userService;

  @Override
  public void run(ApplicationArguments args) {
    if (properties.hasAdminAccount()
        && userService.createAdminIfAbsent(properties.adminEmail(), properties.adminPassword())) {
      log.info("Created the admin account from ADMIN_EMAIL");
    }
  }
}
