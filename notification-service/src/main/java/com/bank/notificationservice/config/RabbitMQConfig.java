package com.bank.notificationservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
@Configuration
public class  RabbitMQConfig {

    public static final String NOTIFICATION_QUEUE = "notification_queue";
    public static final String EXCHANGE_NAME = "transaction_exchange";

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE);
    }

    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(EXCHANGE_NAME);
    }

    // Legăm coada de notificare la același exchange principal
    @Bean
    public Binding binding(Queue notificationQueue, FanoutExchange exchange) {
        return BindingBuilder.bind(notificationQueue).to(exchange);
    }

    @Bean
    public Jackson2JsonMessageConverter converter() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }
}