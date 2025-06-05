package quiz.controller;

import quiz.dao.*;
import quiz.service.QuizService;
import quiz.service.QuestionService;
import quiz.model.Question;
import quiz.model.QuizSession;
import quiz.model.User;
import quiz.exceptions.DatabaseException;
import quiz.ui.GameFrame;

import java.util.List;

public class QuizController {
    private QuizService quizService;
    private User currentUser;
    private QuestionDAO questionDAO;
    private QuizSessionDAO quizSessionDAO;
    private ScoreDAO scoreDAO;
    private AchievementDAO achievementDAO;
    private AnalyticsDAO analyticsDAO;
    private GameFrame gameFrame;
    
    // Current quiz state
    private List<Question> currentQuestions;
    private int currentQuizSessionId;

    // Constructor pentru GameFrame (cu toate argumentele)
    public QuizController(User currentUser, QuestionDAO questionDAO, QuizSessionDAO quizSessionDAO, 
                         ScoreDAO scoreDAO, AchievementDAO achievementDAO, AnalyticsDAO analyticsDAO, 
                         GameFrame gameFrame) {
        this.currentUser = currentUser;
        this.questionDAO = questionDAO;
        this.quizSessionDAO = quizSessionDAO;
        this.scoreDAO = scoreDAO;
        this.achievementDAO = achievementDAO;
        this.analyticsDAO = analyticsDAO;
        this.gameFrame = gameFrame;
        this.quizService = new QuizService();
    }

    // Constructor simplu (pentru compatibilitate)
    public QuizController() {
        this.quizService = new QuizService();
        this.questionDAO = new QuestionDAO();
        this.quizSessionDAO = new QuizSessionDAO();
        this.scoreDAO = new ScoreDAO();
        this.achievementDAO = new AchievementDAO();
        this.analyticsDAO = new AnalyticsDAO();
    }

    // Metoda pentru a începe un quiz
    public void startQuiz(int userId, String quizType, int questionCount, int categoryId) throws DatabaseException {
        List<Question> questions;
        
        if ("random".equals(quizType)) {
            questions = questionDAO.getRandomQuestions(questionCount);
        } else if ("category".equals(quizType) && categoryId != -1) {
            questions = questionDAO.getQuestionsByCategory(categoryId, questionCount);
        } else {
            questions = questionDAO.getRandomQuestions(questionCount);
        }
        
        // Creează sesiunea de quiz
        QuizSession session = new QuizSession(userId, questionCount, quizType);
        this.currentQuizSessionId = quizSessionDAO.createQuizSession(session);
        this.currentQuestions = questions;
    }

    // Getter pentru întrebări
    public List<Question> getQuestions() {
        return currentQuestions;
    }

    // Getter pentru ID-ul sesiunii
    public int getQuizSessionId() {
        return currentQuizSessionId;
    }

    // Metoda pentru gestionarea submisiei răspunsului
    public void handleAnswerSubmission(int userId, int sessionId, int questionId, char selectedOption, 
                                     boolean isCorrect, int timeTaken) throws DatabaseException {
        scoreDAO.saveScore(userId, sessionId, questionId, selectedOption, isCorrect, timeTaken);
    }

    // Metoda pentru finalizarea quiz-ului
    public void finishQuiz(int sessionId, int correctAnswers, int totalTimeTaken) throws DatabaseException {
        quizSessionDAO.completeQuizSession(sessionId, correctAnswers, correctAnswers, totalTimeTaken);
    }

    // Metodă pentru a obține întrebări filtrate pentru UI (folosind streams)
    public List<Question> getQuestionsByDifficulty(int difficulty) {
        try {
            return quizService.getFilteredQuestionsByDifficulty(difficulty);
        } catch (DatabaseException e) {
            System.err.println("Eroare la filtrarea întrebărilor: " + e.getMessage());
            return List.of(); // Lista goală în caz de eroare
        }
    }

    // Metodă alternativă care folosește direct DAO-ul
    public List<Question> getQuestionsByDifficultyDirect(int difficulty) {
        try {
            return quizService.getQuestionsByDifficultyDirect(difficulty);
        } catch (DatabaseException e) {
            System.err.println("Eroare la obținerea întrebărilor: " + e.getMessage());
            return List.of();
        }
    }

    // Metodă pentru quiz personalizat
    public void startCustomQuiz(int userId, int difficulty, String category) {
        try {
            List<Question> questions = quizService.startCustomQuiz(userId, difficulty, category, 10);
            System.out.println("Quiz personalizat început cu " + questions.size() + " întrebări");
            // Aici poți integra cu UI-ul pentru a afișa întrebările
        } catch (DatabaseException e) {
            System.err.println("Eroare la începerea quiz-ului: " + e.getMessage());
        }
    }

    // Metodă pentru quiz cu întrebări dificile
    public List<Question> getHardQuestions(int count) {
        try {
            return quizService.startHardQuiz(count);
        } catch (DatabaseException e) {
            System.err.println("Eroare la obținerea întrebărilor dificile: " + e.getMessage());
            return List.of();
        }
    }

    // Metodă pentru quiz sortat
    public List<Question> getSortedQuestions(int count) {
        try {
            return quizService.startSortedCategoryQuiz(count);
        } catch (DatabaseException e) {
            System.err.println("Eroare la sortarea întrebărilor: " + e.getMessage());
            return List.of();
        }
    }
}