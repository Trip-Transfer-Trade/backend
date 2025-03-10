package com.example.module_trip.account;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NormalAccountDTO {
    private Integer accountId;
    private String accountNumber;

    @Builder
    public NormalAccountDTO(Integer accountId, String accountNumber) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
    }

    // Account 엔티티를 NormalAccountDTO로 변환하는 정적 메서드
    public static NormalAccountDTO toDTO(Account account) {
        return NormalAccountDTO.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .build();
    }
}
