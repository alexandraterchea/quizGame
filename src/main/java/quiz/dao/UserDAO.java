package quiz.dao;

import quiz.model.User;
import quiz.exceptions.DatabaseException;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class UserDAO {

    public User findByUsername(String username) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null; // Nu a fost găsit niciun user cu acest username
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by username", e);
        }
    }

    public User findByEmail(String email) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null; // Nu a fost găsit niciun user cu acest email
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email", e);
        }
    }

    public User login(String username, String password) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null; // Autentificare eșuată
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error during login", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRegistration_date(rs.getTimestamp("registration_date").toLocalDateTime());
        user.setTotalScore(rs.getInt("total_score"));
        user.setGamesPlayed(rs.getInt("games_played"));
        user.setBestStreak(rs.getInt("best_streak"));
        user.setCurrentLevel(rs.getInt("current_level")); 
        return user;
    }

    public boolean updateUserProfile(User user) throws DatabaseException {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user profile", e);
        }
    }
    
	public boolean save(User user) throws DatabaseException {
        String sql = "INSERT INTO users (username, password, email, registration_date, totalScore, gamesPlayed, bestStreak, currentLevel) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
             
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getRegistration_date()));
            stmt.setInt(5, user.getTotalScore());
            stmt.setInt(6, user.getGamesPlayed());
            stmt.setInt(7, user.getBestStreak());
            stmt.setInt(8, user.getCurrentLevel());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error saving user", e);
        }
    }
    // Alte metode existente în UserDAO...
}