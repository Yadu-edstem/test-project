package com.edstem.sample.service;

import com.edstem.sample.dto.request.TaskRequest;
import com.edstem.sample.dto.response.TaskResponse;
import com.edstem.sample.entity.Task;
import com.edstem.sample.entity.TaskStatus;
import com.edstem.sample.exception.ResourceNotFoundException;
import com.edstem.sample.mapper.TaskMapper;
import com.edstem.sample.repository.TaskRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskMapper taskMapper;

  @Transactional
  public TaskResponse create(TaskRequest request) {
    return taskMapper.toResponse(taskRepository.saveAndFlush(taskMapper.toEntity(request)));
  }

  public Page<TaskResponse> list(Optional<TaskStatus> status, Pageable pageable) {
    return status
        .map(value -> taskRepository.findByStatus(value, pageable))
        .orElseGet(() -> taskRepository.findAll(pageable))
        .map(taskMapper::toResponse);
  }

  public TaskResponse get(UUID id) {
    return taskMapper.toResponse(findOrThrow(id));
  }

  @Transactional
  public TaskResponse update(UUID id, TaskRequest request) {
    return taskMapper.toResponse(
        taskRepository.saveAndFlush(taskMapper.apply(request, findOrThrow(id))));
  }

  @Transactional
  public void delete(UUID id) {
    taskRepository.delete(findOrThrow(id));
  }

  private Task findOrThrow(UUID id) {
    return taskRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(Task.class, id));
  }
}
