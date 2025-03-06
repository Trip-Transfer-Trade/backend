package com.example.module_trip.account;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

//    @Transactional// repository에 저장하는 작업만 할거면 trnasactional 안묶어도 될 듯
    public void saveAccount(AccountCreateRequestDTO accountCreateRequestDTO) {
        String generatedAccountNumber = generateRandomAccountNumber();
        Account account = accountCreateRequestDTO.toEntity(generatedAccountNumber);
        accountRepository.save(account);
    }

    @Transactional
    public Account createTravelGoalAccount(int userId) {
        Account travelGoalAccount = Account.builder()
                .userId(userId)
                .accountType(AccountType.TRAVEL_GOAL)
                .accountNumber(generateRandomAccountNumber())
                .build();

        return accountRepository.save(travelGoalAccount);
    }

    public Optional<Account> findAccountByUserIdAndType(int userId, AccountType accountType) {
        return accountRepository.findByUserIdAndAccountType(userId, accountType);
    }

    private String generateRandomAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public List<AccountResponseDTO> getAccountByUserId(Integer userId) {
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

    public AccountResponseDTO getAccountByIdAndType(Integer userId, AccountType type) {
        return accountRepository.findByUserIdAndAccountType(userId,type)
                .map(AccountResponseDTO::toDTO)
                .orElseThrow(()-> new IllegalArgumentException("Account not found with user Id & account type"));
    }

    public int findAccountIdByUserId(int userId) {
        return accountRepository.findFirstByUserId(userId)
                .map(Account::getId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found for userId: " + userId));
    }

}