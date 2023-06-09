package com.example.springbatch.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Sales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate orderDate;

    private int amount;
    private String days;

    public Sales(LocalDate orderDate, int amount, String days) {
        this.orderDate = orderDate;
        this.amount = amount;
        this.days = days;
    }
}
