package com.example.module_trip.tripGoal;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/trips")
public class TripGoalController {
    private static final Logger logger = LoggerFactory.getLogger(TripGoalController.class);

    private final TripGoalService tripGoalService;

    public TripGoalController(TripGoalService tripGoalService) {
        this.tripGoalService = tripGoalService;
    }
    @PostMapping("/goal")
    public void saveTripGoal(@RequestBody TripGoalRequestDTO tripGoalRequestDTO) {
        tripGoalService.saveTripGoal(tripGoalRequestDTO);
    }
    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(@RequestHeader(value = "Authorization", required = false) String token,
                                           @RequestHeader(value = "X-Authenticated-User", required = false) String username) {
        return ResponseEntity.ok("토큰: " + token + " 사용자 ID: " + username);
    }

//    @GetMapping("")
//    public TripGoalResponseDTO getTripGoals(@RequestHeader("X-Authenticated-User") Integer userId) {
//        return tripGoalService.findTripGoalById(userId);

    @GetMapping("/{tripId}")
    public TripGoalResponseDTO getTripGoal(@PathVariable Integer tripId) {
        logger.info("📌 getTripGoal 호출됨 - tripId: {}", tripId);
        return tripGoalService.findTripGoalById(tripId);
    }

//    @GetMapping("/name/{tripId}")
//    public ResponseEntity<Response<String>> getTripGoalName(@PathVariable("tripId") Integer tripId) {
//        return ResponseEntity.ok(Response.success(tripGoalService.findTripGoalNameById(tripId)));
//
//    }
//    @GetMapping("")
//    public TripGoalResponseDTO getTripGoals(@RequestParam Integer userId) {
//        return tripGoalService.findTripGoalById(userId);
//    }
}
