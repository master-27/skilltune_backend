package com.example.SkillTune_AI.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;


import javax.lang.model.element.NestingKind;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Table(name = "users")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;

    private String email;

    private  String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnoreProperties("user")
    @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserSkill> userSkills;

    @Column(columnDefinition = "TEXT")
    private String projects  = null;
    @Column(columnDefinition = "TEXT")
    private  String experience = null;

    public  User(String username , String email , String password){
        this.username = username;
        this.email = email;
         this.password = password;

    }
    @Override
    public String toString() {
        String skillNames = userSkills.stream()
                .map(userSkill -> userSkill.getSkill().getSkillName())
                .reduce((skill1, skill2) -> skill1 + ", " + skill2)
                .orElse("No skills");

        return "User{" +
                "id=" + this.userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ",skills="+ skillNames+
                '}';
    }



    // Getters and Setters
}
