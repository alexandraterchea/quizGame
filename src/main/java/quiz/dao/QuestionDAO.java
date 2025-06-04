package quiz.dao;

import quiz.model.Question;
import util.DBUtil;
import util.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    public List<Question> getRandomQuestions(int count) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, category_id, text, option_a, option_b, option_c, option_d, correct_option, difficulty_level FROM questions ORDER BY RANDOM() LIMIT ?";
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, count);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getInt("id"));
                    q.setCategoryId(rs.getInt("category_id"));
                    q.setText(rs.getString("text"));
                    q.setOptionA(rs.getString("option_a"));
                    q.setOptionB(rs.getString("option_b"));
                    q.setOptionC(rs.getString("option_c"));
                    q.setOptionD(rs.getString("option_d"));
                    q.setCorrectOption(rs.getString("correct_option").charAt(0));
                    q.setDifficultyLevel(rs.getInt("difficulty_level"));
                    questions.add(q);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error getting random questions: " + e.getMessage(), e);
        }
        return questions;
    }

    // Această metodă ar trebui să utilizeze funcția get_adaptive_questions
    public List<Question> getAdaptiveQuestions(int userId, int count) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        // Asigură-te că numele funcției din BD este exact "get_adaptive_questions"
        String sql = "SELECT question_id, question_text, opt_a, opt_b, opt_c, opt_d, correct_opt, difficulty FROM get_adaptive_questions(?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, count);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getInt("question_id"));
                    q.setText(rs.getString("question_text"));
                    q.setOptionA(rs.getString("opt_a"));
                    q.setOptionB(rs.getString("opt_b"));
                    q.setOptionC(rs.getString("opt_c"));
                    q.setOptionD(rs.getString("opt_d"));
                    q.setCorrectOption(rs.getString("correct_opt").charAt(0));
                    q.setDifficultyLevel(rs.getInt("difficulty")); // Numele coloanei din funcția PL/pgSQL
                    // q.setCategoryName(rs.getString("category_name")); // Funcția din BD nu returnează category_name
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting adaptive questions: " + e.getMessage(), e);
        }

        return questions;
    }
}