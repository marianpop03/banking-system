package com.bank.auditservice.audit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    // Constructor helper pentru mapare rapidă
    public AuditLog(String transactionId, String type, Long sourceAccountId, Long targetAccountId, BigDecimal amount, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.type = type;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}