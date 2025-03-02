package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import com.example.module_trip.account.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripGoalService {

    private final TripGoalRepository tripGoalRepository;
    private final AccountRepository accountRepository;
    public TripGoalService(TripGoalRepository tripGoalRepository, AccountRepository accountRepository) {
        this.tripGoalRepository = tripGoalRepository;
        this.accountRepository = accountRepository;
    }
    @Transactional
    public void saveTripGoal(Integer userId, TripGoalRequestDTO tripGoalRequestDTO) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found for userId: " + userId));

        TripGoal tripGoal = tripGoalRequestDTO.toEntity(account);
        tripGoalRepository.save(tripGoal);
    }

    public TripGoalResponseDTO findTripGoalById(Integer userId) {
        TripGoal tripGoal = tripGoalRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("TripGoal을 찾을 수 없습니다."));
        return TripGoalResponseDTO.toDTO(tripGoal);
    }
//    @Transactional
//    public void saveTripGoal(TripGoalRequestDTO tripGoalRequestDTO) {
//        TripGoal tripGoal = tripGoalRequestDTO.toEntity();
//        tripGoalRepository.save(tripGoal);
//    }
//
//    public TripGoalResponseDTO findTripGoalById(Integer userId) {
//        TripGoal tripGoal =tripGoalRepository.findById(userId).get();
//        TripGoalResponseDTO tripGoalResponseDTO = TripGoalResponseDTO.toDTO(tripGoal);
//
//        return tripGoalResponseDTO;
//    }

}
