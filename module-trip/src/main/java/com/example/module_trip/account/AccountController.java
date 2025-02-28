package com.example.module_trip.account;

import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @GetMapping("/{accountId}")
    public AccountResponseDTO getAccountById(@PathVariable("accountId") Integer accountId){
        return  accountService.getAccountByAccountId(accountId);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<Response<AccountResponseDTO>> getAccountByAccountNumber(@PathVariable("accountNumber") String accountNumber){
        Response<AccountResponseDTO> response = Response.success(accountService.getAccountByAccountNumber(accountNumber));
        return ResponseEntity.ok(response);

    }

    @PutMapping("/{accountId}")
    public ResponseEntity<Response<AccountUpdateResponseDTO>> updateAccountAmount(@RequestBody BigDecimal amount, @PathVariable("accountId") Integer accountId){
        Response<AccountUpdateResponseDTO> response = Response.success(accountService.updateTotalValue(accountId, amount));
        return ResponseEntity.ok(response);
    }
}
