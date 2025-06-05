package quiz.service;

import quiz.ai.GeminiQuestionGeneratorService;
import quiz.dao.QuestionDAO;
import quiz.dao.ScoreDAO;
import quiz.dao.QuizSessionDAO;
import quiz.exceptions.DatabaseException;
import quiz.model.Question;
import quiz.dao.AnalyticsDAO;
import quiz.dao.AchievementDAO;

import java.util.List;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

public class QuizService {
    private final QuestionDAO questionDAO;
    private final ScoreDAO scoreDAO;
    private final QuizSessionDAO sessionDAO;
    private final AnalyticsDAO analyticsDAO;
    private final AchievementDAO achievementDAO;
    private GeminiQuestionGeneratorService geminiService;

    public QuizService() {
        //initializare dao
        this.questionDAO = new QuestionDAO();
        this.scoreDAO = new ScoreDAO();
        this.sessionDAO = new QuizSessionDAO();
        this.analyticsDAO = new AnalyticsDAO();
        this.achievementDAO = new AchievementDAO();

        //verif daca exista api key pt ai
        String apiKey = System.getenv("GEMINI_API_KEY"); // SCHIMBĂ AICI

        System.out.println("Looking for Gemini API key...");
        System.out.println("API Key found: " + (apiKey != null ? "YES (length: " + apiKey.length() + ")" : "NO"));

        if (apiKey != null && !apiKey.isEmpty()) {
            this.geminiService = new GeminiQuestionGeneratorService(apiKey);
            System.out.println("Gemini AI Service initialized successfully!");
        } else {
            System.out.println("No Gemini API key found. AI features will use fallback questions.");
        }
    }

    public List<Question> startAIRandomQuiz(int userId, int questionCount, int difficultyLevel) throws DatabaseException {
        if (geminiService != null) {
            try {
                System.out.println("Generating " + questionCount + " random AI questions (difficulty: " + difficultyLevel + ")");
                return geminiService.generateRandomQuestions(questionCount, difficultyLevel);
            } catch (DatabaseException e) {
                System.err.println("AI generation failed, falling back to database questions: " + e.getMessage());
                return questionDAO.getRandomQuestions(questionCount);
            }
        } else {
            System.out.println("No AI service available, using database questions");
            return questionDAO.getRandomQuestions(questionCount);
        }
    }


    // Metoda pentru quiz AI random
    public List<Question> startAICategoryQuiz(int userId, String categoryName, int questionCount, int difficultyLevel) throws DatabaseException {
        if (geminiService != null) {
            try {
                System.out.println("Generating AI questions for category: " + categoryName);
                quiz.model.Category category = new quiz.model.Category();
                category.setName(categoryName);
                return geminiService.generateQuestionsForCategory(category, questionCount, difficultyLevel);
            } catch (DatabaseException e) {
                System.err.println("AI generation failed, falling back to database questions: " + e.getMessage());
                return questionDAO.getRandomQuestions(questionCount);
            }
        } else {
            //fallback la intrebari din bd
            return questionDAO.getRandomQuestions(questionCount);
        }
    }

    //intrebari bd+ai
    public List<Question> startHybridQuiz(int userId, int questionCount, int difficultyLevel) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        int dbQuestions = questionCount / 2;
        int aiQuestions = questionCount - dbQuestions;

        List<Question> dbQuestionsList = questionDAO.getRandomQuestions(dbQuestions);
        questions.addAll(dbQuestionsList);

        if (geminiService != null) { // SCHIMBĂ AICI
            try {
                List<Question> aiQuestionsList = geminiService.generateRandomQuestions(aiQuestions, difficultyLevel);
                questions.addAll(aiQuestionsList);
            } catch (DatabaseException e) {
                System.err.println("AI generation failed, adding more DB questions: " + e.getMessage());
                List<Question> additionalDb = questionDAO.getRandomQuestions(aiQuestions);
                questions.addAll(additionalDb);
            }
        } else {
            List<Question> additionalDb = questionDAO.getRandomQuestions(aiQuestions);
            questions.addAll(additionalDb);
        }

        //amesteca intrebarile
        Collections.shuffle(questions);
        return questions;
    }

    public List<Question> startCustomQuiz(int userId, int difficultyLevel, String category, int questionCount) throws DatabaseException {
        if (geminiService != null && category != null) { // SCHIMBĂ AICI
            try {
                return startAICategoryQuiz(userId, category, questionCount, difficultyLevel);
            } catch (DatabaseException e) {
                System.err.println("AI generation failed for category, using database: " + e.getMessage());
            }
        }

        List<Question> allQuestions = questionDAO.getRandomQuestions(100);
        //filtreaza intrebarile din bd
        List<Question> filteredQuestions = allQuestions.stream()
                .filter(q -> q.getDifficultyLevel() == difficultyLevel)
                .filter(q -> category == null || q.getCategoryName() != null && q.getCategoryName().equals(category))
                .limit(questionCount)
                .collect(Collectors.toList());

        return filteredQuestions;
    }

    public List<Question> getFilteredQuestionsByDifficulty(int difficultyLevel) throws DatabaseException {
        List<Question> allQuestions = questionDAO.getRandomQuestions(100);
        QuestionService service = new QuestionService(allQuestions);
        return service.getQuestionsByDifficulty(difficultyLevel);
    }

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

}