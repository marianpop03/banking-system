package com.bank.bankcoreservice.controller;

import com.bank.common.dto.TransactionRequest;
import com.bank.bankcoreservice.model.Account;
import com.bank.bankcoreservice.repository.AccountRepository;
import com.bank.bankcoreservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    // Helper pentru a verifica accesul la citire (pentru balanță)
    private Account getAccountIfOwner(Long accountId, String username) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Cont inexistent"));

        if (!account.getUser().getUsername().equals(username)) {
            throw new SecurityException("Acces interzis: Nu dețineți acest cont.");
        }
        return account;
    }

    // 1. Verificare Sold
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId, Authentication authentication) {
        try {
            // Verificăm dacă cel care cere soldul este proprietarul
            Account account = getAccountIfOwner(accountId, authentication.getName());
            return ResponseEntity.ok(account.getBalance());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2. Depunere
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<?> deposit(
            @PathVariable Long accountId,
            @RequestBody TransactionRequest request,
            Authentication authentication) {
        try {
            // Pasăm username-ul către service pentru validare și execuție
            accountService.deposit(accountId, request.getAmount(), authentication.getName());
            return ResponseEntity.ok("Depunere reușită");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Retragere
    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<?> withdraw(
            @PathVariable Long accountId,
            @RequestBody TransactionRequest request,
            Authentication authentication) {
        try {
            accountService.withdraw(accountId, request.getAmount(), authentication.getName());
            return ResponseEntity.ok("Retragere reușită");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Transfer
    @PostMapping("/transfer/{sourceAccountId}")
    public ResponseEntity<?> transfer(
            @PathVariable Long sourceAccountId,
            @RequestBody TransactionRequest request,
            Authentication authentication) {
        try {
            accountService.transfer(
                    sourceAccountId,
                    request.getTargetAccountId(),
                    request.getAmount(),
                    authentication.getName()
            );
            return ResponseEntity.ok("Transfer reușit");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}