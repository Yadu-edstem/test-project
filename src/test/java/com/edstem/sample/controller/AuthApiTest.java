package com.edstem.sample.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.sample.service.UserService;
import com.edstem.sample.support.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class AuthApiTest {

  private static final String TEST_PASSWORD = "dummy-password-123";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserService userService;

  @Test
  void requestWithoutTokenReturnsJson401() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
  }

  @Test
  void userCanViewOwnProfileButCannotListUsers() throws Exception {
    String email = uniqueEmail();
    register(email)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.role").value("USER"));
    String token = login(email);

    mockMvc
        .perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value(email));
    mockMvc
        .perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
  }

  @Test
  void adminCanListUsers() throws Exception {
    String email = uniqueEmail();
    userService.createAdminIfAbsent(email, TEST_PASSWORD);

    mockMvc
        .perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, bearer(login(email))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page.totalElements").exists());
  }

  @Test
  void wrongPasswordReturns401AndHashIsNeverExposed() throws Exception {
    String email = uniqueEmail();
    register(email)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.password").doesNotExist())
        .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", "dummy-wrong-pass"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
  }

  @Test
  void registeringATakenEmailReturns409() throws Exception {
    String email = uniqueEmail();
    register(email).andExpect(status().isCreated());

    register(email.toUpperCase())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("EMAIL_TAKEN"));
  }

  @Test
  void malformedTokenIsRejected() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, bearer("not.a.jwt")))
        .andExpect(status().isUnauthorized());
  }

  private ResultActions register(String email) throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("email", email, "password", TEST_PASSWORD))));
  }

  private String login(String email) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("email", email, "password", TEST_PASSWORD))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.expiresIn").value(900))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body).path("data").path("accessToken").asText();
  }

  private String json(Object body) throws Exception {
    return objectMapper.writeValueAsString(body);
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }

  private static String uniqueEmail() {
    return "user-" + UUID.randomUUID() + "@example.com";
  }
}
