package com.example.SkillTune_AI.Repositories;

import com.example.SkillTune_AI.Models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepo extends JpaRepository<Session,Long> {
    @Query("SELECT s FROM Session s WHERE s.email = :email ORDER BY s.sessionDate DESC")
    List<Session> findAllSessionByEmail(@Param("email") String email);


    @Query("SELECT s FROM Session s WHERE s.skill = :skill ORDER BY s.sessionDate DESC")
    List<Session> findBySkillOrderBySessionDateDesc(@Param("skill") String skill);
}



