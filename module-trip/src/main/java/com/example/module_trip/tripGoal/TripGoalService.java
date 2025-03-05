package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import com.example.module_trip.account.AccountRepository;
import com.example.module_trip.account.AccountService;
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
    private final AccountRepository accountRepository;
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
        int accountId = accountService.findAccountIdByUserId(userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found for id: " + accountId));

        TripGoal tripGoal = TripGoal.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .goalAmount(dto.getGoalAmount())
                .profit(BigDecimal.ZERO)
                .realisedProfit(BigDecimal.ZERO)
                .endDate(dto.getEndDate())
                .account(account) // ✅ Account 객체 설정
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
}
