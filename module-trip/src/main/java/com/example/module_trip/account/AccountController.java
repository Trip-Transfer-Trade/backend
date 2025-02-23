package com.example.module_trip.account;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("")
    public void saveAccount(@RequestBody AccountCreateRequestDTO accountCreateRequestDTO){
        accountService.saveAccount(accountCreateRequestDTO);

    }
    @GetMapping("")
    public List<AccountResponseDTO> getAccount(@RequestParam("user_id") Integer userId){
        return accountService.getAccountById(userId);
    }
}
