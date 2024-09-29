package com.example.SkillTune_AI.Services;

import com.example.SkillTune_AI.Models.Skill;
import com.example.SkillTune_AI.Repositories.SkillRepo;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Service
public class ImportDataService {
    @Autowired
    private SkillRepo skillRepo;

    @Transactional
    public void importSkills(String csvFilePath){

        try(CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] line;
            while((line = reader.readNext())!= null){
                String skillName = line[0].trim();
                if(!skillRepo.existsBySkillName(skillName)){
                    Skill skill = new Skill(skillName);
                    skillRepo.save(skill);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
