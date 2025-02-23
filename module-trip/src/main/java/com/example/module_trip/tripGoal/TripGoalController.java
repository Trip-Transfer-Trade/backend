package com.example.module_trip.tripGoal;


import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/trips")
public class TripGoalController {

    private final TripGoalService tripGoalService;

    public TripGoalController(TripGoalService tripGoalService) {
        this.tripGoalService = tripGoalService;
    }

    @PostMapping("")
    public void saveTripGoal(@RequestBody TripGoalRequestDTO tripGoalRequestDTO) {
        tripGoalService.saveTripGoal(tripGoalRequestDTO);
    }
    @GetMapping("")
    public TripGoalResponseDTO getTripGoals(@RequestParam Integer userId) {
        return tripGoalService.findTripGoalById(userId);

    }
}
