package com.example.module_exchange.exchange.transactionHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Integer> {

    Collection<TransactionHistory> findByExchangeCurrency_AccountIdOrderByCreatedDateDesc(Integer accountId);
}
