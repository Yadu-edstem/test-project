package com.edstem.sample.mapper;

import com.edstem.sample.dto.request.TaskRequest;
import com.edstem.sample.dto.response.TaskResponse;
import com.edstem.sample.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

  public Task toEntity(TaskRequest request) {
    return apply(request, new Task());
  }

  public Task apply(TaskRequest request, Task task) {
    task.setTitle(request.title().trim());
    task.setDescription(request.description());
    task.setStatus(request.statusOrDefault());
    task.setDueDate(request.dueDate());
    return task;
  }

  public TaskResponse toResponse(Task task) {
    return new TaskResponse(
        task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.getStatus(),
        task.getDueDate(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }
}
