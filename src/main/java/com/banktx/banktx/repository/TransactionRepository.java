package com.banktx.banktx.repository;

import com.banktx.banktx.model.BankTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionRepository {
    private final JdbcTemplate jdbc;

    public TransactionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final RowMapper<BankTransaction> MAPPER = (rs, i) -> new BankTransaction(
            rs.getString("transaction_id"), rs.getString("source_account"),
            rs.getString("destination_account"), rs.getBigDecimal("amount"),
            rs.getString("type"), rs.getString("status"), rs.getString("reason"),
            rs.getString("error_category"), rs.getTimestamp("created_at").toInstant());

    public List<BankTransaction> findRecent(int limit) {
        return jdbc.query("""
                SELECT transaction_id, source_account, destination_account, amount,
                       type, status, reason, error_category, created_at
                FROM bank_transaction ORDER BY created_at DESC, id DESC LIMIT ?
                """, MAPPER, limit);
    }

    public boolean existsByTransactionId(String transactionId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bank_transaction WHERE transaction_id = ?",
                Integer.class, transactionId);
        return n != null && n > 0;
    }

    public void insert(BankTransaction t) {
        jdbc.update("""
                INSERT INTO bank_transaction (transaction_id, type, source_account,
                  destination_account, amount, status, reason, error_category)
                VALUES (?,?,?,?,?,?,?,?)
                """, t.transactionId(), t.type(), t.sourceAccount(), t.destinationAccount(),
                t.amount(), t.status(), t.reason(), t.errorCategory());
    }

        public void insertFailure(String transactionId, String errorCategory, String reason) {
        jdbc.update("""
                INSERT INTO failed_transaction (transaction_id, error_category, reason)
                VALUES (?,?,?)
                """, transactionId, errorCategory, reason);
    }
    
}
