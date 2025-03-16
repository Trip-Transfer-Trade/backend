package com.example.module_exchange.exchange.exchangeCurrency;

import com.example.module_exchange.clients.TripClient;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeHistoryRepository;
import com.example.module_exchange.exchange.stockTradeHistory.StockTradeService;
import com.example.module_exchange.redisData.exchangeData.exchangeRateChart.ExchangeRateChartService;
import com.example.module_trip.account.AccountResponseDTO;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExchangeCurrencyService {

    private final ExchangeCurrencyRepository exchangeCurrencyRepository;
    private final ExchangeRateChartService exchangeRateChartService;
    private final StockTradeHistoryRepository stockTradeHistoryRepository;
    private final TripClient tripClient;
    private final StockTradeService stockTradeService;

    public ExchangeCurrencyService(ExchangeCurrencyRepository exchangeCurrencyRepository,
                                   ExchangeRateChartService exchangeRateChartService,
                                   StockTradeHistoryRepository stockTradeHistoryRepository,
                                   TripClient tripClient,
                                   StockTradeService stockTradeService) {
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
        this.exchangeRateChartService = exchangeRateChartService;
        this.stockTradeHistoryRepository = stockTradeHistoryRepository;
        this.tripClient = tripClient;
        this.stockTradeService = stockTradeService;
    }

    // 일반 계좌 정보 조회
    public ExchangeCurrencyTotalDTO getCurrenciesByAccountId(Integer userId, List<String> currencyCodes) {
        NormalAccountDTO normalAccountDTO = tripClient.getNormalAccountByUserId(userId).getBody();
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

        return new ExchangeCurrencyTotalDTO(accountId, accountNumber, amountKRW, amountUSD, totalAmountInKRW);
    }

    // 여행 목표 계좌 정보 조회
    public List<TripExchangeCurrencyDTO> getTripExchangeCurrencies(int userId, List<String> currencyCodes) {
        ResponseEntity<Response<List<TripGoalListResponseDTO>>> tripGoalListResponse = tripClient.getListTripGoals(userId);
        if (tripGoalListResponse.getBody() == null || tripGoalListResponse.getBody().getData() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No trip goals found.");
        }

        List<TripGoalListResponseDTO> tripGoals = tripGoalListResponse.getBody().getData();

        List<Integer> accountIds = tripClient.getAccountByUserId(userId).getBody().getData()
                .stream().map(AccountResponseDTO::getAccountId).collect(Collectors.toList());

        List<ExchangeCurrency> exchangeCurrencies = exchangeCurrencyRepository.findByAccountIdInAndCurrencyCodeIn(accountIds, currencyCodes);

        BigDecimal usdExchangeRate = getUsdExchangeRate();

        return tripGoals.stream()
                .map(goal -> {
                    List<ExchangeCurrency> goalCurrencies = exchangeCurrencies.stream()
                            .filter(ec -> Objects.equals(ec.getAccountId(), goal.getAccountId()))
                            .collect(Collectors.toList());

                    BigDecimal amountKRW = goalCurrencies.stream()
                            .filter(ec -> "KRW".equals(ec.getCurrencyCode()))
                            .map(ExchangeCurrency::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal amountUS = goalCurrencies.stream()
                            .filter(ec -> "USD".equals(ec.getCurrencyCode()))
                            .map(ExchangeCurrency::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalAmountInKRW = amountKRW.add(amountUS.multiply(usdExchangeRate));

                    BigDecimal totalProfit = goal.getProfit().add(goal.getProfitUs().multiply(usdExchangeRate));

                    return new TripExchangeCurrencyDTO(
                            goal.getId(),
                            goal.getName(),
                            goal.getAccountId(),
                            goal.getAccountNumber(),
                            goal.getCountry(),
                            goal.getGoalAmount(),
                            goal.getEndDate(),
                            amountKRW,
                            amountUS,
                            totalAmountInKRW,
                            goal.getProfit(),
                            goal.getProfitUs(),
                            totalProfit
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
        BigDecimal evaluationAmount;  //평가금액
        BigDecimal totalAssets; //총 자산

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

        //평가금액 가져오기
        evaluationAmount = stockTradeService.calcAssessmentAmount(tripId, currencyCode);

        //총 자산 계산
        totalAssets = depositAmount.add(evaluationAmount);


        return new TripGoalDetailDTO(profit,evaluationAmount, purchaseAmount, totalAssets, depositAmount);

    }

    public void setInitAmount(String accountNumber) {
        Integer accountId = tripClient.getAccountByAccountNumber(accountNumber).getBody().getData().getAccountId();
        Optional<ExchangeCurrency> currency = exchangeCurrencyRepository.findByAccountIdAndCurrencyCode(accountId, "KRW");
        if (currency.isPresent()) {
            ExchangeCurrency curr = currency.get();
            curr.changeAmount(new BigDecimal(5000000).subtract(curr.getAmount()));
            exchangeCurrencyRepository.save(curr);
        } else {
            exchangeCurrencyRepository.save(ExchangeCurrency.builder()
                    .accountId(accountId)
                    .currencyCode("KRW")
                    .amount(new BigDecimal(5000000))
                    .build());
        }
    }
}
