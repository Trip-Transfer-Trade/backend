package com.example.module_exchange.exchange;

import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExchangeTranscationService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ExchangeCurrencyRepository exchangeCurrencyRepository;

    @Transactional
    public void executeTransactionalOperations(BigDecimal amount, ExchangeCurrency fromTransactionCurrency,
                                               ExchangeCurrency toTransactionCurrency,
                                               TransactionHistory fromTransactionHistory,
                                               TransactionHistory toTransactionHistory) {
        transactionHistoryRepository.save(fromTransactionHistory);
        transactionHistoryRepository.save(toTransactionHistory);

        fromTransactionCurrency.changeAmount(amount.negate());
        exchangeCurrencyRepository.save(fromTransactionCurrency);

        toTransactionCurrency.changeAmount(amount);
        exchangeCurrencyRepository.save(toTransactionCurrency);

    }
}
