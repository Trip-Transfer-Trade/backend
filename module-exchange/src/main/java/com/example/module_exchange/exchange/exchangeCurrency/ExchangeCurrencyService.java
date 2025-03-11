package com.example.module_exchange.exchange.exchangeCurrency;

import com.example.module_exchange.clients.AccountClient;
import com.example.module_exchange.clients.TripClient;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeHistoryRepository;
import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartService;
import com.example.module_trip.account.NormalAccountDTO;
import com.example.module_trip.tripGoal.TripGoalListResponseDTO;
import com.example.module_trip.tripGoal.TripGoalResponseDTO;
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
    private final StockTradeHistoryRepository stockTradeHistoryRepository;
    private final AccountClient accountClient;
    private final TripClient tripClient;

    public ExchangeCurrencyService(ExchangeCurrencyRepository exchangeCurrencyRepository,
                                   ExchangeRateChartService exchangeRateChartService,
                                   StockTradeHistoryRepository stockTradeHistoryRepository,
                                   AccountClient accountClient,
                                   TripClient tripClient) {
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
        this.exchangeRateChartService = exchangeRateChartService;
        this.stockTradeHistoryRepository = stockTradeHistoryRepository;
        this.accountClient = accountClient;
        this.tripClient = tripClient;
    }

    // 일반 계좌 정보 조회
    public ExchangeCurrencyTotalDTO getCurrenciesByAccountId(Integer userId, List<String> currencyCodes) {
        NormalAccountDTO normalAccountDTO = accountClient.getNormalAccountByUserId(userId).getBody();
        if (normalAccountDTO == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found for user ID: " + userId);
        }

        Integer accountId = normalAccountDTO.getAccountId();
        String accountNumber = normalAccountDTO.getAccountNumber();

        // 사용자의 특정 통화 보유량 조회
        List<ExchangeCurrency> currencies = exchangeCurrencyRepository.findByAccountIdAndCurrencyCodeIn(accountId, currencyCodes);

        BigDecimal totalAmountInKRW = calculateTotalAmountInKRW(currencies);
        BigDecimal amountKRW = getAmountByCurrency(currencies, "KRW");
        BigDecimal amountUSD = getAmountByCurrency(currencies, "USD");

        return new ExchangeCurrencyTotalDTO(accountNumber, amountKRW, amountUSD, totalAmountInKRW);
    }

    // 여행 목표 계좌 정보 조회
    public List<TripExchangeCurrencyDTO> getTripExchangeCurrencies(int userId, List<String> currencyCodes) {
        // 1. 여행 목표 리스트 조회
        ResponseEntity<Response<List<TripGoalListResponseDTO>>> tripGoalListResponse = tripClient.getListTripGoals(userId);
        if (tripGoalListResponse.getBody() == null || tripGoalListResponse.getBody().getData() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No trip goals found.");
        }
        List<TripGoalListResponseDTO> tripGoals = tripGoalListResponse.getBody().getData();

        // 2. 사용자 통화 보유량 조회
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
                            exchangeTotal.getAmount(),
                            exchangeTotal.getAmountUS(),
                            exchangeTotal.getTotalAmountInKRW(), // 사용자의 총 원화 금액
                            goal.getProfit(),
                            goal.getProfitUs(),
                            totalProfit // 목표별 총 수익
                    );
                })
                .collect(Collectors.toList());
    }

    // 특정 통화(KRW, USD)의 금액 가져오기 (단일 값)
    private BigDecimal getAmountByCurrency(List<ExchangeCurrency> currencies, String currencyCode) {
        return currencies.stream()
                .filter(currency -> currencyCode.equals(currency.getCurrencyCode()))
                .map(ExchangeCurrency::getAmount)
                .findFirst()  // 단일 값이므로 첫 번째 값만 가져옴
                .orElse(BigDecimal.ZERO);
    }

    // 전체 금액을 KRW 기준으로 변환하여 합산
    private BigDecimal calculateTotalAmountInKRW(List<ExchangeCurrency> currencies) {
        BigDecimal usdToKrwRate = getUsdExchangeRate(); // USD 환율 가져오기

        return currencies.stream()
                .map(currency -> {
                    BigDecimal amount = currency.getAmount();
                    return "USD".equals(currency.getCurrencyCode()) ? amount.multiply(usdToKrwRate) : amount;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // USD 환율 조회
    private BigDecimal getUsdExchangeRate() {
        String usdRateStr = exchangeRateChartService.getUSExchangeRate().getRate().replace(",", "");
        return new BigDecimal(usdRateStr);
    }

    public TripGoalDetailDTO getTripGoalDetail(Integer tripId, String currencyCode) {
        ResponseEntity<Response<TripGoalResponseDTO>>  tripGoalResponse = tripClient.getTripGoal(tripId);

        TripGoalResponseDTO responseDTO = tripGoalResponse.getBody().getData();

        Integer accountId = responseDTO.getAccountId(); //tripId에 해당하는 accountId
        BigDecimal profit;  //누적 수익금
        BigDecimal depositAmount; //예수금
        BigDecimal purchaseAmount; //매입금액
//        BigDecimal evaluationAmount;  //평가금액

        //누적 수익금 가져오기
        if (currencyCode.contains("KRW")) {
            profit = responseDTO.getProfit();
        } else if (currencyCode.contains("USD")) {
            profit = responseDTO.getProfitUs();
        } else {
            profit = BigDecimal.ZERO;
        }

        // 계좌에 있는 모든 통화 정보 가져오기
        List<ExchangeCurrency> currencies = exchangeCurrencyRepository.findByAccountIdAndCurrencyCodeIn(accountId, List.of(currencyCode));

        // 특정 통화(KRW, USD 등)의 예수금 가져오기
        depositAmount = getAmountByCurrency(currencies, currencyCode);

        //매입금액 가져오기
        BigDecimal totalBuyAmount = stockTradeHistoryRepository.findTotalBuyAmountByAccountAndCurrency(accountId, currencyCode);
        BigDecimal totalSellAmount = stockTradeHistoryRepository.findTotalSellAmountByAccountAndCurrency(accountId, currencyCode);
        purchaseAmount = totalBuyAmount.subtract(totalSellAmount);

        return new TripGoalDetailDTO(profit, purchaseAmount,depositAmount);

    }
}
