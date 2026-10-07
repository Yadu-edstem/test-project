package com.edstem.sample.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.sample.entity.ShortLink;
import com.edstem.sample.repository.ShortLinkRepository;
import com.edstem.sample.service.ShortLinkService;
import com.edstem.sample.support.Concurrently;
import com.edstem.sample.support.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class ShortLinkApiTest {

  private static final String LINKS = "/api/v1/links";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ShortLinkService shortLinkService;
  @Autowired private ShortLinkRepository shortLinkRepository;

  @Test
  void shortenRedirectAndCountVisits() throws Exception {
    String url = uniqueUrl();
    String code = codeOf(shorten(Map.of("url", url)).andExpect(status().isCreated()));

    assertThat(code).hasSizeLessThanOrEqualTo(8).matches("[A-Za-z0-9]+");
    mockMvc
        .perform(get("/s/" + code))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", url));
    mockMvc
        .perform(get(LINKS + "/" + code + "/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.originalUrl").value(url))
        .andExpect(jsonPath("$.data.visitCount").value(1))
        .andExpect(jsonPath("$.data.createdAt").exists());
  }

  @Test
  void shorteningTheSameUrlTwiceReturnsTheSameLink() throws Exception {
    String url = uniqueUrl();
    String first = codeOf(shorten(Map.of("url", url)).andExpect(status().isCreated()));

    String second = codeOf(shorten(Map.of("url", url)).andExpect(status().isOk()));

    assertThat(second).isEqualTo(first);
  }

  @Test
  void invalidUrlIsRejected() throws Exception {
    shorten(Map.of("url", "ftp://not-allowed"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.details.url[0]").exists());
  }

  @Test
  void urlThatCannotBeRedirectedToIsRejected() throws Exception {
    shorten(Map.of("url", "https://example.com/has space"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.details.url[0]").exists());
  }

  @Test
  void unknownCodeReturnsNotFoundAndExpiredCodeReturnsGone() throws Exception {
    ShortLink expired =
        shortLinkRepository.save(
            ShortLink.builder()
                .code("Expired1")
                .originalUrl(uniqueUrl())
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build());

    mockMvc.perform(get("/s/Unknown9")).andExpect(status().isNotFound());
    mockMvc
        .perform(get("/s/" + expired.getCode()))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.error.code").value("LINK_EXPIRED"));
  }

  @Test
  void concurrentVisitsAreAllCounted() throws Exception {
    String code = codeOf(shorten(Map.of("url", uniqueUrl())));
    int visitors = 50;

    var outcomes = Concurrently.run(visitors, index -> () -> shortLinkService.resolve(code));

    assertThat(outcomes).allMatch(Concurrently.Outcome::succeeded);
    assertThat(shortLinkService.stats(code).visitCount()).isEqualTo(visitors);
  }

  private ResultActions shorten(Map<String, Object> body) throws Exception {
    return mockMvc.perform(
        post(LINKS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
  }

  private String codeOf(ResultActions result) throws Exception {
    String body = result.andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body).path("data").path("code").asText();
  }

  private static String uniqueUrl() {
    return "https://example.com/articles/" + UUID.randomUUID();
  }
}
