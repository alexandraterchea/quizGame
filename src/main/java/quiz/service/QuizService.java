package quiz.service;

import quiz.dao.QuestionDAO;
import quiz.dao.ScoreDAO;
import quiz.dao.QuizSessionDAO;
import quiz.exceptions.DatabaseException;
import quiz.model.Question;
import quiz.model.QuizSession;
import quiz.dao.AnalyticsDAO;
import quiz.dao.AchievementDAO;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

public class QuizService {
    private QuestionDAO questionDAO;
    private ScoreDAO scoreDAO;
    private QuizSessionDAO sessionDAO;
    private AnalyticsDAO analyticsDAO;
    private AchievementDAO achievementDAO;
    private AIQuestionGeneratorService aiService;

    public QuizService() {
        this.questionDAO = new QuestionDAO();
        this.scoreDAO = new ScoreDAO();
        this.sessionDAO = new QuizSessionDAO();
        this.analyticsDAO = new AnalyticsDAO();
        this.achievementDAO = new AchievementDAO();
        
        // Initialize AI service (you'll need to provide API key)
        String apiKey = System.getenv("OPENAI_API_KEY"); // Get from environment variable
        if (apiKey != null && !apiKey.isEmpty()) {
            this.aiService = new AIQuestionGeneratorService(apiKey);
        }
    }

    // În QuizService.java - verifică implementarea metodei startAIRandomQuiz
    public List<Question> startAIRandomQuiz(int userId, int questionCount, int difficultyLevel) throws DatabaseException {
        try {
            if (aiService != null) {
                // Încearcă să generezi întrebări AI
                List<Question> aiQuestions = aiService.generateRandomQuestions(questionCount, difficultyLevel);
                if (!aiQuestions.isEmpty()) {
                    return aiQuestions;
                }
            }
            
            // Fallback: folosește întrebări generate artificial sau afișează un mesaj
            System.out.println("AI service not available, using fallback questions");
            return aiService.generateFallbackQuestions(questionCount, difficultyLevel);
            
        } catch (Exception e) {
            System.err.println("AI question generation failed: " + e.getMessage());
            // Nu te întoarce la întrebările din BD, ci folosește fallback-ul
            return aiService.generateFallbackQuestions(questionCount, difficultyLevel);
        }
    }

    // Quiz cu AI pentru o categorie specifică
    public List<Question> startAICategoryQuiz(int userId, String categoryName, int questionCount, int difficultyLevel) throws DatabaseException {
        if (aiService != null) {
            try {
                System.out.println("Generating AI questions for category: " + categoryName);
                quiz.model.Category category = new quiz.model.Category();
                category.setName(categoryName);
                return aiService.generateQuestionsForCategory(category, questionCount, difficultyLevel);
            } catch (DatabaseException e) {
                System.err.println("AI generation failed, falling back to database questions: " + e.getMessage());
                return questionDAO.getRandomQuestions(questionCount);
            }
        } else {
            return questionDAO.getRandomQuestions(questionCount);
        }
    }

    // Quiz hibrid: combină întrebări din baza de date cu întrebări AI
    public List<Question> startHybridQuiz(int userId, int questionCount, int difficultyLevel) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        
        int dbQuestions = questionCount / 2;
        int aiQuestions = questionCount - dbQuestions;
        
        // Adaugă întrebări din baza de date
        List<Question> dbQuestionsList = questionDAO.getRandomQuestions(dbQuestions);
        questions.addAll(dbQuestionsList);
        
        // Încearcă să adauge întrebări AI
        if (aiService != null) {
            try {
                List<Question> aiQuestionsList = aiService.generateRandomQuestions(aiQuestions, difficultyLevel);
                questions.addAll(aiQuestionsList);
            } catch (DatabaseException e) {
                System.err.println("AI generation failed, adding more DB questions: " + e.getMessage());
                List<Question> additionalDb = questionDAO.getRandomQuestions(aiQuestions);
                questions.addAll(additionalDb);
            }
        } else {
            // Adaugă mai multe întrebări din DB dacă AI nu e disponibil
            List<Question> additionalDb = questionDAO.getRandomQuestions(aiQuestions);
            questions.addAll(additionalDb);
        }
        
        // Amestecă întrebările
        Collections.shuffle(questions);
        return questions;
    }

    // Metodă pentru a obține întrebări filtrate după dificultate
    public List<Question> getFilteredQuestionsByDifficulty(int difficultyLevel) throws DatabaseException {
        List<Question> allQuestions = questionDAO.getRandomQuestions(100);
        QuestionService service = new QuestionService(allQuestions);
        return service.getQuestionsByDifficulty(difficultyLevel);
    }

    // Metodă pentru a obține întrebări filtrate după categorie
    public List<Question> getFilteredQuestionsByCategory(String category) throws DatabaseException {
        List<Question> allQuestions = questionDAO.getRandomQuestions(100);
        QuestionService service = new QuestionService(allQuestions);
        return service.getQuestionsByCategory(category);
    }

    // Quiz personalizat cu filtrare avansată (poate include AI)
    public List<Question> startCustomQuiz(int userId, int difficultyLevel, String category, int questionCount) throws DatabaseException {
        // Încearcă să folosească AI pentru categoria specificată
        if (aiService != null && category != null) {
            try {
                return startAICategoryQuiz(userId, category, questionCount, difficultyLevel);
            } catch (DatabaseException e) {
                System.err.println("AI generation failed for category, using database: " + e.getMessage());
            }
        }
        
        // Fallback la metodele existente
        List<Question> allQuestions = questionDAO.getRandomQuestions(100);
        
        List<Question> filteredQuestions = allQuestions.stream()
            .filter(q -> q.getDifficultyLevel() == difficultyLevel)
            .filter(q -> category == null || q.getCategoryName() != null && q.getCategoryName().equals(category))
            .limit(questionCount)
            .collect(Collectors.toList());

        return filteredQuestions;
    }

    // Restul metodelor rămân la fel...
    public List<Question> startSortedCategoryQuiz(int questionCount) throws DatabaseException {
        List<Question> allQuestions = questionDAO.getRandomQuestions(questionCount * 2);
        QuestionService service = new QuestionService(allQuestions);
        
        return service.getQuestionsSortedByCategory().stream()
            .limit(questionCount)
            .collect(Collectors.toList());
    }

    public List<Question> startHardQuiz(int questionCount) throws DatabaseException {
        List<Question> allQuestions = questionDAO.getRandomQuestions(100);
        QuestionService service = new QuestionService(allQuestions);
        
        return service.getHardQuestionsSorted().stream()
            .limit(questionCount)
            .collect(Collectors.toList());
    }

    public List<Question> getQuestionsByDifficultyDirect(int difficultyLevel) throws DatabaseException {
        return questionDAO.getQuestionsByDifficulty(difficultyLevel);
    }
    
    public List<Question> startAdaptiveQuiz(int userId, int questionCount) throws DatabaseException {
        List<Question> adaptiveQuestions = questionDAO.getAdaptiveQuestions(userId, questionCount);

        System.out.println("Adaptive questions for user " + userId + ":");
        for (Question q : adaptiveQuestions) {
            System.out.println("Difficulty: " + q.getDifficultyLevel() + " - " + q.getText());
        }

        return adaptiveQuestions;
    }

    // Restul metodelor rămân neschimbate...
    public void playCompleteQuiz(int userId, String quizType) {
        // ... (implementarea existentă)
    }

    private void playQuestions(int userId, int sessionId, List<Question> questions) throws DatabaseException {
        // ... (implementarea existentă)
    }

    private void showNewAchievements(int userId) {
        // ... (implementarea existentă)
    }
}