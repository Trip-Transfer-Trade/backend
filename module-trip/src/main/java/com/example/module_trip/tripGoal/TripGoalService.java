package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import com.example.module_trip.account.AccountService;
import com.example.module_trip.account.AccountType;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.tree.TreePath;
import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class TripGoalService {

    private final TripGoalRepository tripGoalRepository;
    private final AccountService accountService;

    //    @Transactional
//    public void saveTripGoal(TripGoalRequestDTO tripGoalRequestDTO) {
//        Account account = accountRepository.findById(tripGoalRequestDTO.getAccountId())
//                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
//
//        TripGoal tripGoal = tripGoalRequestDTO.toEntity();
//        tripGoalRepository.save(tripGoal);
//    }

    @Transactional
    public void saveTripGoal(int userId, TripGoalRequestDTO dto) {
        boolean hasNormalAccount = accountService.findAccountByUserIdAndType(userId, AccountType.NORMAL).isPresent();
        if (!hasNormalAccount) {
            throw new IllegalStateException("여행 목표를 생성하려면 먼저 NORMAL 계좌를 만들어야 합니다.");
        }

        Account newTravelGoalAccount = accountService.createTravelGoalAccount(userId);

        TripGoal tripGoal = TripGoal.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .goalAmount(dto.getGoalAmount())
                .profit(BigDecimal.ZERO)
                .realisedProfit(BigDecimal.ZERO)
                .endDate(dto.getEndDate())
                .account(newTravelGoalAccount)
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
