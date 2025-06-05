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

    public User getUserById(int userId) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                } else {
                    return null; // Sau aruncă o excepție dacă userul nu există
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching user by ID: " + e.getMessage(), e);
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
    try (Connection conn = DBUtil.getConnection()) {
        // Verifică dacă username-ul este folosit de altcineva
        String checkUsernameSql = "SELECT id FROM users WHERE username = ? AND id <> ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkUsernameSql)) {
            checkStmt.setString(1, user.getUsername());
            checkStmt.setInt(2, user.getId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Username deja folosit de alt utilizator
                    return false;
                }
            }
        }

        // Verifică dacă email-ul este folosit de altcineva
        String checkEmailSql = "SELECT id FROM users WHERE email = ? AND id <> ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql)) {
            checkStmt.setString(1, user.getEmail());
            checkStmt.setInt(2, user.getId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Email deja folosit de alt utilizator
                    return false;
                }
            }
        }

        // Dacă nu există conflicte, fac updateul
        String updateSql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setString(1, user.getUsername());
            updateStmt.setString(2, user.getEmail());
            updateStmt.setInt(3, user.getId());
            int affectedRows = updateStmt.executeUpdate();
            return affectedRows > 0;
        }
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

    public boolean registerUser(User user) throws DatabaseException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error registering user: " + e.getMessage(), e);
        }
    }

}