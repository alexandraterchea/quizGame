package quiz.dao;

import util.DBUtil;
import quiz.exceptions.DatabaseException;

import java.sql.*;

public class ScoreDAO {
    // Adăugat parametru timeTaken
    public void saveScore(int userId, int sessionId, int questionId, char selectedOption, boolean isCorrect, int timeTaken) throws DatabaseException {
        String sql = "INSERT INTO scores (user_id, quiz_session_id, question_id, selected_option, is_correct, time_taken) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, sessionId);
            ps.setInt(3, questionId);
            ps.setString(4, String.valueOf(selectedOption));
            ps.setBoolean(5, isCorrect);
            ps.setInt(6, timeTaken); // Setează time_taken
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error saving score: " + e.getMessage(), e); // Mesaj de eroare mai descriptiv
        }
    }
}