package com.example.SkillTune_AI.Controllers;

import com.example.SkillTune_AI.Models.User;
import com.example.SkillTune_AI.Repositories.UserRepo;
import com.example.SkillTune_AI.Services.ExtractSkillService;
import com.example.SkillTune_AI.Services.UserService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import com.example.SkillTune_AI.Models.UserDao;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequestMapping("auth/")
@RestController
public class authController {

    @Autowired
    ExtractSkillService extractSkillService;
    @Autowired
    UserService userService;
    @Autowired
    UserRepo userRepo;
    @PostMapping("signUpWithResume/")
    ResponseEntity<String> signWithResume(@RequestParam String username , @RequestParam String email , @RequestParam String password, @RequestPart MultipartFile file) throws IOException, TikaException, SAXException {

        try {
            userService.createUser(username,email,password,MutliPartToFile(file),null);
//            System.out.println("here in authController: "+ user.getUserSkills().toString());
//            userRepo.save(user);
//            System.out.println("Here from auth: " + userRepo.findByEmail(user.getEmail()).getUserSkills().toString());
            return  new ResponseEntity<>("Account Created ",HttpStatus.OK);
        }
       catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Exception Occured", HttpStatus.CONFLICT);
       }
    }

    @PostMapping("login/")
    public ResponseEntity<UserDao> login(@RequestBody Map<String,String> map){
        if(!userRepo.existByEmail(map.get("email"))){
            return  new ResponseEntity<>(null,HttpStatus.NOT_ACCEPTABLE);
        }
        User u = userRepo.findByEmail(map.get("email"));
        if(u.getPassword().equals(map.get("password"))){
            return new ResponseEntity<>(new UserDao(u.getUsername(),u.getEmail()),HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null,HttpStatus.CONFLICT);
        }
    }

    @PostMapping("signUpWithoutResume/")
    public ResponseEntity<String>  signWithoutResume(@RequestParam String username, @RequestParam String email , @RequestParam String password, @RequestParam List<String> skills) throws TikaException, IOException, SAXException {
       try {
           userService.createUser(username, email,password, null, skills);
           return  new ResponseEntity<>("User SignedUp ",HttpStatus.OK);
       }
       catch (Exception e){
           e.printStackTrace();
           return  new ResponseEntity<>(null,HttpStatus.CONFLICT);
       }

    }
    @PostMapping("emailExists")
    ResponseEntity<Boolean> emainExists(@RequestParam String email){
        try {
            return new ResponseEntity<>(userRepo.existByEmail(email), HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println("Inside EmailExists");
            e.printStackTrace();
            return  new ResponseEntity<>(false,HttpStatus.CONFLICT);
        }
    }


    private File MutliPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;


    }

}
