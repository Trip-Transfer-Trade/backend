package com.example.module_exchange.exchange;

import com.example.module_exchange.exchange.exchangeCurrency.WalletResponseDTO;
import com.example.module_exchange.exchange.exchangeCurrency.WalletSummaryResponseDTO;
import com.example.module_exchange.exchange.stockTradeHistory.StockHoldingsDTO;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeDTO;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeService;
import com.example.module_exchange.exchange.transactionHistory.AccountListDTO;
import com.example.module_exchange.exchange.transactionHistory.TransactionDTO;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryResponseDTO;
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

    @PostMapping("/batch")
    public ResponseEntity<Response<Void>> saveExchangeBatch(@RequestBody ExchangeBatchDTO exchangeBatchDTO) {
        exchangeService.executeExchangeBatchProcess(exchangeBatchDTO);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @PostMapping("/transactions")
    public ResponseEntity<Response<Void>> saveTransaction(@RequestHeader (value = "X-Authenticated-Username", required = false) String username, @RequestBody TransactionDTO transactionDTO) {
        exchangeService.executeTransactionProcess(transactionDTO, username);
        return ResponseEntity.ok(Response.successWithoutData());
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

    @PostMapping("/stocks/bulksell")
    public ResponseEntity<Response<Void>> getStockTrades(@RequestBody StockTradeDTO stockTradeDTO) {
        stockTradeService.orderBulkSell(stockTradeDTO);
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

//    @GetMapping("/wallet/account/{accountId}")
//    public ResponseEntity<Response<List<WalletResponseDTO>>> getWalletBalance(@PathVariable int accountId) {
//        List<WalletResponseDTO> wallet = exchangeService.getWalletBalance(accountId);
//        return ResponseEntity.ok(Response.success(wallet));
//    }

    @GetMapping("/wallet")
    public ResponseEntity<Response<List<WalletSummaryResponseDTO>>> getUserWallet(
            @RequestHeader(value = "X-Authenticated-User", required = false) String username) {
        List<WalletSummaryResponseDTO> walletSummary = exchangeService.getUserWalletSummary(username);
        return ResponseEntity.ok(Response.success(walletSummary));
    }


    @GetMapping("/account/all")
    public ResponseEntity<Response<List<AccountListDTO>>> getUserAccount(@RequestHeader(value = "X-Authenticated-User", required = false) int userid, @RequestParam String currencyCode){
        List<AccountListDTO> response = exchangeService.getAccountList(userid, currencyCode);
        return ResponseEntity.ok(Response.success(response));
    }
}
