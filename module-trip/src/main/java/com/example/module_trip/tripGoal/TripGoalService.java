package com.example.module_trip.tripGoal;

import com.example.module_trip.account.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

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
        AccountResponseDTO accountDTO = accountService.saveAccount(AccountCreateRequestDTO.builder()
                .userId(userId)
                .accountType(AccountType.TRAVEL_GOAL).build()); //userId, type
        Account account = accountRepository.findById(accountDTO.getAccountId())
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
}
