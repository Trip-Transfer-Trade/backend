package com.example.module_exchange.exchange.exchangeCurrency;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeCurrency extends BaseEntity {

    @Id
    @GeneratedValue
    private Integer id;

    private String currencyCode;
    private BigDecimal amount;
    private Integer accountId;

    public void changeAmount(BigDecimal amount) {
        this.amount = this.amount.add(amount);
    }
}

