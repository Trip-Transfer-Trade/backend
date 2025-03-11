package com.example.module_exchange.exchange.exchangeCurrency;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TripGoalDetailDTO {

    private BigDecimal profit;       // 누적 수익금
    private BigDecimal evaluationAmount;  // 평가 금액
    private BigDecimal purchaseAmount;    // 매입 금액
    private BigDecimal totalAssets;       // 총 자산
    private BigDecimal depositAmount;     // 예수금

    public TripGoalDetailDTO(BigDecimal profit,BigDecimal evaluationAmount, BigDecimal purchaseAmount,BigDecimal totalAssets, BigDecimal depositAmount) {
        this.profit = profit;
        this.evaluationAmount = evaluationAmount;
        this.purchaseAmount = purchaseAmount;
        this.totalAssets = totalAssets;
        this.depositAmount = depositAmount;
    }
}
