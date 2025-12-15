package com.bank.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.io.Serializable;

@Data
public class TransactionRequest implements Serializable {
    private Long targetAccountId;
    private BigDecimal amount;
}