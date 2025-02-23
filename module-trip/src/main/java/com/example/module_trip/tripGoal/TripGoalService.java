package com.example.module_trip.tripGoal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripGoalService {

    private final TripGoalRepository tripGoalRepository;

    public TripGoalService(TripGoalRepository tripGoalRepository) {
        this.tripGoalRepository = tripGoalRepository;
    }

    @Transactional
    public void saveTripGoal(TripGoalRequestDTO tripGoalRequestDTO) {
        TripGoal tripGoal = tripGoalRequestDTO.toEntity();
        tripGoalRepository.save(tripGoal);
    }

    public TripGoalResponseDTO findTripGoalById(Integer userId) {
        TripGoal tripGoal =tripGoalRepository.findById(userId).get();
        TripGoalResponseDTO tripGoalResponseDTO = TripGoalResponseDTO.toDTO(tripGoal);

        return tripGoalResponseDTO;
    }

}
