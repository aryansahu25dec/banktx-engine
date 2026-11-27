package com.banktx.banktx.controller;

import com.banktx.banktx.dto.TransactionRequest;
import com.banktx.banktx.dto.TransactionResult;
import com.banktx.banktx.model.BankTransaction;
import com.banktx.banktx.repository.TransactionRepository;
import com.banktx.banktx.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;
    private final TransactionRepository transactions;

    public TransactionController(TransactionService service, TransactionRepository transactions) {
        this.service = service;
        this.transactions = transactions;
    }

    @PostMapping
    public ResponseEntity<TransactionResult> create(@RequestBody TransactionRequest request) {
        TransactionResult result = service.process(request);
        HttpStatus http = switch (result.status()) {
            case "SUCCESS" -> HttpStatus.OK;
            case "REJECTED" -> "DuplicateTransactionException".equals(result.errorCategory())
                    ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        return ResponseEntity.status(http).body(result);
    }

    @GetMapping
    public List<BankTransaction> recent(@RequestParam(defaultValue = "100") int limit) {
        return transactions.findRecent(Math.min(Math.max(limit, 1), 500));
    }
}
