package quiz.dao;

import quiz.model.User;
import util.DBUtil;
import util.DatabaseException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean register(User user) throws DatabaseException {
        String sql = "INSERT INTO users(username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail()); // Adăugat email
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Este important să prindeți "duplicate key" specific (error code)
            if (e.getSQLState().equals("23505")) { // Cod de eroare pentru încălcarea constrângerii UNIQUE (duplicate key)
                throw new DatabaseException("Registration failed: Username already exists.", e);
            }
            throw new DatabaseException("Error registering user: " + e.getMessage(), e);
        }
    }

    public User login(String username, String password) throws DatabaseException {
        String sql = "SELECT id, username, email, registration_date, total_score, games_played, best_streak, current_level FROM users WHERE username = ? AND password = ?"; // Preluăm toate câmpurile relevante
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email")); // Setăm email-ul
                    user.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
                    user.setTotalScore(rs.getInt("total_score"));
                    user.setGamesPlayed(rs.getInt("games_played"));
                    user.setBestStreak(rs.getInt("best_streak"));
                    user.setCurrentLevel(rs.getInt("current_level"));
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error logging in: " + e.getMessage(), e);
        }
        return null; // Returnează null dacă autentificarea eșuează
    }

    public boolean userExists(String username) throws DatabaseException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking if user exists: " + e.getMessage(), e);
        }
    }

    public List<User> getLeaderboard(int limit) throws DatabaseException {
        // Asigură-te că funcția din BD este exact "get_leaderboard"
        String sql = "SELECT username, total_score, games_played, best_streak FROM users ORDER BY total_score DESC LIMIT ?"; // S-a folosit direct tabelul users, deoarece funcția get_leaderboard nu a fost furnizată în scriptul BD, dar e referită în UserDAO. Am presupus o implementare simplă.
        // Daca exista functia get_leaderboard(INTEGER) in DB, atunci linia corecta ar fi:
        // String sql = "SELECT username, total_score, games_played, best_streak FROM get_leaderboard(?)";

        List<User> leaderboard = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setTotalScore(rs.getInt("total_score"));
                    user.setGamesPlayed(rs.getInt("games_played"));
                    user.setBestStreak(rs.getInt("best_streak"));
                    leaderboard.add(user);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting leaderboard: " + e.getMessage(), e);
        }
        return leaderboard;
    }
}