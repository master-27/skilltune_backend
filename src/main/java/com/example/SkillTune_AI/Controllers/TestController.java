package com.example.SkillTune_AI.Controllers;

import com.example.SkillTune_AI.AwsService;
import com.example.SkillTune_AI.Models.Question;
import com.example.SkillTune_AI.Models.Skill;
import com.example.SkillTune_AI.Models.User;
import com.example.SkillTune_AI.Repositories.SkillRepo;
import com.example.SkillTune_AI.Repositories.UserRepo;
import com.example.SkillTune_AI.Services.ExtractSkillService;
import com.example.SkillTune_AI.Services.GeminiAIService;

import com.example.SkillTune_AI.Services.UserService;
import jakarta.transaction.Status;
import jdk.jshell.Snippet;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RequestMapping("test/")
@RestController
public class TestController {

    @Autowired
    SkillRepo skillRepo;
    @GetMapping("getSkills/")
    ResponseEntity<List<String>> getSkills(){
        List<Skill> list = skillRepo.findAll();
        List<String> skillList = new ArrayList<>();
        for(Skill sk:list){
            skillList.add(sk.getSkillName());
        }
        return (new ResponseEntity<>(skillList, HttpStatus.OK));
    }

    @Autowired
    UserService userService;
    @GetMapping("skills/")
    public Set<String> getUserSkillsByEmail(@RequestParam("email") String email) {
        return userService.getUserSkillsByEmail(email);
    }

    @Autowired
    ExtractSkillService extractSkills;
    @GetMapping("resumeSkills/")
    ResponseEntity<List<String>> getResumeSkills(@RequestParam("file")MultipartFile file) throws IOException, TikaException, SAXException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of("Please Upload file"));
        }

        // Ensure the uploaded file is a PDF
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body(List.of("type mismatched:only pdf files are allowed."));
        }
        List<String> matchedSkills = extractSkills.pdfToSkills(MutliPartToFile(file));
         return new ResponseEntity<>(matchedSkills,HttpStatus.OK);
    }
    @Autowired
    GeminiAIService geminiService;
//    @PostMapping("gemini")
//    public String getGeminiData(@RequestBody Map<String,String> map){
//        String prompt = map.get("prompt");
//        return geminiService.GenerateQuestions(prompt);
//    }

    @Autowired
    UserRepo userRepo;
    @Autowired
    AwsService awsService;
    @PostMapping("skFromResume/")
    ResponseEntity<List<String>> getSkillsFromResume(@RequestPart("file") MultipartFile file)throws IOException, TikaException, SAXException {
        try{
          //  awsService.uploadResumeFile(file);
           List<String> list =  extractSkills.pdfToSkills(MutliPartToFile(file));

           return  new ResponseEntity<>(list,HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            String[] arr  = {"React","Spring Boot","MySQL","SQLite","Python","C++","Java","Firebase"};
            return  new ResponseEntity<>(Arrays.stream(arr).toList(),HttpStatus.OK);

        }
    }

    @Autowired
    ExtractSkillService extractSkillService;
    @PostMapping("IQWithoutLogin/")
    ResponseEntity<List<String>> getIQuestions(@RequestPart("file") MultipartFile resume)throws IOException, TikaException, SAXException {
        try{
            File file = MutliPartToFile(resume);
            List<String> skillsList = extractSkills.pdfToSkills(file);
            StringBuilder str = new StringBuilder();
            for(int i = 0;i<skillsList.size();i++){
                str.append(skillsList.get(i));
            }
            Map<String,String> map =  extractSkillService.getSections(file);
             String jsonQuestions =  geminiService.GenerateQuestionsOnResume(map.get("projects"),map.get("experience"),str.toString());
             List<String> questions = userService.extractInterviewQuestions(jsonQuestions);
             return  new ResponseEntity<>(questions,HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            return  new ResponseEntity<>(null,HttpStatus.CONFLICT);
        }
    }


    @PostMapping("getSections/")
    ResponseEntity<Map<String,String>> getSections(@RequestParam("file") MultipartFile file) throws IOException, TikaException {
        try {

           Map<String,String> map =  extractSkills.getSections(MutliPartToFile(file));
            return  new ResponseEntity<>(map,HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            return  new ResponseEntity<>(null,HttpStatus.CONFLICT);
        }
    }


    private File MutliPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;

    }



}
