package org.example.sirnaq.repository;

import org.example.sirnaq.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task,Long> {
}
