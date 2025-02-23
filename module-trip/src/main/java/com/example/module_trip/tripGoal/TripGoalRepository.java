package com.example.module_trip.tripGoal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripGoalRepository extends JpaRepository<TripGoal, Integer> {


}
