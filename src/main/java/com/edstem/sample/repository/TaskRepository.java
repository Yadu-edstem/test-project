package com.edstem.sample.repository;

import com.edstem.sample.entity.Task;
import com.edstem.sample.entity.TaskStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {

  Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
