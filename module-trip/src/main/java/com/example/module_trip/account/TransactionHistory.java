package com.example.module_trip.account;

import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class TransactionHistory extends BaseEntity {

    @Id
    @GeneratedValue
    private Integer id;
    private TransactionType transactionType;
    private BigDecimal transactionAmount;
    private String description;
    private String targetAccountNumber;

    @ManyToOne
    @JoinColumn(name="account_id",nullable = false)
    private Account account;
}
