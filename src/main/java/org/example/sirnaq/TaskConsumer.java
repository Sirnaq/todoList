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

    public TaskConsumer(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void ReceiveTask(Task task) {

        logger.info("Received task: {}", task.getTitle());
        taskRepository.save(task);
        logger.info("Task saved: {}", task.getTitle());
    }

}
