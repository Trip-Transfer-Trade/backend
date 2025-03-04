package com.example.module_trip.tripGoal;



import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripGoalController {

    private final TripGoalService tripGoalService;

    public TripGoalController(TripGoalService tripGoalService) {
        this.tripGoalService = tripGoalService;
    }

    @PostMapping("/goal")
    public ResponseEntity<Response<Void>> saveTripGoal(@RequestBody TripGoalRequestDTO tripGoalRequestDTO) {
        tripGoalService.saveTripGoal(tripGoalRequestDTO);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(@RequestHeader(value = "Authorization", required = false) String token,
                                           @RequestHeader(value = "X-Authenticated-User", required = false) String username) {
        return ResponseEntity.ok("토큰: " + token + " 사용자 ID: " + username);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<Response<TripGoalResponseDTO>> getTripGoal(@PathVariable Integer tripId) {
        TripGoalResponseDTO response = tripGoalService.findTripGoalById(tripId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Response<TripGoalResponseDTO>> getTripGoalByAccountId(@PathVariable Integer accountId) {
        TripGoalResponseDTO response = tripGoalService.findTripGoalByAccountId(accountId);
        return ResponseEntity.ok(Response.success(response));
    }

}
