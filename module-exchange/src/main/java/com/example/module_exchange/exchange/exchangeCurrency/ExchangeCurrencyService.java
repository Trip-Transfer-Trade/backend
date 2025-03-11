package com.example.module_exchange.exchange.exchangeCurrency;

import com.example.module_exchange.clients.AccountClient;
import com.example.module_exchange.clients.TripClient;
import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartService;
import com.example.module_trip.account.NormalAccountDTO;
import com.example.module_trip.tripGoal.TripGoalListResponseDTO;
import com.example.module_utility.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExchangeCurrencyService {

    private final ExchangeCurrencyRepository exchangeCurrencyRepository;
    private final ExchangeRateChartService exchangeRateChartService;
    private final AccountClient accountClient;
    private final TripClient tripClient;

    public ExchangeCurrencyService(ExchangeCurrencyRepository exchangeCurrencyRepository,
                                   ExchangeRateChartService exchangeRateChartService,
                                   AccountClient accountClient,
                                   TripClient tripClient) {
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
        this.exchangeRateChartService = exchangeRateChartService;
        this.accountClient = accountClient;
        this.tripClient = tripClient;
    }
    //일반 계좌 정보
    public ExchangeCurrencyTotalDTO getCurrenciesByAccountId(Integer userId, List<String> currencyCodes) {
        NormalAccountDTO normalAccountDTO = accountClient.getNormalAccountByUserId(userId).getBody();
        if (normalAccountDTO == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found for user ID: " + userId);
        }

        Integer accountId = normalAccountDTO.getAccountId();
        String accountNumber = normalAccountDTO.getAccountNumber();

        // 사용자의 특정 통화 보유량 조회
        List<ExchangeCurrency> currencies = exchangeCurrencyRepository.findByAccountIdAndCurrencyCodeIn(accountId, currencyCodes);

        // 전체 원화 합계 계산 (USD 변환 포함)
        BigDecimal totalAmountInKRW = calculateTotalAmountInKRW(currencies);

        return new ExchangeCurrencyTotalDTO(accountNumber, totalAmountInKRW);
    }
    //여행 목표 계좌 정보
    public List<TripExchangeCurrencyDTO> getTripExchangeCurrencies(int userId, List<String> currencyCodes) {
        // 1. 여행 목표 리스트 조회
        ResponseEntity<Response<List<TripGoalListResponseDTO>>> tripGoalListResponse = tripClient.getListTripGoals(userId);
        if (tripGoalListResponse.getBody() == null || tripGoalListResponse.getBody().getData() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No trip goals found.");
        }
        List<TripGoalListResponseDTO> tripGoals = tripGoalListResponse.getBody().getData();

        ExchangeCurrencyTotalDTO exchangeTotal = getCurrenciesByAccountId(userId, currencyCodes);

        BigDecimal usdExchangeRate = getUsdExchangeRate();

        return tripGoals.stream()
                .map(goal -> {
                    BigDecimal profit = goal.getProfit();
                    BigDecimal profitUs = goal.getProfitUs();

                    BigDecimal totalProfit = profit.add(profitUs.multiply(usdExchangeRate));

                    return new TripExchangeCurrencyDTO(
                            goal.getId(),
                            goal.getName(),
                            goal.getCountry(),
                            goal.getGoalAmount(),
                            goal.getEndDate(),
                            exchangeTotal.getTotalAmountInKRW(), // 사용자의 총 원화 금액
                            totalProfit // 목표별 총 수익
                    );
                })
                .collect(Collectors.toList());
    }


    private BigDecimal calculateTotalAmountInKRW(List<ExchangeCurrency> currencies) {
        BigDecimal usdToKrwRate = getUsdExchangeRate(); // USD 환율 가져오기

        return currencies.stream()
                .map(currency -> {
                    BigDecimal amount = currency.getAmount();
                    return "USD".equals(currency.getCurrencyCode()) ? amount.multiply(usdToKrwRate) : amount;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getUsdExchangeRate() {
        String usdRateStr = exchangeRateChartService.getUSExchangeRate().getRate().replace(",", "");
        return new BigDecimal(usdRateStr);
    }
}
