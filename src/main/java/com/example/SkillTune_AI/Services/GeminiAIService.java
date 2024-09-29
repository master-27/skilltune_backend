package com.example.SkillTune_AI.Services;

import com.example.SkillTune_AI.Models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAIService {

    private final WebClient webClient;
    private final String apiKey = "AIzaSyBc97dO1lxGaWc5eTfMhvHuPlEa1PBBIRI";  // Ensure to store your API key securely

    public GeminiAIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")  // Base URL for the API
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String GenerateQuestions(String topic, String level, String num) {
        // Define the prompt to ask for cookie recipes
        String prompt = "Please generate exactly " + num + " multiple-choice questions for the following level: " + level +
                ". The questions should be focused on the topic: " + topic + ". Each question must have exactly 4 unique options, with exactly 1 correct option among them. Additionally, provide a concise explanation of the correct answer for each question in the minimum possible words. Ensure strict adherence to the request for the specified number of questions.";

        // JSON body for the POST request with schema
        String requestBody = String.format(
                "{\n" +
                        "  \"contents\": [{\n" +
                        "    \"parts\": [{\n" +
                        "      \"text\": \"%s\"\n" +
                        "    }]\n" +
                        "  }],\n" +
                        "  \"generationConfig\": {\n" +
                        "    \"response_mime_type\": \"application/json\",\n" +
                        "    \"response_schema\": {\n" +
                        "      \"type\": \"ARRAY\",\n" +
                        "      \"items\": {\n" +
                        "        \"type\": \"OBJECT\",\n" +
                        "        \"properties\": {\n" +
                        "          \"question\": { \"type\": \"STRING\" },\n" +
                        "          \"options\": {\n" +
                        "            \"type\": \"ARRAY\",\n" +
                        "            \"items\": { \"type\": \"STRING\" }\n" +
                        "          },\n" +
                        "          \"correct_answer\": { \"type\": \"STRING\" },\n" +
                        "          \"explanation\": { \"type\": \"STRING\" }\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"
                , prompt);

        // WebClient POST request to the Gemini API
        return webClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }


    public  String GenerateQuestionsOnResume(String projects, String experience, String skills){

        final String prompt = "\n" +
                "Projects: \n" +
              projects+"\n" +
                "\n" +
                "Experience: \n" +
                experience+
                "\n" +
                "Skills: \n" +
               skills+
                "\n" +
                "In addition, please generate a few behavioral and soft skills questions relevant to a early level professional, focusing on teamwork, leadership, communication, and problem-solving.\n" +
                "\n" +
                "I need the following:\n" +
                "- 5 questions related to the technical challenges and decisions I made in my projects.\n" +
                "- 5 questions focusing on my technical skills and how I’ve applied them in my work.\n" +
                "- 3-5 behavioral questions about my teamwork, leadership, and handling tight deadlines or difficult situations.\n" +
                "\n" +
                "Please ensure the questions should be of easy level with not more than 30 words.";
//        and a mix of technical depth and open-ended scenarios
        String requestBody = String.format(
                "{\n" +
                        "  \"contents\": [{\n" +
                        "    \"parts\": [{\n" +
                        "      \"text\": \"%s\"\n" +
                        "    }]\n" +
                        "  }],\n" +
                        "  \"generationConfig\": {\n" +
                        "    \"response_mime_type\": \"application/json\",\n" +
                        "    \"response_schema\": {\n" +
                        "      \"type\": \"ARRAY\",\n" +
                        "      \"items\": {\n" +
                        "        \"type\": \"OBJECT\",\n" +
                        "        \"properties\": {\n" +
                        "          \"question\": { \"type\": \"STRING\" },\n" +
                        "          \"options\": {\n" +
                        "            \"type\": \"ARRAY\",\n" +
                        "            \"items\": { \"type\": \"STRING\" }\n" +
                        "          },\n" +
                        "          \"correct_answer\": { \"type\": \"STRING\" },\n" +
                        "          \"explanation\": { \"type\": \"STRING\" }\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                prompt
        );


        return webClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }




    public Map<String, Object> evaluateAnswers(Map<String, String> QandA) {
        // List to hold the evaluation criteria and question-answer pairs
        List<Map<String, String>> questionAnswerList = new ArrayList<>();

        // Prepare question-answer pairs for the request
        for (Map.Entry<String, String> entry : QandA.entrySet()) {
            Map<String, String> questionAnswer = new HashMap<>();
            questionAnswer.put("question", entry.getKey());
            questionAnswer.put("answer", entry.getValue());
            questionAnswerList.add(questionAnswer);
        }

        // Convert the question-answer list into JSON string
        String questionAnswerJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            questionAnswerJson = objectMapper.writeValueAsString(questionAnswerList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while converting question-answer list to JSON", e);
        }

        // Construct the prompt
        String prompt =
                "Evaluate the following question and answer pairs on the following criteria. Each answer should be rated on a scale of 100, with feedback provided for each response.\n" +
                        "\n" +
                        "**Evaluation Criteria:**\n" +
                        "1. **Technical Knowledge:** Assess the correctness of the information and the use of appropriate technical terms relevant to the subject.\n" +
                        "2. **Clarity of Explanation:** Evaluate how clearly and concisely the answer is communicated, especially for complex concepts.\n" +
                        "3. **English Proficiency:** Evaluate the quality of spoken or written English, including grammar, fluency, vocabulary, and coherence.\n" +
                        "4. **Confidence:** Consider how confidently the answer is provided. Assess any signs of hesitation, uncertainty, or lack of clarity.\n" +
                        "5. **Relevance to the Question:** Ensure that the response directly addresses the question and provides relevant details.\n" +
                        "\n" +
                        "**Questions and Answers to Evaluate:**\n" +
                        questionAnswerJson +  // This contains your question-answer JSON data
                        "\nFor each pair, provide:\n" +
                        "- **Rating (out of 100):** A score reflecting the overall quality of the answer.\n" +
                        "- **Feedback:** Specific, actionable feedback on how to improve in each of the evaluation areas.\n" +
                        "- **Question:** The question for which feedback is being provided.\n";

        // Create the request body in the required format for Gemini API
        String requestBody = String.format(
                "{\n" +
                        "  \"contents\": [{\n" +
                        "    \"parts\": [{\n" +
                        "      \"text\": \"%s\"\n" +
                        "    }]\n" +
                        "  }],\n" +
                        "  \"generationConfig\": {\n" +
                        "    \"response_mime_type\": \"application/json\",\n" +
                        "    \"response_schema\": {\n" +
                        "      \"type\": \"ARRAY\",\n" +
                        "      \"items\": {\n" +
                        "        \"type\": \"OBJECT\",\n" +
                        "        \"properties\": {\n" +
                        "          \"question\": { \"type\": \"STRING\" },\n" +  // Added question property
                        "          \"rating\": { \"type\": \"NUMBER\" },\n" +
                        "          \"feedback\": { \"type\": \"STRING\" }\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                prompt.replace("\"", "\\\"") // Escape quotes for valid JSON
        );



        // Send the request to Gemini API and retrieve the response
        Map<String, Object> response = this.webClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Process the response and return it
        return processEvaluationResponse(response);
    }

    private Map<String, Object> processEvaluationResponse(Map<String, Object> response) {
        System.out.println(response.toString());

        // Access the candidates list from the response
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");

        // Handle the case where there are no candidates
        if (candidates == null || candidates.isEmpty()) {
            return new HashMap<>(); // Return empty map if no candidates
        }

        // Get the first candidate's content
        Map<String, Object> firstCandidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        double totalRating = 0.0; // Keep this as double
        int countAnswers = 0;
        List<Map<String, Object>> feedbackList = new ArrayList<>();

        // Iterate over each part (assuming parts contain the feedback, rating, and question information)
        for (Map<String, Object> part : parts) {
            // Extract the text which contains JSON string
            String jsonText = (String) part.get("text");

            // Parse the JSON text using ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // Assuming the text is a JSON array
                List<Map<String, Object>> evaluations = objectMapper.readValue(jsonText, new TypeReference<List<Map<String, Object>>>() {});

                // Process each evaluation (which should contain question, rating, and feedback)
                for (Map<String, Object> evaluation : evaluations) {
                    String question = (String) evaluation.get("question");
                    String feedback = (String) evaluation.get("feedback");
                    Object ratingObject = evaluation.get("rating");

                    // Check if feedback, rating, and question are present
                    if ((feedback != null || ratingObject != null) && question != null) {
                        Map<String, Object> feedbackData = new HashMap<>();

                        // Add question to the feedback data
                        feedbackData.put("question", question);

                        if (feedback != null) {
                            feedbackData.put("feedback", feedback);
                        }

                        if (ratingObject != null && ratingObject instanceof Double) {
                            // Round the rating to two decimal places
                            double rating = Math.round((Double) ratingObject * 100) / 100.0;
                            feedbackData.put("rating", rating);
                            totalRating += rating; // Sum up the rating for average calculation
                            countAnswers++;
                        }

                        // Only add feedback if there was a valid question, feedback, or rating
                        if (!feedbackData.isEmpty()) {
                            feedbackList.add(feedbackData);
                        }
                    }
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                // Handle JSON parsing exception
            }
        }

        // Calculate average rating if feedbackList is not empty
        double averageRating = countAnswers > 0 ? totalRating / countAnswers : 0.0;  // Keep this as double

        // Prepare final response map with feedback and average rating
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("averageRating", averageRating);
        finalResponse.put("feedback", feedbackList);

        return finalResponse;
    }




}
