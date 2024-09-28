package com.example.SkillTune_AI.Models;


import com.example.SkillTune_AI.Models.Skill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@ToString
@Getter
@Setter
public class Question {


    private Long questionId;


    private String skill;

    private String questionText;

    private String explanation;

    private String options;  // Use String to store JSON array

    private String correctAnswer;



    private LocalDateTime createdAt = LocalDateTime.now();

    public Question(Long id, String skill, String question, String options, String correctAnswer, String explanation) {
        this.questionId = id;
        this.skill = skill;
        this.questionText = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }


    // Getters and Setters
}
