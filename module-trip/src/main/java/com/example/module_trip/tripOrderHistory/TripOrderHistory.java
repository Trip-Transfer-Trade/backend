package com.example.module_trip.tripOrderHistory;

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
public class TripOrderHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String type;
    private int quantity;
    private BigDecimal price;
    private String stockCode;
    private BigDecimal profit;

    @ManyToOne
    @JoinColumn(name="trip_id", referencedColumnName = "id", nullable = false)
    private TripGoal tripGoal;
}
