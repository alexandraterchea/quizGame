package quiz.ai;

import quiz.model.Question;
import quiz.model.Category;
import quiz.exceptions.DatabaseException;
import quiz.dao.QuestionDAO;
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

public class GeminiQuestionGeneratorService {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Random random;
    private final QuestionDAO questionDAO;

    public GeminiQuestionGeneratorService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
        this.questionDAO = new QuestionDAO();
    }

    public List<Question> generateRandomQuestions(int count, int difficultyLevel) throws DatabaseException {
        String[] topics = {
            "Science and Technology", "History", "Geography", "Literature", 
            "Mathematics", "Arts", "Sports", "General Knowledge", "Nature", "Culture"
        };
        
        String selectedTopic = topics[random.nextInt(topics.length)];
        return generateQuestionsForTopic(selectedTopic, count, difficultyLevel);
    }

    public List<Question> generateQuestionsForCategory(Category category, int count, int difficultyLevel) throws DatabaseException {
        return generateQuestionsForTopic(category.getName(), count, difficultyLevel);
    }

    private List<Question> generateQuestionsForTopic(String topic, int count, int difficultyLevel) throws DatabaseException {
        String difficulty = getDifficultyText(difficultyLevel);
        String prompt = createPrompt(topic, count, difficulty);
        
        try {
            String response = callGeminiAPI(prompt);
            List<Question> questions = parseAIResponse(response, topic, difficultyLevel);
            
            //salveaza intrebarile generate in bd
            return saveQuestionsToDatabase(questions);
            
        } catch (IOException | InterruptedException e) {
            throw new DatabaseException("Error generating AI questions: " + e.getMessage(), e);
        }
    }

    //salvare in bd
    private List<Question> saveQuestionsToDatabase(List<Question> questions) throws DatabaseException {
        List<Question> savedQuestions = new ArrayList<>();
        
        for (Question question : questions) {
            try {
                //gaseste/creeaza categoria
                int categoryId = findOrCreateCategory(question.getCategoryName());
                question.setCategoryId(categoryId);
                
                //salveaza intrebarea in bd
                int questionId = questionDAO.addQuestion(question);
                question.setId(questionId); // IMPORTANT: Setează ID-ul real din baza de date
                
                savedQuestions.add(question);
                System.out.println("Saved AI question with ID: " + questionId);
                
            } catch (DatabaseException e) {
                System.err.println("Failed to save AI question: " + e.getMessage());
                //continua cu urmatoarea intrebare
            }
        }
        
        return savedQuestions;
    }

    // gaseste sau creeaza categoria
    private int findOrCreateCategory(String categoryName) throws DatabaseException {
        return 1; // ID-ul categoriei "General Knowledge"
    }

    private String createPrompt(String topic, int count, String difficulty) {
        return String.format(
            "Generate %d multiple choice questions about %s with %s difficulty level. " +
            "Format the response as a valid JSON array where each question has this exact structure:\n" +
            "[\n" +
            "  {\n" +
            "    \"question\": \"Question text here?\",\n" +
            "    \"optionA\": \"First option\",\n" +
            "    \"optionB\": \"Second option\",\n" +
            "    \"optionC\": \"Third option\",\n" +
            "    \"optionD\": \"Fourth option\",\n" +
            "    \"correctOption\": \"A\"\n" +
            "  }\n" +
            "]\n" +
            "IMPORTANT: Return ONLY the JSON array, no other text. Make sure questions are educational, accurate, and appropriate for a quiz game. Ensure correct option is one of A, B, C, or D.",
            count, topic, difficulty
        );
    }

    private String callGeminiAPI(String prompt) throws IOException, InterruptedException {
        String requestBody = String.format(
            "{\n" +
            "  \"contents\": [{\n" +
            "    \"parts\": [{\n" +
            "      \"text\": \"%s\"\n" +
            "    }]\n" +
            "  }],\n" +
            "  \"generationConfig\": {\n" +
            "    \"temperature\": 0.7,\n" +
            "    \"maxOutputTokens\": 2048\n" +
            "  }\n" +
            "}",
            prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Gemini API error: " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }

    private List<Question> parseAIResponse(String apiResponse, String topic, int difficultyLevel) throws DatabaseException {
        try {
            JsonNode root = objectMapper.readTree(apiResponse);
            String content = root.path("candidates").get(0)
                               .path("content").path("parts").get(0)
                               .path("text").asText();
            
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();
            
            JsonNode questionsArray = objectMapper.readTree(content);
            List<Question> questions = new ArrayList<>();
            
            for (JsonNode questionNode : questionsArray) {
                Question question = new Question();
                // NU setăm ID-ul aici - va fi setat când salvăm în baza de date
                question.setCategoryId(1);
                question.setCategoryName(topic);
                question.setText(questionNode.path("question").asText());
                question.setOptionA(questionNode.path("optionA").asText());
                question.setOptionB(questionNode.path("optionB").asText());
                question.setOptionC(questionNode.path("optionC").asText());
                question.setOptionD(questionNode.path("optionD").asText());
                
                String correctOption = questionNode.path("correctOption").asText();
                if (correctOption.length() > 0) {
                    question.setCorrectOption(correctOption.charAt(0));
                } else {
                    question.setCorrectOption('A');
                }
                question.setDifficultyLevel(difficultyLevel);
                questions.add(question);
            }
            
            return questions;
        } catch (Exception e) {
            System.err.println("Debug - Raw API response: " + apiResponse);
            throw new DatabaseException("Error parsing Gemini response: " + e.getMessage(), e);
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
}