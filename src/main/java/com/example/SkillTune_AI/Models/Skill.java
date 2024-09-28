package com.example.SkillTune_AI.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@Entity
@AllArgsConstructor
@Getter
@Setter
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String skillName;



    @OneToMany(mappedBy = "skill")
    private Set<UserSkill> userSkills;

    public Skill(String name){
        skillName = name;
    }
}
