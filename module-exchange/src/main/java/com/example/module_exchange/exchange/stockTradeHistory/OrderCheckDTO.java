package com.example.module_exchange.exchange.stockTradeHistory;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class OrderCheckDTO {
    private BigDecimal amount;
    private int quantity;

    @Builder
    public OrderCheckDTO(BigDecimal amount, int quantity) {
        this.amount = amount;
        this.quantity = quantity;
    }
}
