package com.edstem.sample.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.sample.support.IntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class TaskApiTest {

  private static final String TASKS = "/api/v1/tasks";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void createGetUpdateAndDeleteTask() throws Exception {
    String id = createTask(Map.of("title", "Plan sprint", "dueDate", tomorrow()));

    mockMvc
        .perform(get(TASKS + "/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("TODO"))
        .andExpect(jsonPath("$.data.createdAt").exists());

    mockMvc
        .perform(
            put(TASKS + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "Plan sprint", "status", "DONE"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("DONE"));

    mockMvc.perform(delete(TASKS + "/" + id)).andExpect(status().isNoContent());
    mockMvc.perform(get(TASKS + "/" + id)).andExpect(status().isNotFound());
  }

  @Test
  void invalidTaskReturnsFieldLevelMessages() throws Exception {
    mockMvc
        .perform(
            post(TASKS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "x".repeat(101), "dueDate", "2000-01-01"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
        .andExpect(
            jsonPath("$.error.details.title[0]").value("Title must be at most 100 characters"))
        .andExpect(jsonPath("$.error.details.dueDate[0]").value("Due date cannot be in the past"));
  }

  @Test
  void unknownStatusValueReturnsFieldLevelMessage() throws Exception {
    mockMvc
        .perform(
            post(TASKS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "Task", "status", "LATER"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.details.status[0]").exists());
  }

  @Test
  void unsupportedContentTypeReturns415InStandardFormat() throws Exception {
    mockMvc
        .perform(post(TASKS).contentType(MediaType.TEXT_PLAIN).content("title=x"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));
  }

  @Test
  void unknownTaskReturnsNotFoundInStandardFormat() throws Exception {
    mockMvc
        .perform(get(TASKS + "/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
  }

  @Test
  void listFiltersByStatus() throws Exception {
    createTask(Map.of("title", "Doing", "status", "IN_PROGRESS"));
    createTask(Map.of("title", "Done", "status", "DONE"));

    mockMvc
        .perform(get(TASKS).param("status", "IN_PROGRESS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[*].status", everyItem(is("IN_PROGRESS"))))
        .andExpect(jsonPath("$.data.page.totalElements").exists());
  }

  private String createTask(Map<String, Object> body) throws Exception {
    String response =
        mockMvc
            .perform(post(TASKS).contentType(MediaType.APPLICATION_JSON).content(json(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.createdAt").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode data = objectMapper.readTree(response).path("data");
    return data.path("id").asText();
  }

  private String json(Object body) throws Exception {
    return objectMapper.writeValueAsString(body);
  }

  private static String tomorrow() {
    return LocalDate.now().plusDays(1).toString();
  }
}
