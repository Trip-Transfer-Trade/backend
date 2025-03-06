package com.example.module_exchange.clients;

import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import com.example.module_trip.tripGoal.TripGoalUpdateDTO;
import com.example.module_utility.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="trip-service", url="http://localhost:8082/api/trips")
public interface TripClient {

    @GetMapping("/{tripId}")
    ResponseEntity<Response<TripGoalResponseDTO>> getTripGoal(@PathVariable("tripId") Integer tripId);

    @GetMapping("/all")
    ResponseEntity<Response<List<TripGoalResponseDTO>>> getAllTrips();

    @GetMapping("/account/{accountId}")
    ResponseEntity<Response<TripGoalResponseDTO>> getTripGoalByAccountId(@PathVariable("accountId") Integer accountId);

    @PutMapping("/realised")
    ResponseEntity<Response<TripGoalResponseDTO>> updateRealisedProfit(@RequestBody TripGoalUpdateDTO tripGoalUpdateDTO);
}
