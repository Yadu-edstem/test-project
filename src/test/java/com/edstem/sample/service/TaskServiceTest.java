package com.edstem.sample.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edstem.sample.dto.request.TaskRequest;
import com.edstem.sample.dto.response.TaskResponse;
import com.edstem.sample.entity.Task;
import com.edstem.sample.entity.TaskStatus;
import com.edstem.sample.exception.ResourceNotFoundException;
import com.edstem.sample.mapper.TaskMapper;
import com.edstem.sample.repository.TaskRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;
  @Spy private TaskMapper taskMapper;
  @InjectMocks private TaskService taskService;

  @Test
  void createDefaultsStatusToTodoAndTrimsTitle() {
    TaskRequest request = new TaskRequest("  Write report  ", null, null, LocalDate.now());
    when(taskRepository.saveAndFlush(any(Task.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TaskResponse response = taskService.create(request);

    assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    assertThat(response.title()).isEqualTo("Write report");
  }

  @Test
  void getUnknownTaskThrowsNotFound() {
    UUID id = UUID.randomUUID();
    when(taskRepository.findById(id)).thenReturn(Optional.empty());

    ResourceNotFoundException error =
        assertThrows(ResourceNotFoundException.class, () -> taskService.get(id));

    assertThat(error.getMessage()).contains("Task", id.toString());
  }

  @Test
  void deleteUnknownTaskDeletesNothing() {
    UUID id = UUID.randomUUID();
    when(taskRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> taskService.delete(id));

    verify(taskRepository, never()).delete(any(Task.class));
  }
}
