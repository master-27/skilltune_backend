package com.example.SkillTune_AI.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Table(name = "user_skills")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userSkillId;

    @JsonIgnoreProperties("userSkills")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @JsonIgnoreProperties("userSkills")
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;


    @Override
    public String toString() {
        return "UserSkill{" +
                "id=" + this.userSkillId +
                ", skill=" + skill.getSkillName() +  // Just print the skill name
                '}';
    }

    // Getters and Setters
}

