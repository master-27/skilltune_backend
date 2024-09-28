package com.example.SkillTune_AI.Services;

import com.example.SkillTune_AI.Models.Skill;
import com.example.SkillTune_AI.Repositories.SkillRepo;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;

import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExtractSkillService {

    @Autowired
    private SkillRepo skillRepo;

    public List<String> pdfToSkills(File pdfFile) throws IOException, TikaException, SAXException {
        String extractedText = "";
        PDFParser parser = new PDFParser();
        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler();
        ParseContext context = new ParseContext();

        try (InputStream stream = new FileInputStream(pdfFile)) {
            parser.parse(stream, handler, metadata, context);
            extractedText = handler.toString();
        } catch (IOException | TikaException | SAXException e) {
            // Log the exception
            e.printStackTrace();
            throw e; // Optionally rethrow the exception
        }

        // Check extracted text
        if (extractedText.isEmpty()) {
            System.out.println("No text extracted from PDF.");
        } else {
            System.out.println("Extracted text: " + extractedText);
        }

        List<Skill> skills = skillRepo.findAll();
        String finalExtractedText = extractedText;
        Set<String> matchedSkills = skills.stream()
                .filter(skill -> finalExtractedText.toLowerCase().contains(skill.getSkillName().toLowerCase()))
                .map(Skill::getSkillName)
                .collect(Collectors.toSet());
      return   matchedSkills.stream().collect(Collectors.toList());

    }



    public String extractSection(String resumeText, String startKeyword, String[] endKeywords) {
        String lowerText = resumeText.toLowerCase();
        int startIdx = lowerText.indexOf(startKeyword.toLowerCase());

        if (startIdx == -1) {
            return null;
        }

        startIdx += startKeyword.length();
        int endIdx = resumeText.length();

        for (String endKeyword : endKeywords) {
            int tempIdx = lowerText.indexOf(endKeyword.toLowerCase(), startIdx);
            if (tempIdx != -1 && tempIdx < endIdx) {
                endIdx = tempIdx;
            }
        }

        return resumeText.substring(startIdx, endIdx).trim();
    }

    public Map<String,String> getSections(File resumeFile) throws TikaException, IOException {
        Tika tika = new Tika();
        String resumeText = tika.parseToString(resumeFile);




        String experience = extractSection(resumeText, "experience", new String[]{"projects", "education", "skills"});
        String projects = extractSection(resumeText, "projects", new String[]{"experience", "education", "skills"});

        Map<String, String> result = new HashMap<>();
        result.put("experience", experience);
        result.put("projects", projects);

        return result;
    }


}