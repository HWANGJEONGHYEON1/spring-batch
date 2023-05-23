package com.example.springbatch.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Teacher {

    @Id
    private Long id;

    private String name;
}
