package com.example.module_exchange.clients;

import com.example.module_trip.account.NormalAccountDTO;
import com.example.module_trip.account.AccountResponseDTO;
import com.example.module_trip.account.AccountType;
import com.example.module_trip.account.AccountUpdateResponseDTO;
import com.example.module_utility.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "account-service")
public interface AccountClient {
    @GetMapping(value = "", params = "accountType")
    ResponseEntity<Response<AccountResponseDTO>> getAccountByUserIdAndAccountType(@RequestParam("userId") Integer userId, @RequestParam("accountType") AccountType accountType);

    @GetMapping(value = "", params = "!accountType")
    ResponseEntity<Response<List<AccountResponseDTO>>> getAccountByUserId(@RequestParam("userId") Integer userId);

    @GetMapping("/{accountId}")
    ResponseEntity<Response<AccountResponseDTO>> getAccountById(@PathVariable int accountId);

    @GetMapping("/number/{accountNumber}")
    ResponseEntity<Response<AccountResponseDTO>> getAccountByAccountNumber(@PathVariable String accountNumber);

    @PutMapping("/{accountId}")
    ResponseEntity<Response<AccountUpdateResponseDTO>> updateAccountAmount(@PathVariable int accountId, @RequestBody BigDecimal amount);

    @GetMapping("/all")
    ResponseEntity<Response<List<AccountResponseDTO>>> getAllAccount(@RequestHeader(value = "X-Authenticated-User", required = false) int userid);

    @GetMapping("/normal")
    ResponseEntity<NormalAccountDTO> getNormalAccountByUserId(@RequestHeader(value = "X-Authenticated-User", required = false) int userId);

}