package org.example.sirnaq;

import org.example.sirnaq.model.Task;
import org.example.sirnaq.repository.TaskRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {
    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/tasks")
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @PostMapping("/tasks")
    public Task createTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }

    @PutMapping("/tasks/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        if(!taskRepository.existsById(id)){
            throw new RuntimeException("Task not found");
        }
        updatedTask.setId(id); // Upewniamy się że ID się zgadza
        return taskRepository.save(updatedTask);
    }
    
    @DeleteMapping("/tasks/{id}")
    public void deleteTask(@PathVariable Long id) {
        if(!taskRepository.existsById(id)){
            throw new RuntimeException("Task not found");
        }
        taskRepository.deleteById(id);
    }
}
