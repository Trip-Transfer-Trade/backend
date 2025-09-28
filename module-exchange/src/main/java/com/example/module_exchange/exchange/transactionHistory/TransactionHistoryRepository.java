package com.example.module_exchange.exchange.transactionHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Integer> {
    List<TransactionHistory> findByExchangeCurrency_AccountIdAndTransactionCategoryOrderByCreatedDateDesc(Integer accountId, TransactionCategory transactionCategory);
}
