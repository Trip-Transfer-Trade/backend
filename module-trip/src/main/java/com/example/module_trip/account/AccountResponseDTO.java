package com.example.module_trip.account;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountResponseDTO {

    private Integer accountId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal availableBalance;
    private BigDecimal totalValue;

    private Integer userId;


    @Builder
    public AccountResponseDTO(Integer accountId, String accountNumber, BigDecimal availableBalance, BigDecimal totalValue) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.availableBalance = availableBalance;
        this.totalValue = totalValue;
    }

    public static AccountResponseDTO toDTO(Account account) {
        return AccountResponseDTO.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .totalValue(account.getTotalValue())
                .build();
    }




}
