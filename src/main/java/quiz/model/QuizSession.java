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

    // Constructors
    public QuizSession() {}

    public QuizSession(int userId, int totalQuestions, String sessionType) {
        this.userId = userId;
        this.totalQuestions = totalQuestions;
        this.sessionType = sessionType;
        this.startedAt = LocalDateTime.now();
    }

    public void startSession() {
        this.startedAt = LocalDateTime.now();
        this.completedAt = null;
        this.correctAnswers = 0;
        this.finalScore = 0;
        this.timeTaken = 0;
    }

    public void completeSession(int correctAnswers, int finalScore, int timeTaken) {
        this.correctAnswers = correctAnswers;
        this.finalScore = finalScore;
        this.timeTaken = timeTaken;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public int calculateScorePercentage() {
        if (totalQuestions == 0) return 0;
        return (int)((correctAnswers * 100.0) / totalQuestions);
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

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
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

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }


}