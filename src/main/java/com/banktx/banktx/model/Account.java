
package com.banktx.banktx.model;

import java.math.BigDecimal;

public record Account(String accountId, 
    String customerId,String customerName, 
    BigDecimal balance, String status) {}

    