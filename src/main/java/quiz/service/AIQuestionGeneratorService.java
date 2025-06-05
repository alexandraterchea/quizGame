package quiz.service;

import quiz.model.Question;
import quiz.model.Category;
import quiz.exceptions.DatabaseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIQuestionGeneratorService {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Random random;

    public AIQuestionGeneratorService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
    }

    /**
     * Generează întrebări AI pentru quiz random
     */
    public List<Question> generateRandomQuestions(int count, int difficultyLevel) throws DatabaseException {
        String[] topics = {
            "Science and Technology", "History", "Geography", "Literature", 
            "Mathematics", "Arts", "Sports", "General Knowledge", "Nature", "Culture"
        };
        
        String selectedTopic = topics[random.nextInt(topics.length)];
        return generateQuestionsForTopic(selectedTopic, count, difficultyLevel);
    }

    /**
     * Generează întrebări AI pentru o categorie specifică
     */
    public List<Question> generateQuestionsForCategory(Category category, int count, int difficultyLevel) throws DatabaseException {
        return generateQuestionsForTopic(category.getName(), count, difficultyLevel);
    }

    private List<Question> generateQuestionsForTopic(String topic, int count, int difficultyLevel) throws DatabaseException {
        String difficulty = getDifficultyText(difficultyLevel);
        
        String prompt = createPrompt(topic, count, difficulty);
        
        try {
            String response = callOpenAIAPI(prompt);
            return parseAIResponse(response, topic, difficultyLevel);
        } catch (IOException | InterruptedException e) {
            throw new DatabaseException("Error generating AI questions: " + e.getMessage(), e);
        }
    }

    private String createPrompt(String topic, int count, String difficulty) {
        return String.format(
            "Generate %d multiple choice questions about %s with %s difficulty level. " +
            "Format each question as JSON with the following structure:\n" +
            "{\n" +
            "  \"question\": \"Question text here?\",\n" +
            "  \"optionA\": \"First option\",\n" +
            "  \"optionB\": \"Second option\",\n" +
            "  \"optionC\": \"Third option\",\n" +
            "  \"optionD\": \"Fourth option\",\n" +
            "  \"correctOption\": \"A\" (or B, C, D)\n" +
            "}\n" +
            "Return only a JSON array containing all questions. Make sure questions are educational, accurate, and appropriate for a quiz game.",
            count, topic, difficulty
        );
    }

    private String callOpenAIAPI(String prompt) throws IOException, InterruptedException {
        String requestBody = String.format(
            "{\n" +
            "  \"model\": \"gpt-3.5-turbo\",\n" +
            "  \"messages\": [\n" +
            "    {\n" +
            "      \"role\": \"user\",\n" +
            "      \"content\": \"%s\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"max_tokens\": 2000,\n" +
            "  \"temperature\": 0.7\n" +
            "}",
            prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("OpenAI API error: " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }

    private List<Question> parseAIResponse(String apiResponse, String topic, int difficultyLevel) throws DatabaseException {
        try {
            JsonNode root = objectMapper.readTree(apiResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            // Parse the JSON array of questions
            JsonNode questionsArray = objectMapper.readTree(content);
            
            List<Question> questions = new ArrayList<>();
            
            for (JsonNode questionNode : questionsArray) {
                Question question = new Question();
                question.setCategoryId(1); // Default category, you might want to map this properly
                question.setCategoryName(topic);
                question.setText(questionNode.path("question").asText());
                question.setOptionA(questionNode.path("optionA").asText());
                question.setOptionB(questionNode.path("optionB").asText());
                question.setOptionC(questionNode.path("optionC").asText());
                question.setOptionD(questionNode.path("optionD").asText());
                question.setCorrectOption(questionNode.path("correctOption").asText().charAt(0));
                question.setDifficultyLevel(difficultyLevel);
                
                questions.add(question);
            }
            
            return questions;
        } catch (Exception e) {
            throw new DatabaseException("Error parsing AI response: " + e.getMessage(), e);
        }
    }

    private String getDifficultyText(int level) {
        switch (level) {
            case 1: return "easy";
            case 2: return "medium";
            case 3: return "hard";
            default: return "medium";
        }
    }

    /**
     * Fallback method pentru când AI nu este disponibil
     */
    public List<Question> generateFallbackQuestions(int count, int difficultyLevel) {
        List<Question> fallbackQuestions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Question q = new Question();
            q.setCategoryId(1);
            q.setCategoryName("General Knowledge");
            q.setText("Sample AI-generated question " + (i + 1) + "?");
            q.setOptionA("Option A");
            q.setOptionB("Option B");
            q.setOptionC("Option C");
            q.setOptionD("Option D");
            q.setCorrectOption('A');
            q.setDifficultyLevel(difficultyLevel);
            
            fallbackQuestions.add(q);
        }
        
        return fallbackQuestions;
    }
}