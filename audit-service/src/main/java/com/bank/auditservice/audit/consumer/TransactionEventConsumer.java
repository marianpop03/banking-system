package com.bank.auditservice.audit.consumer;



import com.bank.auditservice.audit.model.AuditLog;
import com.bank.auditservice.audit.repository.AuditLogRepository;
import com.bank.common.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final AuditLogRepository auditLogRepository;

    // Ascultăm coada "audit_queue" definită în core/config sau creată automat
    @RabbitListener(queues = "audit_queue")
    public void handleTransactionEvent(TransactionEvent event) {
        log.info("Eveniment primit pentru audit: {}", event);

        AuditLog logEntry = new AuditLog(
                event.getTransactionId(),
                event.getType().toString(),
                event.getAccountId(),
                event.getTargetAccountId(),
                event.getAmount(),
                event.getTimestamp()
        );

        auditLogRepository.save(logEntry);
        log.info("Tranzacție salvată în istoric cu ID intern: {}", logEntry.getId());
    }
}