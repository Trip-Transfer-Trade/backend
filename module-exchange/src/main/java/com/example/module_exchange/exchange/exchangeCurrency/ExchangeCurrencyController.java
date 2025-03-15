package com.example.module_exchange.exchange.exchangeCurrency;

import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeCurrencyController {

    private final ExchangeCurrencyService exchangeCurrencyService;

    public ExchangeCurrencyController(ExchangeCurrencyService exchangeCurrencyService) {
        this.exchangeCurrencyService = exchangeCurrencyService;
    }

    @GetMapping("/normalAccount/info")
    public ExchangeCurrencyTotalDTO getCurrenciesByAccountId(
            @RequestHeader(value = "X-Authenticated-User", required = false) Integer userId,
            @RequestParam("currencyCodes") List<String> currencyCodes) {
        return exchangeCurrencyService.getCurrenciesByAccountId(userId, currencyCodes);
    }

    @GetMapping("/tripAccount/info")
    public List<TripExchangeCurrencyDTO> getCurrenciesByTripAccountId(
            @RequestHeader(value = "X-Authenticated-User", required = false) Integer userId,
            @RequestParam("currencyCodes") List<String> currencyCodes) {
        return exchangeCurrencyService.getTripExchangeCurrencies(userId, currencyCodes);
    }

    @GetMapping("/detail/{tripId}/{currencyCode}")
    public TripGoalDetailDTO getTripGoalDetail(@PathVariable Integer tripId, @PathVariable String currencyCode) {
        return exchangeCurrencyService.getTripGoalDetail(tripId, currencyCode);
    }

    @PostMapping("/init/{accountNumber}")
    public ResponseEntity<Response<Void>> init(@PathVariable String accountNumber) {
        exchangeCurrencyService.setInitAmount(accountNumber);
        return ResponseEntity.ok(Response.successWithoutData());
    }
}
