package com.example.module_exchange.exchange;

import com.example.module_exchange.exchange.exchangeCurrency.WalletResponseDTO;
import com.example.module_exchange.exchange.stockTradeHistory.StockHoldingsDTO;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeDTO;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeService;
import com.example.module_exchange.exchange.transactionHistory.TransactionDTO;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryResponseDTO;
import com.example.module_trip.account.AccountUpdateResponseDTO;
import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private final ExchangeService exchangeService;
    private final StockTradeService stockTradeService;

    public ExchangeController(ExchangeService exchangeService, StockTradeService stockTradeService) {
        this.exchangeService = exchangeService;
        this.stockTradeService = stockTradeService;
    }

    @PostMapping("")
    public ResponseEntity<Response<Void>> saveExchange(@RequestBody ExchangeDTO exchangeDTO) {
        exchangeService.executeExchangeProcess(exchangeDTO);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @PostMapping("/transactions")
    public ResponseEntity<Response<AccountUpdateResponseDTO>> saveTransaction(@RequestHeader (value = "X-Authenticated-User", required = false) String username, @RequestBody TransactionDTO transactionDTO) {
        AccountUpdateResponseDTO accountUpdateResponseDTO = exchangeService.executeTransactionProcess(transactionDTO, username);
        Response<AccountUpdateResponseDTO> response = Response.success(accountUpdateResponseDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stocks/buy")
    public ResponseEntity<Response<Void>> getBuyStock(@RequestBody StockTradeDTO stockTradeDTO) {
        stockTradeService.orderStockBuy(stockTradeDTO);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @PostMapping("/stocks/sell")
    public ResponseEntity<Response<Void>> getSellStock(@RequestBody StockTradeDTO stockTradeDTO) {
        stockTradeService.orderStockSell(stockTradeDTO);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @GetMapping("/stocks/holding")
    public ResponseEntity<Response<StockHoldingsDTO>> getHoldingStock(@RequestParam int tripId) {
        StockHoldingsDTO response= stockTradeService.getStockInfoFromRedis(tripId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/transactions/{accountId}")
    public ResponseEntity<Response<List<TransactionHistoryResponseDTO>>> getTransactions(@PathVariable Integer accountId) {
        List<TransactionHistoryResponseDTO> response = exchangeService.getTransactionHistory(accountId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("")
    public ResponseEntity<Response<List<WalletResponseDTO>>> findExchangeCurrecyByUsernameAndCurrencyCode(@RequestHeader (value = "X-Authenticated-User", required = false) String username, @RequestParam String currencyCode) {
        System.out.println(username+" "+currencyCode);
        List<WalletResponseDTO> response = exchangeService.findExchangeCurrecyByUsernameAndCurrencyCode(username, currencyCode);
        return ResponseEntity.ok(Response.success(response));
    }
}
