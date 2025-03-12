package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.time.temporal.ChronoUnit;

@Repository
public interface TripGoalRepository extends JpaRepository<TripGoal, Integer> {
    TripGoal findByAccount_Id(Integer accountId);
    Integer account(Account account);

    List<TripGoal> findAllByAccountIdIn(List<Integer> accountId);

    @Query(value = "SELECT t.id FROM trip_goal t " +
            "WHERE t.id <> :currentTripId " +
            "  AND t.end_date > CURRENT_DATE " +
            "  AND ABS(t.goal_amount - :currentGoalAmount) / :currentGoalAmount <= 0.2 " +
            "  AND ABS(TIMESTAMPDIFF(DAY, t.created_date, t.end_date) - :currentPeriod) / :currentPeriod <= 0.2",
            nativeQuery = true)
    List<Integer> findSimilarTripIds(@Param("currentTripId") int currentTripId,
                                     @Param("currentGoalAmount") BigDecimal currentGoalAmount,
                                     @Param("currentPeriod") int currentPeriod);

    default List<Integer> getSimilarTripIds(int currentTripId) {
        TripGoal currentTrip = findById(currentTripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + currentTripId));

        BigDecimal currentGoalAmount = currentTrip.getGoalAmount();

        // 목표 기간을 일수로 계산 (예: createdDate와 endDate 사이의 차이)
        int currentPeriod = (int) ChronoUnit.DAYS.between(
                currentTrip.getCreatedDate().toLocalDate(),
                currentTrip.getEndDate());

        return findSimilarTripIds(currentTripId, currentGoalAmount, currentPeriod);
    }
}
