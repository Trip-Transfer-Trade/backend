package com.example.module_trip.tripGoal;

import com.example.module_trip.account.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripGoalService {

    private final TripGoalRepository tripGoalRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

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
}
