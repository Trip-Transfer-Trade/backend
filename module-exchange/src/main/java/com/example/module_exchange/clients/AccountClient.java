package com.example.module_exchange.clients;

import com.example.module_trip.account.AccountResponseDTO;
import com.example.module_trip.account.AccountType;
import com.example.module_trip.account.AccountUpdateResponseDTO;
import com.example.module_utility.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "account-service", url = "http://localhost:8082/api/accounts")
public interface AccountClient {
    @GetMapping(value = "", params = "accountType")
    AccountResponseDTO getAccountByUserIdAndAccountType(@RequestParam("userId") Integer userId, @RequestParam("accountType") AccountType accountType);

    @GetMapping(value="",params="!accountType")
    ResponseEntity<Response<List<AccountResponseDTO>>> getAccountByUserId(@RequestParam("userId") Integer userId);

    @GetMapping("/{accountId}")
    AccountResponseDTO getAccountById(@PathVariable int accountId);

    @GetMapping("/number/{accountNumber}")
    ResponseEntity<Response<AccountResponseDTO>> getAccountByAccountNumber(@PathVariable String accountNumber);

    @PutMapping("/{accountId}")
    ResponseEntity<Response<AccountUpdateResponseDTO>> updateAccountAmount(@PathVariable int accountId, @RequestBody BigDecimal amount);
}