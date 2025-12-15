package com.bank.bankcoreservice.producer;

import com.bank.common.dto.TransactionEvent;
import com.bank.bankcoreservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishTransactionEvent(TransactionEvent event) {
        log.info("Publishing event for transaction: {}", event.getTransactionId());
        // Routing key este gol ("") pentru Fanout exchange
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "", event);
    }
}