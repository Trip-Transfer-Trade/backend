package com.example.module_trip.tripGoal;

import com.example.module_trip.account.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    public TripGoalResponseDTO updateProfit(TripGoalProfitUpdateDTO tripGoalProfitUpdateDTO) {

        TripGoal tripGoal = tripGoalRepository.findById(tripGoalProfitUpdateDTO.getTripGoalId()).get();
        boolean before = tripGoal.isGoalReached(tripGoalProfitUpdateDTO.getRate());

        tripGoal.setProfit(tripGoalProfitUpdateDTO.getProfit());
        tripGoal.setProfitUs(tripGoalProfitUpdateDTO.getProfitUs());
        TripGoal updatedTripGoal = tripGoalRepository.save(tripGoal);
        log.info(updatedTripGoal.toString());

        String json = null;
        try {
            json = objectMapper.writeValueAsString(TripGoalAlarmDTO.toDTO(updatedTripGoal.getName(), updatedTripGoal.getAccount().getUserId()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        boolean after = updatedTripGoal.isGoalReached(tripGoalProfitUpdateDTO.getRate());
        if (!before && after){
            rabbitTemplate.convertAndSend("exchange.goal","goal.alert", json);
            System.out.println("알림 전송 ");
        }
        return TripGoalResponseDTO.toDTO(updatedTripGoal);
    }

}
