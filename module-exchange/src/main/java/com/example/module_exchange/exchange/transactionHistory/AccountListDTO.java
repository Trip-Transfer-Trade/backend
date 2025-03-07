package com.example.module_exchange.exchange.transactionHistory;

import com.example.module_trip.account.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountListDTO {
    private AccountType accountType;
    private BigDecimal amount;
    private String name;
}
