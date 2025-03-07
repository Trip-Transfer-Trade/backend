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
    private BigDecimal totalValue;


    @Builder
    public AccountResponseDTO(Integer accountId, String accountNumber, BigDecimal totalValue, AccountType accountType) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.totalValue = totalValue;
        this.accountType = accountType;
    }

    public static AccountResponseDTO toDTO(Account account) {
        return AccountResponseDTO.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .totalValue(account.getTotalValue())
                .build();
    }


}
