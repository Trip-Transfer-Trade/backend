package com.example.module_exchange.clients;

import com.example.module_trip.account.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "trip-service", url = "http://localhost:8083/api/accounts")
public interface AccountClient {

    @GetMapping("/{accountId}")
    AccountResponseDTO getAccountById(@PathVariable int accountId);
}