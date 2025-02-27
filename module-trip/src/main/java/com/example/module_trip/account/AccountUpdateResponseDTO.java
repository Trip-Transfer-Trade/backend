package com.example.module_trip.account;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountUpdateResponseDTO {
    private Integer id;
    private BigDecimal totalValue;

    public static AccountUpdateResponseDTO toDTO(Account account) {
        return AccountUpdateResponseDTO.builder()
                .id(account.getId())
                .totalValue(account.getTotalValue())
                .build();
    }

}
