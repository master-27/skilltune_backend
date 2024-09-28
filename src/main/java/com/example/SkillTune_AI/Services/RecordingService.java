package com.example.SkillTune_AI.Services;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
//import com.example.SkillTune_AI.AwsService;
import com.example.SkillTune_AI.AwsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RecordingService {

    private final AssemblyAI client;
    //api key of assembly ai to convert speech to text.
    public RecordingService() {
        client =AssemblyAI.builder()
                .apiKey("014482a2940047f1a33dcaa71133b262")
                .build();
    }

    @Autowired
    AwsService awsService;
    public String transcribeAudio(MultipartFile audioFile) throws Exception {
        String bucketName = "skilltune-bucket";
        String audioUrl = awsService.uploadAudioFile(
                bucketName,
                audioFile.getInputStream(),
                audioFile.getSize(),
                audioFile.getContentType()
        );





        Transcript transcript = client.transcripts().transcribe(audioUrl);

        if (transcript.getStatus() == TranscriptStatus.ERROR) {
            throw new Exception("Transcript failed with error: " + transcript.getError().get());
        }



        String tr =  transcript.getText().orElse("");
        if(tr.equals("")) System.out.println("nothing found in audio: "+ tr);
        return tr;
    }



}
