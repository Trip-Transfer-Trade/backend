package com.example.module_alarm.alarm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<AlarmHistory,Integer> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AlarmHistory a SET a.isRead = true WHERE a.userId = :userId AND a.isRead = false")
    void updateAllAsReadByUserId(@Param("userId") Integer userId);

    List<AlarmHistory> findByUserIdOrderByIdDesc(Integer userId);
}
