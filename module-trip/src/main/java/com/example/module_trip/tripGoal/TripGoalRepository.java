package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripGoalRepository extends JpaRepository<TripGoal, Integer> {
    TripGoal findByAccount_Id(Integer accountId);
    Integer account(Account account);

    List<TripGoal> findAllByAccountIdIn(List<Integer> accountId);
}
