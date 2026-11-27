package com.banktx.banktx.service;

import com.banktx.banktx.dto.TransactionRequest;
import com.banktx.banktx.dto.TransactionResult;
import com.banktx.banktx.exception.*;
import com.banktx.banktx.model.Account;
import com.banktx.banktx.model.BankTransaction;
import com.banktx.banktx.repository.AccountRepository;
import com.banktx.banktx.repository.TransactionRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class TransactionService {

    private static final Set<String> TYPES = Set.of("DEPOSIT", "WITHDRAWAL", "TRANSFER");

    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TransactionTemplate tx;

    public TransactionService(AccountRepository accounts,
                              TransactionRepository transactions,
                              TransactionTemplate tx) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.tx = tx;
    }

    public TransactionResult process(TransactionRequest req) {
        // Requests we cannot even store (no id, or a type the table cannot hold)
        if (req.transactionId() == null || req.transactionId().isBlank()) {
            return new TransactionResult("REJECTED", "transactionId is required",
                    "InvalidTransactionException");
        }
        if (req.type() == null || !TYPES.contains(req.type())) {
            return new TransactionResult("REJECTED",
                    "Unsupported transaction type: " + req.type(),
                    "InvalidTransactionException");
        }

        try {
            validate(req);
            if (transactions.existsByTransactionId(req.transactionId())) {
                throw new DuplicateTransactionException("duplicate");
            }
            // One DB transaction: balance change + SUCCESS row commit together or not at all
            String message = tx.execute(status -> {
                String msg = apply(req);
                transactions.insert(row(req, "SUCCESS", msg, null));
                return msg;
            });
            return new TransactionResult("SUCCESS", message, null);

        } catch (DuplicateTransactionException | DuplicateKeyException e) {
            return duplicate(req);
        } catch (InvalidTransactionException e) {
            return record(req, "REJECTED", e.getMessage(), "InvalidTransactionException");
        } catch (AccountNotFoundException e) {
            return record(req, "FAILED", e.getMessage(), "AccountNotFoundException");
        } catch (InsufficientBalanceException e) {
            return record(req, "FAILED", e.getMessage(), "InsufficientBalanceException");
        } catch (DataAccessException e) {
            return record(req, "FAILED", "Database error: " + e.getClass().getSimpleName(),
                    "DatabaseException");
        }
    }

    // ---------- business rules ----------

    private void validate(TransactionRequest r) {
        if (r.amount() == null || r.amount().signum() <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }
        if (!r.type().equals("DEPOSIT") && isBlank(r.sourceAccount())) {
            throw new InvalidTransactionException("sourceAccount is required for " + r.type());
        }
        if (!r.type().equals("WITHDRAWAL") && isBlank(r.destinationAccount())) {
            throw new InvalidTransactionException("destinationAccount is required for " + r.type());
        }
    }

    private String apply(TransactionRequest r) {
        BigDecimal amt = r.amount();

        if (r.type().equals("DEPOSIT")) {
            Account dst = load(r.destinationAccount());
            requireActive(dst);
            accounts.updateBalance(dst.accountId(), dst.balance().add(amt));
            return "Credited " + amt + " to " + dst.accountId();
        }

        if (r.type().equals("WITHDRAWAL")) {
            Account src = load(r.sourceAccount());
            requireActive(src);
            requireFunds(src, amt);
            accounts.updateBalance(src.accountId(), src.balance().subtract(amt));
            return "Debited " + amt + " from " + src.accountId();
        }

        // TRANSFER
        if (r.sourceAccount().equals(r.destinationAccount())) {
            throw new InvalidTransactionException("Source and destination accounts cannot be identical");
        }
        Account src = load(r.sourceAccount());
        Account dst = load(r.destinationAccount());
        requireActive(src);
        requireActive(dst);
        requireFunds(src, amt);
        accounts.updateBalance(src.accountId(), src.balance().subtract(amt));
        accounts.updateBalance(dst.accountId(), dst.balance().add(amt));
        return "Transferred " + amt + " from " + src.accountId() + " to " + dst.accountId();
    }

    private Account load(String accountId) {
        return accounts.findById(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account " + accountId + " not found"));
    }

    private void requireActive(Account a) {
        if (!"ACTIVE".equals(a.status())) {
            throw new InvalidTransactionException("Account " + a.accountId() + " is " + a.status());
        }
    }

    private void requireFunds(Account a, BigDecimal amt) {
        if (a.balance().compareTo(amt) < 0) {
            throw new InsufficientBalanceException("Insufficient balance on " + a.accountId()
                    + " (available: " + a.balance() + ")");
        }
    }

    // ---------- recording outcomes ----------

    /** Stores a FAILED/REJECTED attempt in its own DB transaction. */
    private TransactionResult record(TransactionRequest req, String status,
                                     String reason, String category) {
        try {
            tx.executeWithoutResult(s -> {
                transactions.insert(row(req, status, reason, category));
                transactions.insertFailure(req.transactionId(), category, reason);
            });
        } catch (DuplicateKeyException e) {
            return duplicate(req);
        }
        return new TransactionResult(status, reason, category);
    }

    private TransactionResult duplicate(TransactionRequest req) {
        return new TransactionResult("REJECTED",
                "Duplicate transactionId (" + req.transactionId() + ") - no balance change applied.",
                "DuplicateTransactionException");
    }

    private BankTransaction row(TransactionRequest r, String status,
                                String reason, String category) {
        BigDecimal amt = r.amount() == null ? BigDecimal.ZERO : r.amount();
        return new BankTransaction(r.transactionId(), r.sourceAccount(), r.destinationAccount(),
                amt, r.type(), status, reason, category, null);
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}