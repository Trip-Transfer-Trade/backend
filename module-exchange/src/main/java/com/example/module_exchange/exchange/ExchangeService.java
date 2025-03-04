package com.example.module_exchange.exchange;

import com.example.module_exchange.clients.AccountClient;
import com.example.module_exchange.clients.TripClient;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeHistory;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionDTO;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import com.example.module_trip.account.AccountResponseDTO;
import com.example.module_trip.account.AccountType;
import com.example.module_trip.account.AccountUpdateResponseDTO;
import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ExchangeService {

    private final AccountClient accountClient;
    private final TripClient tripClient;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ExchangeCurrencyRepository exchangeCurrencyRepository;

    public ExchangeService(AccountClient accountClient, TripClient tripClient,ExchangeHistoryRepository exchangeHistoryRepository, TransactionHistoryRepository transactionHistoryRepository, ExchangeCurrencyRepository exchangeCurrencyRepository) {
        this.accountClient = accountClient;
        this.tripClient = tripClient;
        this.exchangeHistoryRepository = exchangeHistoryRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
    }

    @Transactional
    public void executeExchangeProcess(ExchangeDTO exchangeDTO) {
        Integer accountId = exchangeDTO.getAccountId();

        String fromCurrencyCode = exchangeDTO.getFromCurrency();
        String toCurrencyCode = exchangeDTO.getToCurrency();

        BigDecimal fromAmount = exchangeDTO.getFromAmount();
        BigDecimal toAmount = exchangeDTO.getToAmount();

        ExchangeCurrency fromExchangeCurrency = getOrCreateExchangeCurrency(accountId, fromCurrencyCode);
        ExchangeCurrency toExchangeCurrency = getOrCreateExchangeCurrency(accountId, toCurrencyCode);
        validateSufficientBalance(fromExchangeCurrency,fromAmount);

        ExchangeHistory fromExchangeHistory = exchangeDTO.toExchangeHistory(fromExchangeCurrency);
        exchangeHistoryRepository.save(fromExchangeHistory);
        ExchangeHistory toExchangeHistory = exchangeDTO.toExchangeHistory(toExchangeCurrency);
        exchangeHistoryRepository.save(toExchangeHistory);

        TransactionHistory fromTransactionHistory = exchangeDTO.toTransactionHistory(fromExchangeCurrency, TransactionType.WITHDRAWAL, fromAmount);
        transactionHistoryRepository.save(fromTransactionHistory);
        TransactionHistory toTransactionHistory = exchangeDTO.toTransactionHistory(toExchangeCurrency, TransactionType.DEPOSIT, toAmount);
        transactionHistoryRepository.save(toTransactionHistory);

        fromExchangeCurrency.changeAmount(fromAmount.negate());
        toExchangeCurrency.changeAmount(toAmount);

    }

    @Transactional
    public AccountUpdateResponseDTO executeTransactionProcess(TransactionDTO transactionDTO) {
        Integer accountId = transactionDTO.getAccountId();
        Integer targetAccountId = getAccountIdFromAccountNumber(transactionDTO.getTargetAccountNumber());

        BigDecimal amount = transactionDTO.getAmount();
        ExchangeCurrency fromTransactionCurrency = getOrCreateExchangeCurrency(accountId, transactionDTO.getCurrencyCode());
        ExchangeCurrency toTransactionCurrency = getOrCreateExchangeCurrency(targetAccountId, transactionDTO.getCurrencyCode());
        validateSufficientBalance(fromTransactionCurrency,amount);

        TransactionHistory fromTransactionHistory = transactionDTO.toTransactionHistory(fromTransactionCurrency, TransactionType.WITHDRAWAL);
        transactionHistoryRepository.save(fromTransactionHistory);
        TransactionHistory toTransactionHistory = transactionDTO.toTransactionHistory(toTransactionCurrency, TransactionType.DEPOSIT);
        transactionHistoryRepository.save(toTransactionHistory);

        fromTransactionCurrency.changeAmount(amount.negate());
        toTransactionCurrency.changeAmount(amount);

        accountClient.updateAccountAmount(accountId, amount.negate());
        ResponseEntity<Response<AccountUpdateResponseDTO>> response = accountClient.updateAccountAmount(targetAccountId, amount);
        return response.getBody().getData();
    }

    private Integer getAccountIdFromUserIdAndType(int userId, AccountType accountType) {
        AccountResponseDTO accountResponse = accountClient.getAccountByUserIdAndAccountType(userId, accountType);
        return accountResponse.getAccountId();
    }

    private Integer getAccountIdFromTripId(int tripId) {
        TripGoalResponseDTO tripGoalResponseDTO = tripClient.getTripGoal(tripId);
        return tripGoalResponseDTO.getAccountId();
    }

    private Integer getAccountIdFromAccountNumber(String accountNumber) {
        ResponseEntity<Response<AccountResponseDTO>> accountResponse = accountClient.getAccountByAccountNumber(accountNumber);
        return accountResponse.getBody().getData().getAccountId();
    }

    private void validateSufficientBalance(ExchangeCurrency fromCurrency, BigDecimal fromAmount) {
        if (fromCurrency.getAmount().compareTo(fromAmount) < 0) {
            throw new RuntimeException("잔액 부족: 출금 금액이 계좌 잔액보다 큽니다.");
        }
    }

    private ExchangeCurrency getOrCreateExchangeCurrency(Integer accountId, String currencyCode) {
        return exchangeCurrencyRepository
                .findByAccountIdAndCurrencyCode(accountId, currencyCode)
                .orElseGet(() -> {
                    ExchangeCurrency newCurrency = ExchangeCurrency.builder()
                            .accountId(accountId)
                            .currencyCode(currencyCode)
                            .amount(BigDecimal.ZERO)
                            .build();
                    return exchangeCurrencyRepository.save(newCurrency);
                });
    }
}
