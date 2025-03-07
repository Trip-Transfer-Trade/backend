package com.example.module_trip.account;

import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("")
    public void saveAccount(@RequestBody AccountCreateRequestDTO accountCreateRequestDTO){
        accountService.saveAccount(accountCreateRequestDTO);
    }

    @GetMapping(value = "", params = "accountType")
    public ResponseEntity<Response<AccountResponseDTO>> getAccountByUserIdAndAccountType(@RequestParam("userId") Integer userId, @RequestParam("accountType") AccountType type){
        AccountResponseDTO response = accountService.getAccountByIdAndType(userId, type);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping(value = "", params = "!accountType")
    public ResponseEntity<Response<List<AccountResponseDTO>>> getAccountByUserId(@RequestParam("userId") Integer userId){
        List<AccountResponseDTO> response = accountService.getAccountByUserId(userId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Response<AccountResponseDTO>> getAccountById(@PathVariable("accountId") Integer accountId){
        AccountResponseDTO response =  accountService.getAccountByAccountId(accountId);
        return ResponseEntity.ok(Response.success(response));
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

    @GetMapping("/all")
    public ResponseEntity<Response<List<AccountResponseDTO>>> getAllAccount(@RequestHeader(value = "X-Authenticated-User", required = false) int userid){
        List<AccountResponseDTO> response = accountService.getAllAccountByUserId(userid);
        return ResponseEntity.ok(Response.success(response));
    }
}
