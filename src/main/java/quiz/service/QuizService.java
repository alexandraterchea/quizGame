package quiz.service;

import quiz.dao.QuestionDAO;
import quiz.dao.ScoreDAO;
import quiz.dao.QuizSessionDAO;
import quiz.exceptions.DatabaseException;
import quiz.model.Question;
import quiz.model.QuizSession;
import quiz.dao.AnalyticsDAO; // Added import
import quiz.dao.AchievementDAO; // Added import

import java.util.List;
import java.util.Random; // Added import

public class QuizService {
    private QuestionDAO questionDAO;
    private ScoreDAO scoreDAO;
    private QuizSessionDAO sessionDAO;
    private AnalyticsDAO analyticsDAO; // Added
    private AchievementDAO achievementDAO; // Added

    public QuizService() {
        this.questionDAO = new QuestionDAO();
        this.scoreDAO = new ScoreDAO();
        this.sessionDAO = new QuizSessionDAO();
        this.analyticsDAO = new AnalyticsDAO(); // Initialize
        this.achievementDAO = new AchievementDAO(); // Initialize
    }

    // pentru quiz personalizat bazat pe performanta utilizatorului
    public List<Question> startAdaptiveQuiz(int userId, int questionCount) throws DatabaseException {
        // Presupunem că QuestionDAO are o metodă getAdaptiveQuestions(userId, questionCount)
        // care apelează funcția PostgreSQL corespunzătoare.
        List<Question> adaptiveQuestions = questionDAO.getAdaptiveQuestions(userId, questionCount);

        System.out.println("Adaptive questions for user " + userId + ":");
        for (Question q : adaptiveQuestions) {
            System.out.println("Dificulty: " + q.getDifficultyLevel() + " - " + q.getText());
        }

        return adaptiveQuestions;
    }

    // verificare ca merge tot
    public void playCompleteQuiz(int userId, String quizType) {
        try {
            // 1. Verifică nivelul utilizatorului
            String currentLevel = analyticsDAO.getPerformanceLevel(userId);
            System.out.println("Nivelul tău: " + currentLevel);

            // 2. Selectează întrebări adaptate (sau random/categorie pentru test)
            List<Question> questions;
            if ("adaptive".equalsIgnoreCase(quizType)) {
                questions = questionDAO.getAdaptiveQuestions(userId, 10);
            } else if ("random".equalsIgnoreCase(quizType)) {
                questions = questionDAO.getRandomQuestions(10);
            } else {
                // For category quiz, you'd need a category ID, let's pick a random one for simulation
                List<quiz.model.Category> categories = new quiz.dao.CategoryDAO().getAllCategories();
                if (!categories.isEmpty()) {
                    int categoryId = categories.get(new Random().nextInt(categories.size())).getId();
                    questions = questionDAO.getQuestionsByCategory(categoryId, 10);
                } else {
                    questions = questionDAO.getRandomQuestions(10); // Fallback
                }
            }


            // 3. Creează sesiunea
            QuizSession session = new QuizSession(userId, questions.size(), quizType);
            int sessionId = sessionDAO.createQuizSession(session);
            System.out.println("Sesiune quiz nouă creată cu ID: " + sessionId);

            // 4. Simulează răspunsurile (în realitate, vin de la UI)
            playQuestions(userId, sessionId, questions);

            // 5. Finalizează sesiunea
            // Calculate final score and time for simulation
            int simulatedCorrectAnswers = 0;
            int simulatedTimeTaken = 0; // in milliseconds
            for (Question q : questions) {
                // Simulate random correct/incorrect answers and time
                if (Math.random() > 0.5) { // 50% chance of correct answer
                    simulatedCorrectAnswers++;
                }
                simulatedTimeTaken += (new Random().nextInt(30) + 10) * 1000; // 10-40 seconds per question
            }
            sessionDAO.completeQuizSession(sessionId, simulatedCorrectAnswers, simulatedCorrectAnswers, simulatedTimeTaken);
            System.out.println("Sesiune quiz finalizată. Răspunsuri corecte: " + simulatedCorrectAnswers + ", Timp total: " + simulatedTimeTaken + "ms");


            // 6. Afișează realizările noi (acordate automat de trigger)
            showNewAchievements(userId);

        } catch (DatabaseException e) {
            System.err.println("Eroare în timpul simulării quiz-ului: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void playQuestions(int userId, int sessionId, List<Question> questions) throws DatabaseException {
        Random random = new Random();
        for (Question q : questions) {
            char selectedOption = 'A'; // Simulate selecting option A
            boolean isCorrect = (selectedOption == q.getCorrectOption());
            int timeTaken = random.nextInt(20000) + 5000; // Simulate 5-25 seconds per question

            scoreDAO.saveScore(userId, sessionId, q.getId(), selectedOption, isCorrect, timeTaken);
            System.out.println("  Răspuns salvat pentru întrebarea " + q.getId() + ": " + (isCorrect ? "Corect" : "Greșit"));
        }
    }

    private void showNewAchievements(int userId) {
        try {
            // Aici, triggerul din DB ar trebui să fi acordat deja realizările.
            // Putem prelua realizările utilizatorului pentru a le afișa.
            List<quiz.model.Achievement> userAchievements = achievementDAO.getUserAchievements(userId);
            System.out.println("\nRealizările tale:");
            if (userAchievements.isEmpty()) {
                System.out.println("  Nici o realizare încă.");
            } else {
                for (quiz.model.Achievement ach : userAchievements) {
                    System.out.println("  - " + ach.getAchievementName() + " (" + ach.getDescription() + ") la " + ach.getAchievedAt());
                }
            }
        } catch (DatabaseException e) {
            System.err.println("Eroare la preluarea realizărilor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}