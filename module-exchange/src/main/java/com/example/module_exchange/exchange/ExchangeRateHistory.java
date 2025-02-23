package com.example.module_exchange.exchange;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class ExchangeRateHistory extends BaseEntity {

    @Id
    @GeneratedValue
    private Integer id;

    private String currencyCode;
    private BigDecimal exchangeRate;
}
