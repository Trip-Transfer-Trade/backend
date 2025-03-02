package com.example.module_exchange.exchange.exchangeCurrency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeCurrencyRepository extends JpaRepository<ExchangeCurrency, Integer> {

    // 계좌 ID(accountId)와 통화 코드(currencyCode)로 환전 통화 데이터를 찾는 메서드
    @Query("SELECT e FROM ExchangeCurrency e WHERE e.accountId = :accountId AND e.currencyCode = :currencyCode")
    Optional<ExchangeCurrency> findByAccountIdAndCurrencyCode(
            @Param("accountId") Integer accountId,
            @Param("currencyCode") String currencyCode
    );
}
