package com.example.module_trip.tripGoal;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/trips")
public class TripGoalController {

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
//    @GetMapping("/{tripId}")
//    public TripGoalResponseDTO getTripGoal(@PathVariable Integer tripId) {
//        return tripGoalService.findTripGoalById(tripId);
//
//    }
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
