package com.example.SkillTune_AI.Repositories;

import com.example.SkillTune_AI.Models.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SkillRepo extends JpaRepository<Skill,Long> {

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Skill s WHERE s.skillName = :skillName")
    boolean existsBySkillName(@Param("skillName")String skillName);

    @Query("SELECT s FROM Skill s where s.skillName in :skillSet")
    List<Skill> getSkillsByName(@Param("skillSet")List<String> skillSet);

    @Query("select s from Skill s where s.skillName = :skillName")
    Skill findBySkillName(@Param("skillName") String skillName);




}
