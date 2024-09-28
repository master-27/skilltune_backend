package com.example.SkillTune_AI;

import com.example.SkillTune_AI.Repositories.SkillRepo;
import com.example.SkillTune_AI.Services.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoaderBean  implements CommandLineRunner {

    @Autowired
    private ImportDataService importDataService;
    @Autowired
    private SkillRepo skillRepo;
    @Override
    public void run(String... args) throws Exception {
        if(skillRepo.count()==0) {
            String csvFilePath = "src/main/resources/Skills.csv";
            importDataService.importSkills(csvFilePath);
        }
    }
}
