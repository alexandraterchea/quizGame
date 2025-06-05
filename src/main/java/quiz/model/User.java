package quiz.model;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String password;
    private String email; // Adăugat câmpul email
    private LocalDateTime registration_date;
    private int totalScore;
    private int gamesPlayed;
    private int bestStreak;
    private int currentLevel;

    public User() {} // Constructor implicit

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        // Email-ul va fi implicit null sau gol, sau necesită un set explicit
    }

    // Noul constructor cu email
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getteri și Setteri existenți...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username){this.username=username;}

    public String getPassword() { return password; }
    public void setPassword(String password){this.password=password;}

    public String getEmail() { return email; } // Getter pentru email
    public void setEmail(String email) { this.email = email; } // Setter pentru email

    public LocalDateTime getRegistration_date() { return registration_date; }
    public void setRegistration_date(LocalDateTime registration_date) { this.registration_date = registration_date; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }

    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
}