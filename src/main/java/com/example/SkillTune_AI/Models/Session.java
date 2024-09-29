package com.example.SkillTune_AI.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;


    private String skill;


    private int score;


    private double percentage;


    private LocalDate sessionDate;

    private  int noOfQuestions;

    public Session(String email, String skill,int score,double percentage,LocalDate sessionDate,int noOfQuestions) {
        this.email = email;
        this.skill = skill;
        this.percentage = percentage;
        this.sessionDate = sessionDate;
        this.noOfQuestions =  noOfQuestions;
    }

}
