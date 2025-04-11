package org.example.sirnaq;

import org.example.sirnaq.model.Task;
import org.example.sirnaq.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeleteTaskConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeleteTaskConsumer.class);
    private final TaskRepository taskRepository;
    private final TaskWebSocketHandler webSocketHandler;

    public DeleteTaskConsumer(TaskRepository taskRepository, TaskWebSocketHandler webSocketHandler) {
        this.taskRepository = taskRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @RabbitListener(queues = RabbitMQConfig.DELETE_QUEUE)
    public void deleteTask(Task task) {
        logger.info("Reveived Delete request for task: {}, (ID: {})", task.getTitle(), task.getId());
        if (taskRepository.existsById(task.getId())) {
            taskRepository.deleteById(task.getId());
            logger.info("Deleted task: {}, (ID: {})", task.getTitle(), task.getId());
            try {
                webSocketHandler.sendTaskUpdate(task.getId(), "taskDeleted");
            } catch (Exception e) {
                logger.error("Failed to send WebSocket update for deletion: ", e);
            }
        } else {
            logger.warn("Task with ID: {} not found for deletion", task.getId());
        }
    }
}
