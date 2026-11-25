
package com.banktx.banktx.model;

import java.math.BigDecimal;
import java.time.Instant;

public record BankTransaction(String transactionId, 
    String sourceAccount, String destinationAccount, 
    BigDecimal amount, String type,
        String status, String reason, 
        String errorCategory, Instant createdAt) {}

        