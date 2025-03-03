package com.example.module_trip.tripGoal;


import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{tripId}")
    public TripGoalResponseDTO getTripGoal(@PathVariable Integer tripId) {
        return tripGoalService.findTripGoalById(tripId);

    }

    @GetMapping("/name/{tripId}")
    public ResponseEntity<Response<String>> getTripGoalName(@PathVariable("tripId") Integer tripId) {
        return ResponseEntity.ok(Response.success(tripGoalService.findTripGoalNameById(tripId)));
    }
}
