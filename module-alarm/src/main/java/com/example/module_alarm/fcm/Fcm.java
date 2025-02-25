package com.example.module_alarm.fcm;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Fcm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    @Column(nullable = false,unique = true)
    private String token;

    @Builder
    public Fcm(Integer userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}
