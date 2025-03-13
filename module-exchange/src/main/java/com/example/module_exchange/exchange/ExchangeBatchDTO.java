package com.example.module_exchange.exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class ExchangeBatchDTO {
    private Integer accountId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal fromAmount;
    private List<BatchDTO> batchDTOList;

}

@Getter
class BatchDTO {
    private BigDecimal amount;
    private Integer accountId;
}

