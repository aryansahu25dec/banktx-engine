package com.banktx.banktx.controller;

import com.banktx.banktx.model.Account;
import com.banktx.banktx.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository accounts;

    public AccountController(AccountRepository accounts) { this.accounts = accounts; }

    @GetMapping
    public List<Account> all() { return accounts.findAll(); }
}
