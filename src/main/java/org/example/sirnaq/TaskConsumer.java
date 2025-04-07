package org.example.sirnaq;

import org.example.sirnaq.model.Task;
import org.example.sirnaq.repository.TaskRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TaskConsumer {

    private final TaskRepository taskRepository;

    public TaskConsumer(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void ReceiveTask(Task task) {
        taskRepository.save(task);
        System.out.println("Task saved: " + task.getTitle()); // Log dla debugowania
    }

}
