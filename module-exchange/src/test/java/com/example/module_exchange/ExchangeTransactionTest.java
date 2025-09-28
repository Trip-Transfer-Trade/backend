package com.example.module_exchange;

import com.example.module_exchange.exchange.ExchangeTranscationService;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import com.example.module_exchange.exchange.transactionHistory.TransactionCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
@SpringBootTest
class ExchangeTransactionTest {

    @Autowired
    private ExchangeTranscationService service;

    @Autowired
    private TransactionHistoryRepository historyRepository;

    @Autowired
    private ExchangeCurrencyRepository currencyRepository;

    @Test
    @Transactional
    void 예외발생시_DB롤백된다() {
        // given: ExchangeCurrency 저장
        ExchangeCurrency fromCurrency = ExchangeCurrency.builder()
                .currencyCode("USD")
                .amount(BigDecimal.valueOf(1000))
                .availableAmount(BigDecimal.valueOf(1000))
                .accountId(1)
                .build();

        ExchangeCurrency toCurrency = ExchangeCurrency.builder()
                .currencyCode("USD")
                .amount(BigDecimal.valueOf(500))
                .availableAmount(BigDecimal.valueOf(500))
                .accountId(2)
                .build();

        currencyRepository.save(fromCurrency);
        currencyRepository.save(toCurrency);

        // TransactionHistory 생성
        TransactionHistory fromHistory = TransactionHistory.builder()
                .transactionType(TransactionType.WITHDRAWAL)
                .transactionCategory(TransactionCategory.EXCHANGE)
                .transactionAmount(BigDecimal.valueOf(100))
                .description("테스트 출금")
                .targetAccountNumber("123-456-789")
                .exchangeCurrency(fromCurrency)
                .build();

        TransactionHistory toHistory = TransactionHistory.builder()
                .transactionType(TransactionType.DEPOSIT)
                .transactionCategory(TransactionCategory.EXCHANGE)
                .transactionAmount(BigDecimal.valueOf(100))
                .description("테스트 입금")
                .targetAccountNumber("123-456-789")
                .exchangeCurrency(toCurrency)
                .build();

        // when & then: 강제 예외 발생 → 롤백 확인
        assertThrows(RuntimeException.class, () -> {
            service.executeTransactionalOperations(
                    BigDecimal.valueOf(100),
                    fromCurrency,
                    toCurrency,
                    fromHistory,
                    toHistory
            );
        });

        // rollback 확인
        assertEquals(0, historyRepository.count(), "트랜잭션 실패 시 history가 저장되지 않아야 함");
        assertEquals(0, currencyRepository.count(), "트랜잭션 실패 시 currency 변경도 반영되지 않아야 함");
    }
}
