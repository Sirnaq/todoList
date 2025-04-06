package org.example.sirnaq.repository;

import org.example.sirnaq.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByCompleted(boolean completed);
}
