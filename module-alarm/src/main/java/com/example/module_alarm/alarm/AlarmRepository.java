package com.example.module_alarm.alarm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<AlarmHistory,Integer> {
    List<AlarmHistory> findByUserIdOrderByIdDesc(Integer userId);
}
