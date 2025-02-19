package com.example.module_trip.postSellProfit;

import com.example.module_trip.tripGoal.TripGoal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSellProfit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private BigDecimal profit;

    @OneToOne
    @JoinColumn(name="trip_id",referencedColumnName = "id", nullable = false)
    private TripGoal trip;
}
