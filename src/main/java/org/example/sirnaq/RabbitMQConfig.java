package org.example.sirnaq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitMQConfig {
    public static final String TASK_QUEUE = "task-queue";
    public static final String DELETE_QUEUE = "delete-queue";

    @Bean
    public Queue taskQueue() {
        return new Queue(TASK_QUEUE, true); //trwała kolejka
    }

    @Bean
    public Queue deleteQueue(){
        return new Queue(DELETE_QUEUE, true); //kolejka na delete
    }

    @Bean
    public SimpleMessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("org.example.sirnaq.model.Task")); // Dozwolona klasa
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, SimpleMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
