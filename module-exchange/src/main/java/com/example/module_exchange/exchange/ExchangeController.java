package com.example.module_exchange.exchange;

import com.example.module_exchange.exchange.transactionHistory.TransactionDTO;
import com.example.module_trip.account.AccountUpdateResponseDTO;
import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping("")
    public void saveExchange(@RequestBody ExchangeDTO exchangeDTO) {
        exchangeService.executeExchangeProcess(exchangeDTO);
    }

    @PostMapping("/transactions")
    public ResponseEntity<Response<AccountUpdateResponseDTO>> saveTransaction(@RequestBody TransactionDTO transactionDTO) {
        AccountUpdateResponseDTO accountUpdateResponseDTO = exchangeService.excuteTransactionProcess(transactionDTO);
        Response<AccountUpdateResponseDTO> response = Response.success(accountUpdateResponseDTO);
        return ResponseEntity.ok(response);
    }
}
