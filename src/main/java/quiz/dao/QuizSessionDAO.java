package quiz.dao;

import quiz.model.QuizSession;
import util.DBUtil;
import quiz.exceptions.DatabaseException;

import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class QuizSessionDAO {
    public int createQuizSession(QuizSession session) throws DatabaseException {
        String sql = "INSERT INTO quiz_sessions (user_id, total_questions, quiz_type, started_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        ps.setInt(1, session.getUserId());
        ps.setInt(2, session.getTotalQuestions());
        ps.setString(3, session.getQuizType());
        ps.executeUpdate();
        
        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DatabaseException("Creating quiz session failed, no ID obtained.");
            }
        }
    } catch (SQLException e) {
        throw new DatabaseException("Error creating quiz session: " + e.getMessage(), e);
    }
}

    // adaugat parametru timeTaken
    public void completeQuizSession(int sessionId, int correctAnswers, int finalScore, int timeTaken) throws DatabaseException {
        String sql = "UPDATE quiz_sessions SET completed_at = CURRENT_TIMESTAMP, correct_answers = ?, final_score = ?, time_taken = ? WHERE id = ?";

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
                
                Timestamp startedAt = rs.getTimestamp("started_at");
                if (startedAt != null) {
                    session.setStartedAt(startedAt.toLocalDateTime());
                }

                Timestamp completedAt = rs.getTimestamp("completed_at");
                if (completedAt != null) {
                    session.setCompletedAt(completedAt.toLocalDateTime());
                }
                
                session.setTotalQuestions(rs.getInt("total_questions"));
                session.setCorrectAnswers(rs.getInt("correct_answers"));
                session.setFinalScore(rs.getInt("final_score"));
                session.setTimeTaken(rs.getInt("time_taken"));
                
                if (session.getTimeTaken() == 0 && session.getStartedAt() != null && session.getCompletedAt() != null) {
                    long timeTaken = Duration.between(session.getStartedAt(), session.getCompletedAt()).toMillis();
                    session.setTimeTaken((int) timeTaken);
                }
                
                try {
                    session.setQuizType(rs.getString("quiz_type"));
                } catch (SQLException e) {
                    session.setQuizType("STANDARD");
                }
                
                sessions.add(session);
            }
        }
    } catch (SQLException e) {
        throw new DatabaseException("Error fetching quiz sessions for user " + userId, e);
    }

    return sessions;
}
}