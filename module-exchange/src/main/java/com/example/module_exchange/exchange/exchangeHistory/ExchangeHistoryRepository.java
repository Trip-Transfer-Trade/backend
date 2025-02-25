package com.example.module_exchange.exchange.exchangeHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistory, Integer> {

}
