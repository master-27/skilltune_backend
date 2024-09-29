package com.example.SkillTune_AI.Services;

import com.example.SkillTune_AI.Models.Question;
import com.example.SkillTune_AI.Models.*;
import com.example.SkillTune_AI.Repositories.ProgressRepo;
import com.example.SkillTune_AI.Repositories.SessionRepo;
import com.example.SkillTune_AI.Repositories.SkillRepo;
import com.example.SkillTune_AI.Repositories.UserRepo;
import org.apache.tika.exception.TikaException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;
    @Autowired
    SkillRepo skillRepo;
    @Autowired
    ExtractSkillService extractSkillService;

    public User createUser(String username, String email , String password, File file , List<String> skillList) throws TikaException, IOException, SAXException {
        List<String> skills;
        Map<String,String> map = new HashMap<>();
        if(file != null) {
            skills = extractSkillService.pdfToSkills(file);
            map = extractSkillService.getSections(file);
        }
        else{
            skills = skillList;
        }
        System.out.println("Skills parse from resume: "+ skills.toString());
        List<Skill> SkillsList= skillRepo.getSkillsByName(skills);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setProjects(map.get("projects"));
        user.setExperience(map.get("experience"));
        Set<UserSkill> userSkills = new HashSet<UserSkill>();
        for(Skill skill:SkillsList){
            UserSkill userSkill = new UserSkill(null,user,skill);
            userSkills.add(userSkill);
        }
        System.out.println("skills: "+ userSkills);
        user.setUserSkills(userSkills);
        System.out.println("print"+ user.toString());
        userRepo.saveAndFlush(user);
        User u = userRepo.findByEmail(user.getEmail());
        System.out.println(u == null ? "user is null" : "user found: " + u);
        if(u!= null)
        System.out.println("here  "+ u.getUserSkills());
        return  user;
    }

    public Set<String> getUserSkillsByEmail(String email) {
        User user = userRepo.findByEmail(email);
        if (user != null) {
            return user.getUserSkills().stream()
                    .map(userSkill -> userSkill.getSkill().getSkillName())
                    .collect(Collectors.toSet());
        }
        return null; // or throw an exception if user is not found
    }




    public List<Question> extractQuestions(String jsonResponse, Skill skill, String level, String num) {
        List<Question> questions = new ArrayList<>();
        JSONObject response = new JSONObject(jsonResponse);
        JSONArray candidates = response.getJSONArray("candidates");
        JSONArray questionArray = new JSONArray();

        int attempts = 0;
        final int MAX_ATTEMPTS = 2;

        while (attempts < MAX_ATTEMPTS) {
            try {
                // Try extracting the content and questions from the response
                String text = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                questionArray = new JSONArray(text);
                break; // Break the loop if successful
            } catch (JSONException e) {
                // If content is not found, retry API call
                attempts++;
                System.err.println("API call failed or content not found. Attempt: " + attempts);

                if (attempts < MAX_ATTEMPTS) {
                    // Retry the API call
                    JSONObject retryResponse = makeApiCall(skill,level,num);
                    if (retryResponse != null) {
                        response = retryResponse;
                        candidates = response.getJSONArray("candidates");
                    } else {
                        // If retry failed, exit and throw an exception or return an empty list
                        System.err.println("Max retries reached. Could not retrieve content.");
                        return questions;
                    }
                } else {
                    // Max attempts reached, handle failure case
                    System.err.println("Max attempts reached. Exiting.");
                    return questions; // Return empty list or handle failure
                }
            }
        }

        // Process the questions if content is found
        for (int i = 0; i < questionArray.length(); i++) {
            JSONObject questionObj = questionArray.getJSONObject(i);
            String question = questionObj.getString("question");
            String correctAnswer = questionObj.getString("correct_answer");
            String explanation = questionObj.getString("explanation");
            JSONArray optionsArr = questionObj.getJSONArray("options");

            StringBuilder options = new StringBuilder();
            for (int j = 0; j < optionsArr.length(); j++) {
                options.append(optionsArr.getString(j));
                options.append("~$");
            }

            questions.add(new Question(Long.valueOf(i), skill.getSkillName(), question, options.toString(), correctAnswer, explanation));
        }

        System.out.println("Inside service: " + questions.toString());
        return questions;
    }

    @Autowired
    GeminiAIService geminiAIService;
    private JSONObject makeApiCall(Skill skill,String level, String num) {
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            try {
                // Call your API service to generate questions based on skill
                String apiResponse = geminiAIService.GenerateQuestions(skill.getSkillName(),level,num); // Example API call
                if (apiResponse != null) {
                    // If response is valid, return as JSONObject
                    return new JSONObject(apiResponse);
                }
            } catch (Exception e) {
                attempts++;
                System.err.println("API call failed. Attempt: " + attempts);
            }
        }

        // Return null if max attempts failed
        return null;
    }
    @Autowired
    SessionRepo sessionRepo;
    @Autowired
    ProgressRepo progressRepo;
    public void saveSession(Session session){
        sessionRepo.save(session);
        Progress progress = progressRepo.findByEmail(session.getEmail());
        System.out.println("insdie userService: "   +progress);
        if(progress == null){progress = new Progress(session.getEmail());}
        int percent = (int) (progress.getPracticeSessions()*progress.getPercentage() + session.getPercentage())/(progress.getPracticeSessions()+1);
        progress.setPercentage(percent);
        progress.setPracticeSessions(progress.getPracticeSessions()+1);
        System.out.println("insdie userService: "   +progress);
        progressRepo.save(progress);

    }

    public String  getSkillsName(String email){
        Set<UserSkill> userSkills = userRepo.findByEmail(email).getUserSkills();
        StringBuilder skills = new StringBuilder();
        for(UserSkill skill:userSkills){
            skills.append(skill.getSkill().getSkillName());
        }
        return  skills.toString();
    }

    public  List<String> extractInterviewQuestions(String jsonResponse) {
        List<String> questions = new ArrayList<>();

        // Parse the JSON response
        JSONObject jsonObj = new JSONObject(jsonResponse);
        JSONArray candidates = jsonObj.getJSONArray("candidates");

        if (candidates.length() > 0) {
            JSONObject contentObj = candidates.getJSONObject(0).getJSONObject("content");
            JSONArray parts = contentObj.getJSONArray("parts");

            if (parts.length() > 0) {
                // Extract the "text" part
                String text = parts.getJSONObject(0).getString("text");

                // Parse the text as a JSON array
                JSONArray questionArray = new JSONArray(text);

                // Loop through each question object and add the question to the list
                for (int i = 0; i < questionArray.length(); i++) {
                    JSONObject questionObj = questionArray.getJSONObject(i);
                    String question = questionObj.getString("question");
                    questions.add(question);
                }
            }
        }

        return questions;
    }




}
