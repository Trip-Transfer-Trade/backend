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

    @Modifying
    @Query("DELETE FROM AlarmHistory a WHERE a.userId = :userId")
    void deleteAllByUserId(@Param("userId") Integer userId);

    @Query("SELECT a FROM AlarmHistory a WHERE a.userId = :userId OR a.userId = 0 ORDER BY a.id DESC")
    List<AlarmHistory> findByUserIdOrderByIdDesc(Integer userId);
}
