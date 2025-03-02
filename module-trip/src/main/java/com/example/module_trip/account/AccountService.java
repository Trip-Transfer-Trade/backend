package com.example.module_trip.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.module_utility.response.Response;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void saveAccount(AccountCreateRequestDTO accountCreateRequestDTO) {
        String generatedAccountNumber = generateRandomAccountNumber();
        Account account = accountCreateRequestDTO.toEntity(generatedAccountNumber);
        accountRepository.save(account);
    }

    private String generateRandomAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public List<AccountResponseDTO> getAccountById(Integer userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(AccountResponseDTO::toDTO)
                .collect(Collectors.toList());
    }

    public AccountResponseDTO getAccountByAccountId(Integer accountId) {
         return accountRepository.findById(accountId)
                 .map(AccountResponseDTO::toDTO)
                 .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + accountId));
    }

    public AccountResponseDTO getAccountByAccountNumber(String accountNumber) {
        AccountResponseDTO account = accountRepository.findByAccountNumber(accountNumber)
                    .map(AccountResponseDTO::toDTO)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found with account number: " + accountNumber));

        return account;
    }

    public AccountUpdateResponseDTO updateTotalValue(Integer accountId,BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " +accountId));
        account.changeTotalValue(amount);
        Account updatedAccount = accountRepository.save(account);
        return AccountUpdateResponseDTO.toDTO(updatedAccount);
    }

}