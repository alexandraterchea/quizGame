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
                    return null;
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
        //verifica daca username-ul este deja folosit de altcineva
        String checkUsernameSql = "SELECT id FROM users WHERE username = ? AND id <> ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkUsernameSql)) {
            checkStmt.setString(1, user.getUsername());
            checkStmt.setInt(2, user.getId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    //username deja folosit de alt utilizator
                    return false;
                }
            }
        }

        //verifica daca email-ul este deja folosit de altcineva
        String checkEmailSql = "SELECT id FROM users WHERE email = ? AND id <> ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql)) {
            checkStmt.setString(1, user.getEmail());
            checkStmt.setInt(2, user.getId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    //email deja folosit de alt utilizator
                    return false;
                }
            }
        }

        //daca nu exista conflicte, actualizeaza profilul
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