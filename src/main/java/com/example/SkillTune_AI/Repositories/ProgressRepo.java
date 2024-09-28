package com.example.SkillTune_AI.Repositories;

import com.example.SkillTune_AI.Models.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressRepo extends JpaRepository<Progress,Integer> {
    Progress findByEmail(String email);
}
