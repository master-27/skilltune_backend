package com.example.SkillTune_AI.Repositories;

import com.example.SkillTune_AI.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo  extends JpaRepository<User,Long> {



    @Query("SELECT u FROM User u WHERE u.email = :email")
    public User findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u)>0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    public boolean existByEmail(@Param("email")String email);


}
