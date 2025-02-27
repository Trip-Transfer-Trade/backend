package com.example.module_trip.account;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String accountNumber;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private BigDecimal totalValue = BigDecimal.ZERO;

    private Integer userId;

    @Builder
    public Account(Integer userId, String accountNumber, AccountType accountType){
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
    }

    public void changeTotalValue(BigDecimal amount){
        this.totalValue = this.totalValue.add(amount);
    }

}