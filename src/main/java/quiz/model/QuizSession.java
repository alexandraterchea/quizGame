package quiz.model;
import java.time.LocalDateTime;

public class QuizSession {
    private int id;
    private int userId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int totalQuestions;
    private int correctAnswers;
    private int finalScore;
    private int timeTaken;
    private String sessionType;
    private int categoryId;
    private String quizType;

    public QuizSession() {}

    public QuizSession(int userId, int totalQuestions, String quizType) {
        this.userId = userId;
        this.totalQuestions = totalQuestions;
        this.quizType = quizType;
        this.correctAnswers = 0;
        this.finalScore = 0;
        this.timeTaken = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public String getQuizType() { return quizType; }
    public void setQuizType(String quizType) { this.quizType = quizType; }
    
    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public int getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(int timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getSessionType() {
        return sessionType;
    }

    public int getCategoryId() {
        return categoryId;
    }

}