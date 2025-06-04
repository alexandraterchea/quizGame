package quiz.dao;

import quiz.model.QuizSession;
import util.DBUtil;
import util.DatabaseException;

import java.sql.*;
import java.time.LocalDateTime;

public class QuizSessionDAO {
    public int createQuizSession(QuizSession session) throws DatabaseException {
        String sql = "INSERT INTO quiz_sessions (user_id, total_questions, session_type) VALUES (?, ?, ?) RETURNING id"; // Adăugat session_type

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, session.getUserId());
            ps.setInt(2, session.getTotalQuestions());
            ps.setString(3, session.getSessionType()); // Setează session_type

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating quiz session: " + e.getMessage(), e);
        }

        return -1;
    }

    // Adăugat parametru timeTaken
    public void completeQuizSession(int sessionId, int correctAnswers, int finalScore, int timeTaken) throws DatabaseException {
        String sql = "UPDATE quiz_sessions SET completed_at = CURRENT_TIMESTAMP, correct_answers = ?, final_score = ?, time_taken = ? WHERE id = ?"; // Adăugat time_taken

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, correctAnswers);
            ps.setInt(2, finalScore);
            ps.setInt(3, timeTaken); // Setează time_taken
            ps.setInt(4, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error completing quiz session: " + e.getMessage(), e);
        }
    }
}