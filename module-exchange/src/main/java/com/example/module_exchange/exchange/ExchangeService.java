package com.example.module_exchange.exchange;

import com.example.module_exchange.clients.AccountClient;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeHistory;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import com.example.module_trip.account.AccountResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ExchangeService {

    private final AccountClient accountClient;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ExchangeCurrencyRepository exchangeCurrencyRepository;

    public ExchangeService(AccountClient accountClient, ExchangeHistoryRepository exchangeHistoryRepository, TransactionHistoryRepository transactionHistoryRepository, ExchangeCurrencyRepository exchangeCurrencyRepository) {
        this.accountClient = accountClient;
        this.exchangeHistoryRepository = exchangeHistoryRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
    }

    @Transactional
    public void executeExchangeProcess(ExchangeDTO exchangeDTO) {

        //계좌 ID 계속 사용
        Integer accountId = getAccountIdFromUserId(exchangeDTO.getUserId());

        String fromCurrencyCode = exchangeDTO.getFromCurrency();
        String toCurrencyCode = exchangeDTO.getToCurrency();

        BigDecimal fromAmount = exchangeDTO.getFromAmount();
        BigDecimal toAmount = exchangeDTO.getToAmount();

        //해당 계좌와 전 통화 entity
//        ExchangeCurrency fromExchangeCurrency = exchangeCurrencyRepository
//                .findByAccountIdAndCurrencyCode(accountId, fromCurrencyCode)
//                .orElseThrow(() -> new RuntimeException("해당 계좌 ID와 통화 코드에 해당하는 환전 통화를 찾을 수 없습니다."));
//
//        validateSufficientBalance(fromExchangeCurrency, fromAmount);
        ExchangeCurrency fromExchangeCurrency = getOrCreateExchangeCurrency(accountId, fromCurrencyCode);

        //해당 계좌와 후 통화 entity
        ExchangeCurrency toExchangeCurrency = getOrCreateExchangeCurrency(accountId, toCurrencyCode);

        //1. 환전 거래 내역 저장
        ExchangeHistory fromExchangeHistory = exchangeDTO.toExchangeHistory(fromExchangeCurrency);
        exchangeHistoryRepository.save(fromExchangeHistory);

        ExchangeHistory toExchangeHistory = exchangeDTO.toExchangeHistory(toExchangeCurrency);
        exchangeHistoryRepository.save(toExchangeHistory);

        //2. 입출금 거래 내역 저장
        //환전 전 통화 에 해당하는 입출금 거래 내역 저장
        TransactionHistory fromTransactionHistory = exchangeDTO.toTransactionHistory(fromExchangeCurrency, TransactionType.WITHDRAWAL, fromAmount);
        transactionHistoryRepository.save(fromTransactionHistory);

        //환전 전 통화에 해당하는 입출금 거래 내역 저장
        TransactionHistory toTransactionHistory = exchangeDTO.toTransactionHistory(toExchangeCurrency, TransactionType.DEPOSIT, toAmount);
        transactionHistoryRepository.save(toTransactionHistory);

        //3. 환전 통화 업데이트
        fromExchangeCurrency.changeAmount(fromAmount.negate());
        toExchangeCurrency.changeAmount(toAmount);


    }

    private Integer getAccountIdFromUserId(int userId) {
        AccountResponseDTO accountResponse = accountClient.getAccountById(userId);
        return accountResponse.getAccountId();
    }

//    private void validateSufficientBalance(ExchangeCurrency fromExchangeCurrency, BigDecimal fromAmount) {
//        if(fromExchangeCurrency.getAmount().compareTo(fromAmount) < 0) {
//            throw new RuntimeException("잔액 부족: 출금 금액이 계좌 잔액보다 큽니다.");
//        }
//    }
    private ExchangeCurrency getOrCreateExchangeCurrency(Integer accountId, String currencyCode) {
        return exchangeCurrencyRepository
                .findByAccountIdAndCurrencyCode(accountId, currencyCode)
                .orElseGet(() -> {
                    ExchangeCurrency newCurrency = ExchangeCurrency.builder()
                            .accountId(accountId)
                            .currencyCode(currencyCode)
                            .amount(BigDecimal.ZERO) // 처음 생성 시 잔액 0
                            .build();
                    return exchangeCurrencyRepository.save(newCurrency);
                });
    }

}
