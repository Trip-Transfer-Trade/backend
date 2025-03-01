package com.example.module_exchange.exchange.stockTradeHistory;

import com.example.module_exchange.clients.AccountClient;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrency;
import com.example.module_exchange.exchange.exchangeCurrency.ExchangeCurrencyRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistory;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryRepository;
import com.example.module_exchange.exchange.transactionHistory.TransactionType;
import com.example.module_trip.account.AccountResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StockTradeService {
    private final AccountClient accountClient;
    private final StockTradeHistoryRepository stockTradeHistoryRepository;
    private final ExchangeCurrencyRepository exchangeCurrencyRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    public StockTradeService(AccountClient accountClient, StockTradeHistoryRepository stockTradeHistoryRepository, ExchangeCurrencyRepository exchangeCurrencyRepository, TransactionHistoryRepository transactionHistoryRepository) {
        this.accountClient = accountClient;
        this.stockTradeHistoryRepository = stockTradeHistoryRepository;
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
    }

    @Transactional
    public void orderStockBuy(StockTradeDTO stockTradeDTO) {
        Integer accountId = getAccountIdFromUserId(stockTradeDTO.getUserId());
        BigDecimal amount = stockTradeDTO.getAmount();
        TradeType tradeType = TradeType.BUY;

        ExchangeCurrency exchangeCurrency = getExchangeCurrencyFromUserId(accountId, stockTradeDTO.getCurrencyCode());

        validateSufficientBalance(exchangeCurrency, stockTradeDTO);


        StockTradeHistory stockTradeHistory = stockTradeDTO.toStockTradeHistory(exchangeCurrency, tradeType);
        stockTradeHistoryRepository.save(stockTradeHistory);

        TransactionHistory transactionHistory = stockTradeDTO.toTransactionHistory(exchangeCurrency, TransactionType.WITHDRAWAL, amount);
        transactionHistoryRepository.save(transactionHistory);

        exchangeCurrency.changeAmount(amount.negate());
    }

    @Transactional
    public void orderStockSell(StockTradeDTO stockTradeDTO) {
        Integer accountId = getAccountIdFromUserId(stockTradeDTO.getUserId());
        BigDecimal amount = stockTradeDTO.getAmount();
        TradeType tradeType = TradeType.SELL;

        ExchangeCurrency exchangeCurrency = getExchangeCurrencyFromUserId(accountId, stockTradeDTO.getCurrencyCode());

        validateSufficientQuantity(exchangeCurrency, stockTradeDTO);

        StockTradeHistory stockTradeHistory = stockTradeDTO.toStockTradeHistory(exchangeCurrency, tradeType);
        stockTradeHistoryRepository.save(stockTradeHistory);

        TransactionHistory transactionHistory = stockTradeDTO.toTransactionHistory(exchangeCurrency, TransactionType.DEPOSIT, amount);
        transactionHistoryRepository.save(transactionHistory);

        exchangeCurrency.changeAmount(amount);
    }

    private Integer getAccountIdFromUserId(int userId){
        AccountResponseDTO accountResponse = accountClient.getAccountById(userId);
        return accountResponse.getAccountId();
    }

    private ExchangeCurrency getExchangeCurrencyFromUserId(Integer accountId, String currencyCode){
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

    private void validateSufficientBalance(ExchangeCurrency currency, StockTradeDTO stockTradeDTO) {
        BigDecimal totalPurchaseAmount = stockTradeDTO.getAmount().multiply(stockTradeDTO.getAmount());
        if(totalPurchaseAmount.compareTo(currency.getAmount()) > 0){
            throw new RuntimeException("매수 실패: 보유한 예수금이 주문 금액보다 작습니다.");
        }
    }

    private void validateSufficientQuantity(ExchangeCurrency currency, StockTradeDTO stockTradeDTO) {
        BigDecimal totalBuyQuantity = stockTradeHistoryRepository.findTotalBuyQuantityByStockCode(stockTradeDTO.getStockCode());
        BigDecimal totalSellQuantity = stockTradeHistoryRepository.findTotalSellQuantityByStockCode(stockTradeDTO.getStockCode());
        BigDecimal ownedQuantity = totalBuyQuantity.subtract(totalSellQuantity);

        BigDecimal orderSellQuantity = BigDecimal.valueOf(stockTradeDTO.getQuantity());

        if(orderSellQuantity.compareTo(ownedQuantity) > 0){
            throw new RuntimeException("매도 실패: 보유한 수량이 주문 수량보다 작습니다.");
        }


    }
}
