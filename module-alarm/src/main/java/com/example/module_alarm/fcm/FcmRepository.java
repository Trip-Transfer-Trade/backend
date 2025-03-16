package com.example.module_alarm.fcm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmRepository extends JpaRepository<Fcm, Integer> {
    List<Fcm> findByUserId(Integer userId);
    Optional<Fcm> findByToken(String token);

    void deleteByToken(String token);
}
