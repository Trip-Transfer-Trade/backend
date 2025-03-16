package com.example.module_exchange.clients;

import com.example.module_trip.account.AccountResponseDTO;
import com.example.module_trip.account.AccountType;
import com.example.module_trip.account.AccountUpdateResponseDTO;
import com.example.module_trip.account.NormalAccountDTO;
import com.example.module_trip.tripGoal.TripGoalListResponseDTO;
import com.example.module_trip.tripGoal.TripGoalProfitUpdateDTO;
import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import com.example.module_trip.tripGoal.TripGoalUpdateDTO;
import com.example.module_utility.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name="module-trip")
public interface TripClient {

    @GetMapping("/api/trips/{tripId}")
    ResponseEntity<Response<TripGoalResponseDTO>> getTripGoal(@PathVariable("tripId") Integer tripId);

    @GetMapping("/api/trips/all")
    ResponseEntity<Response<List<TripGoalResponseDTO>>> getAllTrips();

    @GetMapping("/api/trips/account/{accountId}")
    ResponseEntity<Response<TripGoalResponseDTO>> getTripGoalByAccountId(@PathVariable("accountId") Integer accountId);

    @PutMapping("/api/trips/realised")
    ResponseEntity<Response<TripGoalResponseDTO>> updateRealisedProfit(@RequestBody TripGoalUpdateDTO tripGoalUpdateDTO);

    @PutMapping("/api/trips/profit")
    ResponseEntity<Response<TripGoalResponseDTO>> updateProfit(@RequestBody TripGoalProfitUpdateDTO tripGoalProfitUpdateDTO);

    @GetMapping("/api/trips/all/{accountId}")
    ResponseEntity<Response<List<TripGoalResponseDTO>>> getAllTripsByAccountIdIn(@PathVariable List<Integer> accountId);

    @GetMapping("/api/trips/list")
    ResponseEntity<Response<List<TripGoalListResponseDTO>>> getListTripGoals(@RequestHeader(value = "X-Authenticated-User", required = false) int userId);

    @GetMapping("/api/trips/similar/{tripId}")
    ResponseEntity<Response<List<Integer>>> getSimilarTrips(@PathVariable Integer tripId);

    @PostMapping("/api/trips/check")
    ResponseEntity<Response<Void>> checkTripGoalTrigger(@PathVariable String rate);

    @GetMapping(value = "/api/accounts", params = "accountType")
    ResponseEntity<Response<AccountResponseDTO>> getAccountByUserIdAndAccountType(@RequestParam("userId") Integer userId, @RequestParam("accountType") AccountType accountType);

    @GetMapping(value = "/api/accounts", params = "!accountType")
    ResponseEntity<Response<List<AccountResponseDTO>>> getAccountByUserId(@RequestParam("userId") Integer userId);

    @GetMapping("/api/accounts/{accountId}")
    ResponseEntity<Response<AccountResponseDTO>> getAccountById(@PathVariable int accountId);

    @GetMapping("/api/accounts/number/{accountNumber}")
    ResponseEntity<Response<AccountResponseDTO>> getAccountByAccountNumber(@PathVariable String accountNumber);

    @PutMapping("/api/accounts/{accountId}")
    ResponseEntity<Response<AccountUpdateResponseDTO>> updateAccountAmount(@PathVariable int accountId, @RequestBody BigDecimal amount);

    @GetMapping("/api/accounts/all")
    ResponseEntity<Response<List<AccountResponseDTO>>> getAllAccount(@RequestHeader(value = "X-Authenticated-User", required = false) int userid);

    @GetMapping("/api/accounts/normal")
    ResponseEntity<NormalAccountDTO> getNormalAccountByUserId(@RequestHeader(value = "X-Authenticated-User", required = false) int userId);

}
