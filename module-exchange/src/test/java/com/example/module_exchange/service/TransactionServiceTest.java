package com.example.module_exchange.service;

import com.example.module_exchange.exchange.ExchangeTranscationService;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private ExchangeCurrencyRepository exchangeCurrencyRepository;

    @InjectMocks
    private ExchangeTranscationService service;

    @Test
    void 정상적으로_트랜잭션_수행되면_save_호출() {
        // given
        ExchangeCurrency fromCurrency = mock(ExchangeCurrency.class);
        ExchangeCurrency toCurrency = mock(ExchangeCurrency.class);
        TransactionHistory fromHistory = mock(TransactionHistory.class);
        TransactionHistory toHistory = mock(TransactionHistory.class);

        // when
        service.executeTransactionalOperations(BigDecimal.TEN, fromCurrency, toCurrency, fromHistory, toHistory);

        // then
        verify(transactionHistoryRepository, times(1)).save(fromHistory);
        verify(transactionHistoryRepository, times(1)).save(toHistory);
        verify(exchangeCurrencyRepository, times(1)).save(fromCurrency);
        verify(exchangeCurrencyRepository, times(1)).save(toCurrency);
    }

    @Test
    void 예외발생시_롤백_호출안됨() {
        // given
        ExchangeCurrency fromCurrency = mock(ExchangeCurrency.class);
        ExchangeCurrency toCurrency = mock(ExchangeCurrency.class);
        TransactionHistory fromHistory = mock(TransactionHistory.class);
        TransactionHistory toHistory = mock(TransactionHistory.class);

        doThrow(new RuntimeException()).when(transactionHistoryRepository).save(fromHistory);

        // when & then
        assertThrows(RuntimeException.class, () ->
                service.executeTransactionalOperations(BigDecimal.TEN, fromCurrency, toCurrency, fromHistory, toHistory));

        // verify: 첫 번째 save만 호출되고 이후 호출은 중단
        verify(transactionHistoryRepository, times(1)).save(fromHistory);
        verify(transactionHistoryRepository, never()).save(toHistory);
        verify(exchangeCurrencyRepository, never()).save(any());
    }
}
