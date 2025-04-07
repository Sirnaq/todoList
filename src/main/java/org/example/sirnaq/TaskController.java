package org.example.sirnaq;

import jakarta.validation.Valid;
import org.example.sirnaq.model.Task;
import org.example.sirnaq.repository.TaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class TaskController {
    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    public TaskController(TaskRepository taskRepository, RabbitTemplate rabbitTemplate) {
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/tasks")
    public List<Task> getTasks(@RequestParam(required = false) Boolean completed) {
        if (completed != null) {
            return taskRepository.findByCompleted(completed);
        }
        return taskRepository.findAll();
    }

    @PostMapping("/tasks")
    public Task createTask(@Valid @RequestBody Task task) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TASK_QUEUE, task);
        return task;
    }

    @PutMapping("/tasks/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody Task updatedTask) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        updatedTask.setId(id); // Upewniamy się że ID się zgadza
        return taskRepository.save(updatedTask);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        taskRepository.deleteById(id);
    }
}
