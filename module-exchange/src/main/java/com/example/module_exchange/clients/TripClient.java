package com.example.module_exchange.clients;

import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="trip-service", url="http://localhost:8082/api/trips")
public interface TripClient {

    @GetMapping("/{tripId}")
    TripGoalResponseDTO getTripGoal(@PathVariable("tripId") Integer tripId);
}
