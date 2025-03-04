package com.example.module_trip.tripGoal;

import com.example.module_trip.account.Account;
import com.example.module_trip.account.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
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
    public void saveTripGoal(TripGoalRequestDTO tripGoalRequestDTO) {
        Account account = accountRepository.findById(tripGoalRequestDTO.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        TripGoal tripGoal = tripGoalRequestDTO.toEntity();
        tripGoalRepository.save(tripGoal);
    }

    public TripGoalResponseDTO findTripGoalById(Integer tripId) {
        TripGoal tripGoal =tripGoalRepository.findById(tripId).get();
        TripGoalResponseDTO tripGoalResponseDTO = TripGoalResponseDTO.toDTO(tripGoal);

        return tripGoalResponseDTO;
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
