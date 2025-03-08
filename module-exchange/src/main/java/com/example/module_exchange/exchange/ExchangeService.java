package com.example.module_exchange.exchange;

import com.example.module_exchange.clients.AccountClient;
import com.example.module_exchange.clients.MemberClient;
import com.example.module_exchange.clients.TripClient;
import com.example.module_exchange.exchange.exchangeCurrency.*;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeHistory;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeHistoryRepository;
import com.example.module_exchange.exchange.exchangeHistory.ExchangeType;
import com.example.module_exchange.exchange.transactionHistory.*;
import com.example.module_trip.account.AccountResponseDTO;
import com.example.module_trip.account.AccountType;
import com.example.module_trip.tripGoal.TripGoalResponseDTO;
import com.example.module_utility.response.Response;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ExchangeService {

    private final AccountClient accountClient;
    private final TripClient tripClient;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ExchangeCurrencyRepository exchangeCurrencyRepository;
    private final MemberClient memberClient;

    public ExchangeService(AccountClient accountClient, TripClient tripClient, MemberClient memberClient, ExchangeHistoryRepository exchangeHistoryRepository, TransactionHistoryRepository transactionHistoryRepository, ExchangeCurrencyRepository exchangeCurrencyRepository) {
        this.accountClient = accountClient;
        this.tripClient = tripClient;
        this.memberClient = memberClient;
        this.exchangeHistoryRepository = exchangeHistoryRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.exchangeCurrencyRepository = exchangeCurrencyRepository;
    }

    public void executeExchangeProcess(ExchangeDTO exchangeDTO) {
        Integer accountId = exchangeDTO.getAccountId();

        String fromCurrencyCode = exchangeDTO.getFromCurrency();
        String toCurrencyCode = exchangeDTO.getToCurrency();

        BigDecimal fromAmount = exchangeDTO.getFromAmount();
        BigDecimal toAmount = exchangeDTO.getToAmount();

        ExchangeCurrency fromExchangeCurrency = getOrCreateExchangeCurrency(accountId, fromCurrencyCode);
        ExchangeCurrency toExchangeCurrency = getOrCreateExchangeCurrency(accountId, toCurrencyCode);
        validateSufficientBalance(fromExchangeCurrency,fromAmount);

        ExchangeHistory fromExchangeHistory = exchangeDTO.toExchangeHistory(fromExchangeCurrency, ExchangeType.WITHDRAWAL, fromAmount);
        ExchangeHistory toExchangeHistory = exchangeDTO.toExchangeHistory(toExchangeCurrency,ExchangeType.DEPOSIT,toAmount);

        TransactionHistory fromTransactionHistory = exchangeDTO.toTransactionHistory(fromExchangeCurrency, TransactionType.WITHDRAWAL, fromAmount);
        TransactionHistory toTransactionHistory = exchangeDTO.toTransactionHistory(toExchangeCurrency, TransactionType.DEPOSIT, toAmount);

        executeExchangeOperations(fromExchangeHistory, toExchangeHistory, fromTransactionHistory, toTransactionHistory, fromExchangeCurrency, toExchangeCurrency, fromAmount, toAmount);
    }

    @Transactional
    public void executeExchangeOperations(ExchangeHistory fromExchangeHistory, ExchangeHistory toExchangeHistory, TransactionHistory fromTransactionHistory, TransactionHistory toTransactionHistory,
                                          ExchangeCurrency fromExchangeCurrency, ExchangeCurrency toExchangeCurrency, BigDecimal fromAmount, BigDecimal toAmount) {
        exchangeHistoryRepository.save(fromExchangeHistory);
        exchangeHistoryRepository.save(toExchangeHistory);

        transactionHistoryRepository.save(fromTransactionHistory);
        transactionHistoryRepository.save(toTransactionHistory);

        fromExchangeCurrency.changeAmount(fromAmount.negate());
        toExchangeCurrency.changeAmount(toAmount);

        exchangeCurrencyRepository.save(fromExchangeCurrency);
        exchangeCurrencyRepository.save(toExchangeCurrency);
        // exchangeCurrency 가져올 때 auto commit -> save 해주어야됨
    }


    public void executeExchangeBatchProcess(ExchangeBatchDTO exchangeBatchDTO) {
        BigDecimal exchangeRate = exchangeBatchDTO.getExchangeRate();

        String fromCurrency = exchangeBatchDTO.getFromCurrency();
        String toCurrency = exchangeBatchDTO.getToCurrency();

        ExchangeCurrency toExchangeCurrency = getOrCreateExchangeCurrency(exchangeBatchDTO.getAccountId(),toCurrency);
        BigDecimal toAmount = BigDecimal.ZERO;
        List<ExchangeHistory> exchangeHistories = new ArrayList<>();
        List<TransactionHistory> transactionHistories = new ArrayList<>();
        List<ExchangeCurrency> exchangeCurrencies = new ArrayList<>();

        for (BatchDTO exchange : exchangeBatchDTO.getBatchDTOList()){
            ExchangeCurrency fromExchangeCurrency = getOrCreateExchangeCurrency(exchange.getAccountId(),fromCurrency);
            System.out.println(toExchangeCurrency.getAccountId());

            BigDecimal fromAmount = exchange.getAmount();
            validateSufficientBalance(fromExchangeCurrency,fromAmount);

            toAmount.add(fromAmount);

            exchangeHistories.add(ExchangeHistory.builder()
                    .exchangeType(ExchangeType.WITHDRAWAL)
                    .amount(fromAmount)
                    .exchangeRate(exchangeRate)
                    .exchangeCurrency(fromExchangeCurrency)
                    .build());

            transactionHistories.add(TransactionHistory.builder()
                    .transactionType(TransactionType.WITHDRAWAL)
                    .transactionCategory(TransactionCategory.EXCHANGE)
                    .transactionAmount(fromAmount)
                    .description("환전 출금")
                    .exchangeCurrency(fromExchangeCurrency)
                    .build());

            fromExchangeCurrency.changeAmount(fromAmount.negate());
            exchangeCurrencies.add(fromExchangeCurrency);
        }

        ExchangeHistory toExchangeHistory = ExchangeHistory.builder()
                        .exchangeType(ExchangeType.DEPOSIT).exchangeRate(exchangeRate)
                        .amount(toAmount).exchangeCurrency(toExchangeCurrency).build();

        TransactionHistory toTransactionHistory = TransactionHistory.builder()
                        .transactionType(TransactionType.DEPOSIT).transactionCategory(TransactionCategory.EXCHANGE)
                        .transactionAmount(toAmount).exchangeCurrency(toExchangeCurrency).description("환전 입금").build();

        BigDecimal totalAmount =toAmount.multiply(exchangeRate);

        executeExchangeBatchOperations(exchangeHistories,transactionHistories,exchangeCurrencies,
                toExchangeCurrency, totalAmount,toExchangeHistory, toTransactionHistory );
    }

    @Transactional
    public void executeExchangeBatchOperations(List<ExchangeHistory> exchangeHistories, List<TransactionHistory> transactionHistories, List<ExchangeCurrency> exchangeCurrencies,
                                               ExchangeCurrency toExchangeCurrency, BigDecimal toAmount, ExchangeHistory toExchangeHistory, TransactionHistory toTransactionHistory){
        exchangeHistoryRepository.saveAll(exchangeHistories);
        exchangeHistoryRepository.save(toExchangeHistory);

        transactionHistoryRepository.saveAll(transactionHistories);
        transactionHistoryRepository.save(toTransactionHistory);

        exchangeCurrencyRepository.saveAll(exchangeCurrencies);
        toExchangeCurrency.changeAmount(toAmount);
        exchangeCurrencyRepository.save(toExchangeCurrency);
    }

    public void executeTransactionProcess(TransactionDTO transactionDTO, String username) {
        Integer accountId = transactionDTO.getAccountId();
        ResponseEntity<Response<AccountResponseDTO>> accountResponse = accountClient.getAccountByAccountNumber(transactionDTO.getTargetAccountNumber());
        Integer targetAccountId = accountResponse.getBody().getData().getAccountId();
        if (accountId.equals(targetAccountId)) {
            throw new RuntimeException("자기 자신에게 송금할 수 없습니다.");
        }
        AccountType targetAccountType = accountResponse.getBody().getData().getAccountType();
        AccountType accountType = accountClient.getAccountById(accountId).getBody().getData().getAccountType();
        String toDescription;
        String fromDescription;
        if (transactionDTO.getDescription()=="") {
            toDescription = setDescriptionFromAccount(targetAccountType, targetAccountId, username);
            fromDescription = setDescriptionFromAccount(accountType, accountId, username);
        } else{
            toDescription = transactionDTO.getDescription();
            fromDescription = transactionDTO.getDescription();
        }

        BigDecimal amount = transactionDTO.getAmount();
        ExchangeCurrency fromTransactionCurrency = getOrCreateExchangeCurrency(accountId, transactionDTO.getCurrencyCode());
        ExchangeCurrency toTransactionCurrency = getOrCreateExchangeCurrency(targetAccountId, transactionDTO.getCurrencyCode());
        validateSufficientBalance(fromTransactionCurrency,amount);

        TransactionHistory fromTransactionHistory = transactionDTO.toTransactionHistory(fromTransactionCurrency, TransactionType.WITHDRAWAL, toDescription);
        TransactionHistory toTransactionHistory = transactionDTO.toTransactionHistory(toTransactionCurrency, TransactionType.DEPOSIT, fromDescription);

        executeTransactionalOperations(amount, fromTransactionCurrency, toTransactionCurrency, fromTransactionHistory, toTransactionHistory);

    }

    @Transactional
    public void executeTransactionalOperations(BigDecimal amount, ExchangeCurrency fromTransactionCurrency, ExchangeCurrency toTransactionCurrency,
                                                                   TransactionHistory fromTransactionHistory,TransactionHistory toTransactionHistory) {
        transactionHistoryRepository.save(fromTransactionHistory);
        transactionHistoryRepository.save(toTransactionHistory);

        fromTransactionCurrency.changeAmount(amount.negate());
        exchangeCurrencyRepository.save(fromTransactionCurrency);

        toTransactionCurrency.changeAmount(amount);
        exchangeCurrencyRepository.save(toTransactionCurrency);

    }

    private String setDescriptionFromAccount(AccountType accountType, Integer accountId, String username) {
        if(accountType==AccountType.TRAVEL_GOAL){
            return getTripNameFromAccountId(accountId);
        }
        return memberClient.findUserByUsername(username).getBody().getData().getName();
    }

    private String getTripNameFromAccountId(Integer accountId) {
        ResponseEntity<Response<TripGoalResponseDTO>> tripResponse = tripClient.getTripGoalByAccountId(accountId);
        return tripResponse.getBody().getData().getName();
    }

    private Integer getAccountIdFromTripId(int tripId) {
        ResponseEntity<Response<TripGoalResponseDTO>> responseEntity = tripClient.getTripGoal(tripId);
        TripGoalResponseDTO tripGoalResponseDTO = responseEntity.getBody().getData();
        return tripGoalResponseDTO.getAccountId();
    }

    private Integer getAccountIdFromAccountNumber(String accountNumber) {
        ResponseEntity<Response<AccountResponseDTO>> accountResponse = accountClient.getAccountByAccountNumber(accountNumber);
        return accountResponse.getBody().getData().getAccountId();
    }

    private void validateSufficientBalance(ExchangeCurrency fromCurrency, BigDecimal fromAmount) {
        if (fromCurrency.getAmount().compareTo(fromAmount) < 0) {
            throw new RuntimeException("잔액 부족: 출금 금액이 계좌 잔액보다 큽니다.");
        }
    }

    private ExchangeCurrency getOrCreateExchangeCurrency(Integer accountId, String currencyCode) {
        return exchangeCurrencyRepository
                .findByAccountIdAndCurrencyCode(accountId, currencyCode)
                .orElseGet(() -> {
                    ExchangeCurrency newCurrency = ExchangeCurrency.builder()
                            .accountId(accountId)
                            .currencyCode(currencyCode)
                            .amount(BigDecimal.ZERO)
                            .build();
                    return exchangeCurrencyRepository.save(newCurrency);
                });
    }

    public List<TransactionHistoryResponseDTO> getTransactionHistory(Integer accountId) {
        return transactionHistoryRepository.findByExchangeCurrency_AccountIdAndTransactionCategoryOrderByCreatedDateDesc(accountId,TransactionCategory.BASIC)
                .stream()
                .map(TransactionHistoryResponseDTO::toDTO)
                .collect(Collectors.toList());
    }

    // 전체 계좌 조회 - 국내/미국 구분, basic 계좌
    public List<AccountListDTO> getAccountList(@RequestHeader(value = "X-Authenticated-User", required = false) int userid, String currencyCode) {
        // user id로 account
        System.out.println("userid: " + userid);

        ResponseEntity<Response<List<AccountResponseDTO>>> response = accountClient.getAllAccount(userid);
        List<AccountResponseDTO> accounts = response.getBody().getData();

        System.out.println("Response Body: " + response.getBody());
        System.out.println("Accounts: " + accounts);

        Map<Integer, AccountType> accountMap = accounts.stream()
                .collect(Collectors.toMap(AccountResponseDTO::getAccountId, AccountResponseDTO::getAccountType));

        List<Integer> accountIds = new ArrayList<>(accountMap.keySet());
        ResponseEntity<Response<List<TripGoalResponseDTO>>> responseTrip = tripClient.getAllTripsByAccountIdIn(accountIds);
        List<TripGoalResponseDTO> tripGoals = responseTrip.getBody().getData();

        System.out.println("Account IDs: " + accountIds);

        // account id로 trip name
        List<TripGoalDTO> trips = tripGoals.stream()
                .map(tripGoal -> TripGoalDTO.builder()
                        .name(tripGoal.getName())
                        .build())
                .collect(Collectors.toList());

        System.out.println("Trip Name: " + trips);
        Map<Integer, String> tripGoalMap = tripGoals.stream()
                .collect(Collectors.toMap(TripGoalResponseDTO::getAccountId, TripGoalResponseDTO::getName));

        // account id로 exchange_currency
        List<ExchangeCurrency> exchangeCurrency = exchangeCurrencyRepository.findByAccountIdIn(accountIds);

        for (ExchangeCurrency ec : exchangeCurrency) {
            System.out.println("Account ID: " + ec.getAccountId() + ", Currency Code: " + ec.getCurrencyCode() + ", Amount: " + ec.getAmount());
        }
        System.out.println("Expected Currency Code: " + currencyCode);

        System.out.println("Exchange Currency List: " + exchangeCurrency);

        // currencyCode 필터링
        List<ExchangeCurrency> filteredExchangeCurrency = exchangeCurrencyRepository.findByAccountIdInAndCurrencyCode(accountIds, currencyCode);

        System.out.println("Filtered Exchange Currency List: " + filteredExchangeCurrency);

        return filteredExchangeCurrency.stream()
                .map(ec -> new AccountListDTO(
                        accountMap.getOrDefault(ec.getAccountId(), null),
                        ec.getAmount(),
                        tripGoalMap != null ? tripGoalMap.getOrDefault(ec.getAccountId(), "Unknown") : "Unknown"
                ))
                .collect(Collectors.toList());
    }


    public List<WalletResponseDTO> findExchangeCurrecyByUsernameAndCurrencyCode(String username, String currencyCode) {
        Integer userId = memberClient.findUserByUsername(username).getBody().getData().getUserId();

        List<Integer> accountIds = accountClient.getAccountByUserId(userId).getBody().getData()
                .stream().map(AccountResponseDTO::getAccountId).collect(Collectors.toList());

        return exchangeCurrencyRepository.findByCurrencyCodeAndAccountIdIn(currencyCode, accountIds)
                .stream()
                .map(WalletResponseDTO::toDto)
                .collect(Collectors.toList());
    }

//    public List<WalletResponseDTO> getWalletBalance(Integer accountId) {
//        List<ExchangeCurrency> currencies = exchangeCurrencyRepository.findByAccountId(accountId);
//
//        return currencies.stream()
//                .map(WalletResponseDTO::toDto)
//                .collect(Collectors.toList());
//    }

    public List<WalletSummaryResponseDTO> getUserWalletSummary(String username) {

        Integer userId = memberClient.findUserByUsername(username).getBody().getData().getUserId();

        List<Integer> accountIds = accountClient.getAccountByUserId(userId).getBody().getData()
                .stream().map(AccountResponseDTO::getAccountId).collect(Collectors.toList());

        Map<String, BigDecimal> currencyBalances = new HashMap<>();

        for (Integer accountId : accountIds) {
            List<ExchangeCurrency> currencies = exchangeCurrencyRepository.findByAccountId(accountId);

            for (ExchangeCurrency currency : currencies) {
                currencyBalances.merge(
                        currency.getCurrencyCode(),
                        currency.getAmount(),
                        BigDecimal::add
                );
            }
        }

        return currencyBalances.entrySet().stream()
                .map(entry -> new WalletSummaryResponseDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<MyWalletDTO> findExchangeCurrencyByUserId(int userId) {
        List<Integer> accountIds = accountClient.getAccountByUserId(userId).getBody().getData()
                .stream().map(AccountResponseDTO::getAccountId).collect(Collectors.toList());

        List<Object[]> amounts = exchangeCurrencyRepository.findTotalAmountByCurrencyCode(accountIds);

        return amounts.stream()
                .map(row -> MyWalletDTO.builder()
                        .currencyCode((String) row[0])
                        .totalAmount((BigDecimal) row[1])
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<WalletResponseDTO> findExchangeCurrencyByUserIdAndCurrencyCode(int userId, String currencyCode) {
        List<Integer> accountIds = accountClient.getAccountByUserId(userId).getBody().getData()
                .stream().map(AccountResponseDTO::getAccountId).collect(Collectors.toList());

        return exchangeCurrencyRepository.findByCurrencyCodeAndAccountIdIn(currencyCode, accountIds)
                .stream()
                .map(WalletResponseDTO::toDto)
                .collect(Collectors.toList());
    }

    public List<AvailableDTO> findExchangeCurrencyTripByUserId(int userId) {
        final BigDecimal USD_TO_KRW = BigDecimal.valueOf(1400);
        List<Integer> accountIds = accountClient.getAccountByUserId(userId).getBody().getData()
                .stream().map(AccountResponseDTO::getAccountId).collect(Collectors.toList());

        List<TripGoalResponseDTO> tripGoals = tripClient.getAllTripsByAccountIdIn(accountIds).getBody().getData();
        List<ExchangeCurrency> exchangeCurrency = exchangeCurrencyRepository.findByAccountIdIn(accountIds);

        Map<Pair<Integer, String>, BigDecimal> currencyAmountMap = exchangeCurrency.stream()
                .filter(ec -> "USD".equals(ec.getCurrencyCode()) || "KRW".equals(ec.getCurrencyCode()))
                .collect(Collectors.groupingBy(
                        ec -> Pair.of(ec.getAccountId(), ec.getCurrencyCode()),
                        Collectors.reducing(BigDecimal.ZERO, ec -> {
                            if("USD".equals(ec.getCurrencyCode())){
                                return ec.getAvailableAmount().multiply(USD_TO_KRW);
                            } else {
                                return ec.getAvailableAmount();
                            }
                        }, BigDecimal::add)
                ));

        return tripGoals.stream()
                .map(trip -> {
                    BigDecimal totalAmount = currencyAmountMap.entrySet().stream()
                            .filter(entry -> entry.getKey().getLeft().equals(trip.getAccountId()))
                            .map(Map.Entry::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return AvailableDTO.builder()
                            .tripId(trip.getId())
                            .tripName(trip.getName())
                            .availableAmount(totalAmount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<AvailableAllDTO> findAllExchangeCurrencyTripByUserId(int accountId) {
        List<ExchangeCurrency> exchangeCurrency = exchangeCurrencyRepository.findByAccountId(accountId);

        return exchangeCurrency.stream()
                .map(ec -> AvailableAllDTO.builder()
                        .currencyCode(ec.getCurrencyCode())
                        .availableAmount(ec.getAvailableAmount())
                        .build()
                )
                .collect(Collectors.toList());
    }

}
