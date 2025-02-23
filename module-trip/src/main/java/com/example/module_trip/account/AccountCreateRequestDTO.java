package com.example.module_trip.account;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class AccountCreateRequestDTO {

    private Integer userId;
    private AccountType accountType;


    public Account toEntity(String accountNumber) {
        return Account.builder()
                .userId(userId)
                .accountType(accountType)
                .accountNumber(accountNumber)
                .build();
    }
}
