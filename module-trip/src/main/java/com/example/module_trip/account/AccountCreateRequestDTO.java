package com.example.module_trip.account;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreateRequestDTO {

    private AccountType accountType;

    public Account toEntity(Integer userId, String accountNumber) {
        return Account.builder()
                .userId(userId)
                .accountType(accountType)
                .accountNumber(accountNumber)
                .build();
    }
}
