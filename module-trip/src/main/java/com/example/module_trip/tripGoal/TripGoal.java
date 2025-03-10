package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import com.example.module_utility.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
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
    private BigDecimal profitUs = BigDecimal.ZERO;
    private BigDecimal realisedProfitUs = BigDecimal.ZERO;
    private LocalDate endDate;

    @OneToOne
    @JoinColumn(name="account_id",nullable = false)
    private Account account;

    @Builder
    public TripGoal(String name, String country, BigDecimal goalAmount, BigDecimal profit, BigDecimal realisedProfit, BigDecimal profitUs, BigDecimal realisedProfitUs, LocalDate endDate, Account account){
        this.name = name;
        this.country = country;
        this.goalAmount = goalAmount;
        this.profit = (profit != null) ? profit : BigDecimal.ZERO;
        this.realisedProfit = (realisedProfit != null) ? realisedProfit : BigDecimal.ZERO;
        this.profitUs = (profitUs != null) ? profitUs : BigDecimal.ZERO;
        this.realisedProfitUs = (realisedProfitUs != null) ? realisedProfitUs : BigDecimal.ZERO;
        this.endDate = endDate;
        this.account = account;
    }

    public void updateFromDTO(TripGoalEditDTO dto) {
        if (dto.getCountry() != null) {
            this.country = dto.getCountry();
        }
        if (dto.getGoalAmount() != null) {
            this.goalAmount = dto.getGoalAmount();
        }
        if (dto.getEndDate() != null) {
            this.endDate = LocalDate.parse(dto.getEndDate()); // String → LocalDate 변환
        }
        if (dto.getName() != null) {
            this.name = dto.getName();
        }
    }

}
