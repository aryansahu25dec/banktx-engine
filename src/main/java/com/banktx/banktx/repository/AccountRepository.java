package com.banktx.banktx.repository;

import com.banktx.banktx.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbc;

    public AccountRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final RowMapper<Account> MAPPER = (rs, i) -> new Account(
            rs.getString("account_id"), rs.getString("customer_id"),
            rs.getString("name"), rs.getBigDecimal("balance"), rs.getString("status"));

    private static final String SELECT = """
            SELECT a.account_id, a.customer_id, c.name, a.balance, a.status
            FROM account a JOIN customer c ON c.customer_id = a.customer_id
            """;

    public List<Account> findAll() {
        return jdbc.query(SELECT + " ORDER BY a.account_id", MAPPER);
    }

    public Optional<Account> findById(String accountId) {
        return jdbc.query(SELECT + " WHERE a.account_id = ?", MAPPER, accountId)
                   .stream().findFirst();
    }
}
