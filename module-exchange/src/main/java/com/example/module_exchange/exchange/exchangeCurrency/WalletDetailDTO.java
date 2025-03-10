package com.example.module_exchange.exchange.exchangeCurrency;

import com.example.module_trip.tripGoal.TripGoal;
import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class WalletDetailDTO {
    private final int accountId;
    private final BigDecimal amount;
    private final String currencyCode;
    private final String tripName;
    private final String country;

    public static WalletDetailDTO toDto(ExchangeCurrency exchangeCurrency, TripGoalResponseDTO tripGoalResponseDTO) {
        return WalletDetailDTO.builder()
                .accountId(exchangeCurrency.getAccountId())
                .currencyCode(exchangeCurrency.getCurrencyCode())
                .amount(exchangeCurrency.getAmount())
                .tripName(tripGoalResponseDTO.getName())
                .country(tripGoalResponseDTO.getCountry())
                .build();
    }
}
