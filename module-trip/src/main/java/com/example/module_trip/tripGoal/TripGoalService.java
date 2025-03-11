package com.example.module_trip.tripGoal;

import com.example.module_trip.account.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class TripGoalService {

    private final TripGoalRepository tripGoalRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveTripGoal(int userId, TripGoalRequestDTO dto) {
        boolean hasNormalAccount = accountService.findAccountByUserIdAndType(userId, AccountType.NORMAL).isPresent();
        if (!hasNormalAccount) {
            throw new IllegalStateException("여행 목표를 생성하려면 먼저 NORMAL 계좌를 만들어야 합니다.");
        }
        //TRAVEL_GOAL 계좌 개설
        String accountNumber = accountService.saveAccount(userId, AccountCreateRequestDTO.builder()
                .accountType(AccountType.TRAVEL_GOAL).build());

        // 계좌 번호를 기반으로 계좌 정보 조회
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("계좌를 찾을 수 없습니다."));

        TripGoal tripGoal = TripGoal.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .goalAmount(dto.getGoalAmount())
                .profit(BigDecimal.ZERO)
                .realisedProfit(BigDecimal.ZERO)
                .endDate(dto.getEndDate())
                .account(account)
                .build();

        tripGoalRepository.save(tripGoal);
    }

    public void updateTripGoal(Integer tripId, TripGoalEditDTO dto){
        TripGoal tripGoal = tripGoalRepository.findById(tripId)
                .orElseThrow(()-> new IllegalStateException("해당 여행 목표를 찾을 수 없습니다."));
        tripGoal.updateFromDTO(dto);
        tripGoalRepository.save(tripGoal);
    }

    public TripGoalResponseDTO findTripGoalById(Integer tripId) {
        TripGoal tripGoal =tripGoalRepository.findById(tripId).get();
        TripGoalResponseDTO tripGoalResponseDTO = TripGoalResponseDTO.toDTO(tripGoal);

        return tripGoalResponseDTO;
    }

    public TripGoalResponseDTO findTripGoalByAccountId(Integer accountId) {
        TripGoal tripGoal = tripGoalRepository.findByAccount_Id(accountId);
        return TripGoalResponseDTO.toDTO(tripGoal);
    }

    public List<TripGoalResponseDTO> findTripGoalsByAccountId(List <Integer> accountId) {
        List<TripGoal> tripGoals = tripGoalRepository.findAllByAccountIdIn(accountId);
        return tripGoals.stream()
                .map(TripGoalResponseDTO::toDTO).collect(Collectors.toList());
    }

    public List<TripGoalResponseDTO> findAllTripGoal() {
        List<TripGoal> tripGoals = tripGoalRepository.findAll();

        return tripGoals.stream()
                .map(TripGoalResponseDTO::toDTO)
                .toList();
    }

    public TripGoalResponseDTO updateRealisedProfit(TripGoalUpdateDTO tripGoalUpdateDTO) {
        TripGoal tripGoal = tripGoalRepository.findById(tripGoalUpdateDTO.getTripGoalId()).get();
        tripGoal.setRealisedProfit(tripGoalUpdateDTO.getRealisedProfit());
        TripGoal updatedTripGoal = tripGoalRepository.save(tripGoal);
        return TripGoalResponseDTO.toDTO(updatedTripGoal);
    }

    public List<TripGoalListResponseDTO> findTripGoalListByUserId(Integer userId) {
        //userId를 기반으로 TRAVEL_GOAL 계좌 조회
        List<Account> travelGoalAccounts = accountRepository.findAllByUserIdAndAccountType(userId, AccountType.TRAVEL_GOAL);

        if (travelGoalAccounts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No TRAVEL_GOAL accounts found for user ID: " + userId);
        }
        // TRAVEL_GOAL 계좌 ID 리스트 추출
        List<Integer> accountIds = travelGoalAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        // 계좌 ID에 해당하는 TripGoal 리스트 조회
        List<TripGoal> tripGoals = tripGoalRepository.findAllByAccountIdIn(accountIds);

        return tripGoals.stream()
                .map(TripGoalListResponseDTO::toDTO)
                .collect(Collectors.toList());
    }

    public TripGoalResponseDTO updateProfit(TripGoalProfitUpdateDTO tripGoalProfitUpdateDTO) {

        TripGoal tripGoal = tripGoalRepository.findById(tripGoalProfitUpdateDTO.getTripGoalId()).get();
        boolean before = tripGoal.isGoalReached(tripGoalProfitUpdateDTO.getRate());

        tripGoal.setProfit(tripGoalProfitUpdateDTO.getProfit());
        tripGoal.setProfitUs(tripGoalProfitUpdateDTO.getProfitUs());
        TripGoal updatedTripGoal = tripGoalRepository.save(tripGoal);
        log.info(updatedTripGoal.toString());

        String json = createAlarmJson(updatedTripGoal);

        boolean after = updatedTripGoal.isGoalReached(tripGoalProfitUpdateDTO.getRate());
        if (!before && after) {
            sendGoalSuccessAlert(json);
        }

        checkGoalHalf(updatedTripGoal,tripGoalProfitUpdateDTO.getRate(),json);
        return TripGoalResponseDTO.toDTO(updatedTripGoal);

    }

    private String createAlarmJson(TripGoal tripGoal) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(
                    TripGoalAlarmDTO.toDTO(tripGoal.getName(), tripGoal.getAccount().getUserId())
            );
            log.info("json :" +json);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패", e);
        }
        return json;
    }

    private void sendGoalSuccessAlert(String json){
        rabbitTemplate.convertAndSend("exchange.goal", "goal.alert", json);
        log.info("목표 도달 알림 전송");
    }

    private void sendHalfAlert(String json) {
        rabbitTemplate.convertAndSend("exchange.halfGoal", "half.alert", json);
        log.info("목표 절반 미달 알림 전송");
    }

    private boolean hasReachedHalfPeriod(TripGoal tripGoal) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = tripGoal.getCreatedDate();
        LocalDateTime endDate = tripGoal.getEndDate().atStartOfDay();
        Duration totalDuration = Duration.between(startDate, endDate);
        LocalDateTime halfTime = startDate.plusSeconds(totalDuration.getSeconds() / 2);
        log.info("totalDuration "+totalDuration + "halfTime " +halfTime + "now "+now);
        return now.isAfter(halfTime);
    }

    private void checkGoalHalf(TripGoal tripGoal, String rateStr,String json) {
        if(hasReachedHalfPeriod(tripGoal)){
            BigDecimal goalAmount = tripGoal.getGoalAmount();
            BigDecimal halfGoalAmount = goalAmount.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);

            BigDecimal profit = tripGoal.getProfit();
            BigDecimal profitUs = tripGoal.getProfitUs();

            BigDecimal rate = new BigDecimal(rateStr.replace(",", ""));

            BigDecimal convertedProfitUs = profitUs.multiply(rate);
            BigDecimal nowProfit = profit.add(convertedProfitUs);
            log.info("nowProfit" +nowProfit + "halfgoal" +halfGoalAmount);
            if (nowProfit.compareTo(halfGoalAmount) < 0){
                sendHalfAlert(json);
            };
        }
    }
}
