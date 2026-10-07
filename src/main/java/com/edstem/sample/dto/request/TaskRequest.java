package com.edstem.sample.dto.request;

import com.edstem.sample.entity.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
    @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,
    @Size(max = 5000, message = "Description must be at most 5000 characters") String description,
    TaskStatus status,
    @FutureOrPresent(message = "Due date cannot be in the past") LocalDate dueDate) {

  public TaskStatus statusOrDefault() {
    return status == null ? TaskStatus.TODO : status;
  }
}
