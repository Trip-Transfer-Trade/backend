package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class TripGoal extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String country;
    private BigDecimal goalAmount;
    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal realisedProfit = BigDecimal.ZERO;
    private LocalDate endDate;

    @OneToOne
    @JoinColumn(name="account_id",nullable = false)
    private Account account;

    @Builder
    public TripGoal(String name, String country, BigDecimal goalAmount, BigDecimal profit, BigDecimal realisedProfit, LocalDate endDate, Account account){
        this.name = name;
        this.country = country;
        this.goalAmount = goalAmount;
        this.profit = (profit != null) ? profit : BigDecimal.ZERO;
        this.realisedProfit = (realisedProfit != null) ? realisedProfit : BigDecimal.ZERO;
        this.endDate = endDate;
        this.account = account;
    }

}
