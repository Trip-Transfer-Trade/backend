package com.example.module_trip.tripGoal;

import com.example.module_utility.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/trips")
public class TripGoalController {
    private static final Logger logger = LoggerFactory.getLogger(TripGoalController.class);

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
        logger.info("📌 getTripGoal 호출됨 - tripId: {}", tripId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Response<TripGoalResponseDTO>> getTripGoalByAccountId(@PathVariable Integer accountId) {
        TripGoalResponseDTO response = tripGoalService.findTripGoalByAccountId(accountId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/all")
    public ResponseEntity<Response<List<TripGoalResponseDTO>>> getAllTrips(){
        List<TripGoalResponseDTO> tripGoals = tripGoalService.findAllTripGoal();
        return ResponseEntity.ok(new Response<>(200, "success", tripGoals));
    }

}
