package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripGoalRepository extends JpaRepository<TripGoal, Integer> {
    TripGoal findByAccount_Id(Integer accountId);
    Integer account(Account account);
}
