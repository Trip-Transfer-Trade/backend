package com.example.module_trip.account;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public String getAccountStatus(Integer userId) {
        if (userId == null) {
            return "NOT_LOGGED_IN";
        }
        boolean hasAccount = accountRepository.existsByUserId(userId);
        return hasAccount ? "LOGGED_IN_WITH_ACCOUNT" : "LOGGED_IN_NO_ACCOUNT";
    }

    public String saveAccount(Integer userId,  AccountCreateRequestDTO accountCreateRequestDTO) {
        AccountType newAccountType = accountCreateRequestDTO.getAccountType();

        if (newAccountType == AccountType.NORMAL) { // NORMAL 계좌만 제한
            boolean hasNormalAccount = accountRepository.existsByUserIdAndAccountType(userId, AccountType.NORMAL);
            if (hasNormalAccount) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A NORMAL account already exists.");
            }
        }

//        Optional<Account> hasAccount = accountRepository.findByUserIdAndAccountType(userId, AccountType.NORMAL);
//
//        if(hasAccount.isPresent()) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists");
//        }

        String generatedAccountNumber = generateRandomAccountNumber();
        Account account = accountCreateRequestDTO.toEntity(userId, generatedAccountNumber);
        accountRepository.save(account);
        return generatedAccountNumber;
    }

    public Optional<Account> findAccountByUserIdAndType(int userId, AccountType accountType) {
        return accountRepository.findByUserIdAndAccountType(userId, accountType);
    }


    private String generateRandomAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextInt(9) + 1); // 첫 번째 숫자는 1~9 중 하나
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

    public List<AccountResponseDTO> getAllAccountByUserId(Integer userId) {
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

    public NormalAccountDTO getNormalAccountByUserId(Integer userId) {
        Account normalAccount = accountRepository.findByUserIdAndAccountType(userId, AccountType.NORMAL)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No NORMAL account found for user ID: " + userId));
        return NormalAccountDTO.toDTO(normalAccount);
    }

    public int countTripCompleted(int userId){
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.size();
    }


}