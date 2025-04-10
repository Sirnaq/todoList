package org.example.sirnaq;

import org.example.sirnaq.model.Task;
import org.example.sirnaq.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TaskConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TaskConsumer.class);
    private final TaskRepository taskRepository;
    private final TaskWebSocketHandler webSocketHandler;

    public TaskConsumer(TaskRepository taskRepository, TaskWebSocketHandler webSocketHandler) {
        this.taskRepository = taskRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void receiveTask(Task task) {
        logger.info("Received task: {} (ID: {})", task.getTitle(), task.getId());
        Task existingTask = taskRepository.findById(task.getId()).orElseGet(() -> {
            logger.warn("Task with ID {} not found, creating new", task.getId());
            return new Task();
        });
        existingTask.setId(task.getId());
        existingTask.setTitle(task.getTitle());
        existingTask.setCompleted(task.isCompleted());
        try {
            taskRepository.save(existingTask);
            logger.info("Task saved: {} (ID: {})", existingTask.getTitle(), existingTask.getId());
        } catch (Exception e) {
            logger.error("Failed to save task: {}", task.getTitle(), e);
        }
        try {
            webSocketHandler.sendTaskUpdate(task.getId());
        } catch (Exception e) {
            logger.error("Failed to send WebSocket update", e);
        }
    }

}
