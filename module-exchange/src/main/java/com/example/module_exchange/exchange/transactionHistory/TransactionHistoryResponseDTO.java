package com.example.module_exchange.exchange.transactionHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
public class TransactionHistoryResponseDTO {
    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;

    public static TransactionHistoryResponseDTO toDTO(TransactionHistory transactionHistory) {
        return TransactionHistoryResponseDTO.builder()
                .amount(transactionHistory.getTransactionAmount())
                .transactionType(transactionHistory.getTransactionType())
                .description(transactionHistory.getDescription())
                .build();
    }
}
