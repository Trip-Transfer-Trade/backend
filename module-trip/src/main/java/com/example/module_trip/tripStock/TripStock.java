package com.example.module_trip.tripStock;

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
public class TripStock  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int stockCode;
    private String name;
    private int quantity;
    private BigDecimal blendedPrice;

    @ManyToOne
    @JoinColumn(name="trip_id", referencedColumnName = "id", nullable = false)
    private TripGoal tripGoal;

}
