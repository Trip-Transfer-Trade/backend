package com.example.module_exchange.exchange;

import com.example.module_exchange.exchange.exchangeCurrency.*;
import com.example.module_exchange.exchange.stockTradeHistory.StockHoldingsDTO;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeDTO;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeService;
import com.example.module_exchange.exchange.transactionHistory.AccountListDTO;
import com.example.module_exchange.exchange.transactionHistory.TransactionDTO;
import com.example.module_exchange.exchange.transactionHistory.TransactionHistoryResponseDTO;
import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @PostMapping("/goal")
    public ResponseEntity<Response<ExchangeGoalListDTO>> saveExchangeGoal(@RequestBody ExchangeGoalDTO exchangeGoalDTO) {
        // 일괄 매도
        stockTradeService.orderBulkSell(exchangeGoalDTO.getTripId());
        // 일괄 환전 가진 모든 통화 -> 목표 나라로
        ExchangeGoalListDTO response = exchangeService.executeExchangeGoal(exchangeGoalDTO);
        return ResponseEntity.ok(Response.success(response));
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
    public ResponseEntity<Response<Void>> getStockTrades(@RequestParam int tripId) {
        stockTradeService.orderBulkSell(tripId);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @GetMapping("/stocks/holding")
    public ResponseEntity<Response<StockHoldingsDTO>> getHoldingStock(@RequestParam int tripId, String country) {
        StockHoldingsDTO response= stockTradeService.getStockInfoFromRedis(tripId, country);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/stocks/mtmProfit")
    public ResponseEntity<Response<Void>> getMtmProfit(@RequestParam int tripId) {
        stockTradeService.calcMtmProfit(tripId);
        return ResponseEntity.ok(Response.successWithoutData());
    }

    @GetMapping("/transactions/{accountId}")
    public ResponseEntity<Response<List<TransactionHistoryResponseDTO>>> getTransactions(@PathVariable Integer accountId) {
        List<TransactionHistoryResponseDTO> response = exchangeService.getTransactionHistory(accountId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("")
    public ResponseEntity<Response<List<WalletResponseDTO>>> findExchangeCurrecyByUsernameAndCurrencyCode(@RequestHeader (value = "X-Authenticated-Username", required = false) String username, @RequestParam String currencyCode) {
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
            @RequestHeader(value = "X-Authenticated-Username", required = false) String username) {
        List<WalletSummaryResponseDTO> walletSummary = exchangeService.getUserWalletSummary(username);
        return ResponseEntity.ok(Response.success(walletSummary));
    }


    @GetMapping("/account/all")
    public ResponseEntity<Response<List<AccountListDTO>>> getUserAccount(@RequestHeader(value = "X-Authenticated-User", required = false) int userid, @RequestParam String currencyCode){
        List<AccountListDTO> response = exchangeService.getAccountList(userid, currencyCode);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/myWallet")
    public ResponseEntity<Response<List<MyWalletDTO>>> findExchangeCurrencyByUserId(@RequestHeader(value = "X-Authenticated-User", required = false) int userId) {
        List<MyWalletDTO> response = exchangeService.findExchangeCurrencyByUserId(userId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/myWallet/detail")
    public ResponseEntity<Response<List<WalletDetailDTO>>> findExchangeCurrencyByUserIdAndCurrencyCode(@RequestHeader(value = "X-Authenticated-User", required = false) int userId, @RequestParam String currencyCode) {
        List<WalletDetailDTO> response = exchangeService.findExchangeCurrencyByUserIdAndCurrencyCode(userId, currencyCode);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/myWallet/trip")
    public ResponseEntity<Response<List<AvailableDTO>>> findExchangeTrip(@RequestHeader(value = "X-Authenticated-User", required = false) int userId) {
        List<AvailableDTO> response = exchangeService.findExchangeCurrencyTripByUserId(userId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/myWallet/trip/all")
    public ResponseEntity<Response<List<AvailableAllDTO>>> findExchangeAllTrip(@RequestParam int accountId) {
        List<AvailableAllDTO> response = exchangeService.findAllExchangeCurrencyTripByUserId(accountId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/assessmentAmount")
    public ResponseEntity<Response<BigDecimal>> calcAssessmentAmount(@RequestParam int tripId, String currencyCode) {
        BigDecimal assessmentAmountSum = stockTradeService.calcAssessmentAmount(tripId, currencyCode);
        return ResponseEntity.ok(Response.success(assessmentAmountSum));
    }

}
