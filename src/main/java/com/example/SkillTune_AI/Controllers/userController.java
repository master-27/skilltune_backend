package com.example.SkillTune_AI.Controllers;

import com.example.SkillTune_AI.Models.*;
import com.example.SkillTune_AI.Repositories.ProgressRepo;
import com.example.SkillTune_AI.Repositories.SessionRepo;
import com.example.SkillTune_AI.Repositories.SkillRepo;
import com.example.SkillTune_AI.Repositories.UserRepo;
import com.example.SkillTune_AI.Services.GeminiAIService;
import com.example.SkillTune_AI.Services.RecordingService;
import com.example.SkillTune_AI.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("user/")
public class userController {


    @Autowired
    GeminiAIService geminiAIService;

    @Autowired
    UserService userService;
    @Autowired
    SkillRepo skillRepo;
    @Autowired
    SessionRepo sessionRepo;
    @PostMapping("getQuestions/")
    ResponseEntity<List<Question>> getQuestions(@RequestBody Map<String,String> request ){
        try {
            String jsonQuestions = String.valueOf(geminiAIService.GenerateQuestions(request.get("skill"),request.get("level"),request.get("questionNum")));
            Skill sk= skillRepo.findBySkillName(request.get("skill"));
            List<Question> list = userService.extractQuestions(jsonQuestions,sk,request.get("level"),request.get("questionNum"));

            System.out.println("controller usrserice:  "+ list.toString());
            return  new ResponseEntity<>(list,HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            return  new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }

    }



    @PostMapping("saveSession/")
     ResponseEntity<String> saveSession(@RequestBody Session session){
        try {
            userService.saveSession(session);
            return new ResponseEntity<>("inserted",HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("exception: ",HttpStatus.CONFLICT);

        }
    }

    @PostMapping("getSessions/")
    ResponseEntity<List<Session>> getSessions(@RequestParam String email) {
        List<Session> list = sessionRepo.findAllSessionByEmail(email);
        return new ResponseEntity<>(list,HttpStatus.OK);
    }
    @Autowired
    ProgressRepo progressRepo;

    @PostMapping("getProgress/")
    ResponseEntity<Progress> getProgress(@RequestParam String email) {
        try {
         Progress prog =   progressRepo.findByEmail(email);

         System.out.println(prog);
            return new ResponseEntity<>(prog,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null,HttpStatus.CONFLICT);
        }

    }

    @Autowired
    RecordingService recordingService;
    @PostMapping("get")
    public String transcribeAudio(@RequestParam("file") MultipartFile audioFile) throws Exception {

        String text =  recordingService.transcribeAudio(audioFile);
        System.out.println("got transcribe:  "+ text );
        return  text;
    }

    @Autowired
    UserRepo userRepo;
    public  @PostMapping("getInterviewQ")
    ResponseEntity<List<String>> getInterviewQuestions(@RequestParam String email){
        try {
            User user = userRepo.findByEmail(email);
            if(user==null){
                throw new Exception();
            }
           String skills =  userService.getSkillsName(user.getEmail());
            String jsonResponse = geminiAIService.GenerateQuestionsOnResume(user,skills);
            List<String> questions = userService.extractInterviewQuestions(jsonResponse);
            return  new ResponseEntity<>(questions,HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            return  new ResponseEntity<>(null,HttpStatus.CONFLICT);
        }
    }


    @PostMapping("evaluateAnswers")
        ResponseEntity<Map<String, Object>>evaluateAnswers(@RequestBody List<QuestionAnswerDto> QandA){
        try {
            System.out.println(QandA);
            if (QandA == null || QandA.isEmpty()) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            else{
                System.out.println(QandA.get(0));
            }

            Map<String, String> qaMap = new HashMap<>();
            for (QuestionAnswerDto qa : QandA) {
                String question = qa.getQuestion();
                String answer = qa.getAnswer();
                qaMap.put(question,answer);
            }
            Map<String,Object> map = geminiAIService.evaluateAnswers(qaMap);
            return new ResponseEntity<>(map,HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            return  new ResponseEntity<>(null,HttpStatus.CONFLICT);

        }

        }

}
