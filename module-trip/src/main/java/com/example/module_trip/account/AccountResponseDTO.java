package com.example.module_trip.account;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountResponseDTO {

    private String accountNumber;
    private BigDecimal availableBalance;
    private BigDecimal totalValue;


    @Builder
    public AccountResponseDTO(String accountNumber, BigDecimal availableBalance, BigDecimal totalValue) {
        this.accountNumber = accountNumber;
        this.availableBalance = availableBalance;
        this.totalValue = totalValue;
    }
    public static AccountResponseDTO toDTO(Account account) {
        return AccountResponseDTO.builder()
                .accountNumber(account.getAccountNumber())
                .availableBalance(account.getAvailableBalance())
                .totalValue(account.getTotalValue())
                .build();
    }


}
