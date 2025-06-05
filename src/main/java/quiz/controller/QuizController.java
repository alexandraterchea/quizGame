package quiz.controller;

import quiz.dao.*;
import quiz.service.QuizService;
import quiz.model.Question;
import quiz.model.QuizSession;
import quiz.model.User;
import quiz.exceptions.DatabaseException;
import quiz.ui.GameFrame;

import java.util.List;

public class QuizController {
    private final QuizService quizService;
    private final User currentUser;
    private final QuestionDAO questionDAO;
    private final QuizSessionDAO quizSessionDAO;
    private final ScoreDAO scoreDAO;
    private final AchievementDAO achievementDAO;
    private final AnalyticsDAO analyticsDAO;
    private final GameFrame gameFrame;

    private List<Question> currentQuestions;
    private int currentQuizSessionId;

    // Constructor pentru GameFrame
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

    //determina tipul quiz-ului si alege intrebarile corespunzatoare
    public void startQuiz(int userId, String quizType, int questionCount, int categoryId) throws DatabaseException {
        List<Question> questions;
        
        switch (quizType.toLowerCase()) {
            case "random":
                questions = questionDAO.getRandomQuestions(questionCount);
                break;
            case "ai_random":
                try {
                    questions = quizService.startAIRandomQuiz(userId, questionCount, 2);
                    if (questions.isEmpty()) {
                        throw new DatabaseException("AI service not available");
                    }
                } catch (Exception e) {
                    throw new DatabaseException("AI Quiz is currently unavailable. Please try another quiz type.");
                }
                break;
            case "category":
                if (categoryId != -1) {
                    questions = questionDAO.getQuestionsByCategory(categoryId, questionCount);
                } else {
                    questions = questionDAO.getRandomQuestions(questionCount);
                }
                break;
            case "hybrid":
                questions = quizService.startHybridQuiz(userId, questionCount, 2);
                break;
            default:
                questions = questionDAO.getRandomQuestions(questionCount);
        }
        
        //creaza sesiunea de quiz
        QuizSession session = new QuizSession(userId, questionCount, quizType);
        this.currentQuizSessionId = quizSessionDAO.createQuizSession(session);
        this.currentQuestions = questions;
    }

    public List<Question> getQuestions() {
        return currentQuestions;
    }

    public int getQuizSessionId() {
        return currentQuizSessionId;
    }

    //salveaza raspunsul in bd, inregistreaza timpul, verif raspunsul si actualizeaza scorul
    public void handleAnswerSubmission(int userId, int sessionId, int questionId, char selectedOption, 
                                     boolean isCorrect, int timeTaken) throws DatabaseException {
        scoreDAO.saveScore(userId, sessionId, questionId, selectedOption, isCorrect, timeTaken);
    }

    //marcheaza sesiunea ca finalizata si actualizeaza scorul
    public void finishQuiz(int sessionId, int correctAnswers, int totalTimeTaken) throws DatabaseException {
        quizSessionDAO.completeQuizSession(sessionId, correctAnswers, correctAnswers, totalTimeTaken);
    }

    // obt intrebari filtrate pentru UI (folosind streams)
    public List<Question> getQuestionsByDifficulty(int difficulty) {
        try {
            return quizService.getFilteredQuestionsByDifficulty(difficulty);
        } catch (DatabaseException e) {
            System.err.println("Eroare la filtrarea întrebărilor: " + e.getMessage());
            return List.of(); // Lista goală în caz de eroare
        }
    }
}