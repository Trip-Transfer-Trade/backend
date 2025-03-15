package com.example.module_exchange.exchange.exchangeCurrency;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeCurrency extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String currencyCode;
    private BigDecimal amount; //예수금 계산할 때 사용
    private BigDecimal availableAmount;
    private Integer accountId;

    public void changeAmount(BigDecimal amount) {
        this.amount = this.amount.add(amount);
    }
}

