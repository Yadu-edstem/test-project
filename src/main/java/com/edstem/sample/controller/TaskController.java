package com.edstem.sample.controller;

import com.edstem.sample.dto.ApiResponse;
import com.edstem.sample.dto.request.TaskRequest;
import com.edstem.sample.dto.response.TaskResponse;
import com.edstem.sample.entity.TaskStatus;
import com.edstem.sample.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tasks", description = "Create, view, update, delete and filter tasks")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  @Operation(summary = "Create a task")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
    return ApiResponse.success(taskService.create(request));
  }

  @Operation(summary = "List tasks, optionally filtered by status")
  @GetMapping
  public ApiResponse<Page<TaskResponse>> list(
      @RequestParam Optional<TaskStatus> status,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ApiResponse.success(taskService.list(status, pageable));
  }

  @Operation(summary = "Get a task")
  @GetMapping("/{id}")
  public ApiResponse<TaskResponse> get(@PathVariable UUID id) {
    return ApiResponse.success(taskService.get(id));
  }

  @Operation(summary = "Replace a task")
  @PutMapping("/{id}")
  public ApiResponse<TaskResponse> update(
      @PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
    return ApiResponse.success(taskService.update(id, request));
  }

  @Operation(summary = "Delete a task")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    taskService.delete(id);
  }
}
