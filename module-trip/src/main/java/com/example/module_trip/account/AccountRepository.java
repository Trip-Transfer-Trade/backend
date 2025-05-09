package com.example.module_trip.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    List<Account> findByUserId(Integer userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByUserIdAndAccountType(Integer userId, AccountType type);

    Optional<Account> findFirstByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    List<Account> findAllByUserIdAndAccountType(Integer Id, AccountType accountType);

    boolean existsByUserIdAndAccountType(Integer userId, AccountType accountType);
}
