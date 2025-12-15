package com.bank.bankcoreservice.service;
import com.bank.common.dto.TransactionEvent;
import com.bank.common.dto.TransactionEvent.Type;
import com.bank.bankcoreservice.model.Account;
import com.bank.bankcoreservice.producer.TransactionEventProducer;
import com.bank.bankcoreservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionEventProducer eventProducer;

    // Metodă auxiliară pentru securitate
    private Account getAccountAndVerifyOwner(Long accountId, String username) {
        // Folosim findByIdWithLock pentru a evita race conditions
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Cont inexistent"));

        if (!account.getUser().getUsername().equals(username)) {
            throw new SecurityException("Acces interzis: Nu sunteți proprietarul acestui cont.");
        }
        return account;
    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount, String username) {
        Account account = getAccountAndVerifyOwner(accountId, username);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        eventProducer.publishTransactionEvent(new TransactionEvent(
                UUID.randomUUID().toString(), Type.DEPOSIT, accountId, null, amount));
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount, String username) {
        Account account = getAccountAndVerifyOwner(accountId, username);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Fonduri insuficiente");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        eventProducer.publishTransactionEvent(new TransactionEvent(
                UUID.randomUUID().toString(), Type.WITHDRAWAL, accountId, null, amount));
    }

    @Transactional
    public void transfer(Long sourceId, Long targetId, BigDecimal amount, String username) {
        if (sourceId.equals(targetId)) throw new IllegalArgumentException("Nu puteți transfera în același cont");

        // Blocăm ambele conturi (ordinea e importantă pt a evita Deadlock - ideal sortăm după ID)
        Account source = getAccountAndVerifyOwner(sourceId, username);
        // La target nu verificăm owner-ul (avem voie să trimitem bani altora), dar îl blocăm pt siguranță
        Account target = accountRepository.findByIdWithLock(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Cont destinatar inexistent"));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Fonduri insuficiente");
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(target);

        eventProducer.publishTransactionEvent(new TransactionEvent(
                UUID.randomUUID().toString(), Type.TRANSFER, sourceId, targetId, amount));
    }
}