package com.bank.notificationservice.consumer;
import com.bank.common.dto.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @RabbitListener(queues = "notification_queue")
    public void sendNotification(TransactionEvent event) {
        log.info("📧 PREGĂTIRE EMAIL PENTRU CONTUL: {}", event.getAccountId());

        String messageBody = String.format(
                "Salut! O tranzacție de tip %s în valoare de %s RON a fost procesată pe contul tău. ID Tranzacție: %s",
                event.getType(),
                event.getAmount(),
                event.getTransactionId()
        );

        // Aici am apela un serviciu real de email (JavaMailSender)
        log.info("------------------------------------------------");
        log.info("TO: User (Owner of Account {})", event.getAccountId());
        log.info("SUBJECT: Actualizare Sold Bancar");
        log.info("BODY: {}", messageBody);
        log.info("------------------------------------------------");
    }
}
