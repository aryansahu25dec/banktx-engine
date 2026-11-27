package com.banktx.banktx.dto;

import java.math.BigDecimal;

public record TransactionRequest(String transactionId, String type,
        String sourceAccount, String destinationAccount, BigDecimal amount) {}
        