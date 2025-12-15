package com.bank.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent implements Serializable {
    // Enum-ul poate sta în interior sau separat
    public enum Type { DEPOSIT, WITHDRAWAL, TRANSFER }

    private String transactionId;
    private Type type;
    private Long accountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status;

    // Constructor custom pentru a inițializa timestamp-ul automat
    public TransactionEvent(String transactionId, Type type, Long accountId, Long targetAccountId, BigDecimal amount) {
        this.transactionId = transactionId;
        this.type = type;
        this.accountId = accountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.status = "COMPLETED";
    }
}