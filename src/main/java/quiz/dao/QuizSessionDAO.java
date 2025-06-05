package quiz.dao;

import quiz.model.QuizSession;
import util.DBUtil;
import quiz.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    // adaugat parametru timeTaken
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
    
    public List<QuizSession> getUserQuizSessions(int userId) throws DatabaseException {
        List<QuizSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM quiz_sessions WHERE user_id = ? ORDER BY started_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuizSession session = new QuizSession();
                    session.setId(rs.getInt("id"));
                    session.setUserId(rs.getInt("user_id"));
                    session.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
                    
                    Timestamp completedAt = rs.getTimestamp("completed_at");
                    if (completedAt != null) {
                        session.setCompletedAt(completedAt.toLocalDateTime());
                    }
                    
                    session.setFinalScore(rs.getInt("final_score"));
                    session.setTimeTaken(rs.getInt("time_taken"));
                    
                    sessions.add(session);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching quiz sessions for user " + userId, e);
        }

        return sessions;
    }
}