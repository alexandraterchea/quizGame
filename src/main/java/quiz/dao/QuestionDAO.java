// quiz/dao/QuestionDAO.java - MODIFIED
package quiz.dao;

import quiz.model.Question;
import util.DBUtil;
import quiz.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    public List<Question> getRandomQuestions(int count) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        // Modified SQL to include category_id and difficulty_level in SELECT statement
        String sql = "SELECT id, category_id, text, option_a, option_b, option_c, option_d, correct_option, difficulty_level FROM questions ORDER BY RANDOM() LIMIT ?";
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, count);
            try( ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getInt("id"));
                    q.setCategoryId(rs.getInt("category_id")); // Set categoryId
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
            throw new DatabaseException("Error getting random questions: "+e.getMessage(),e);
        }
        return questions;
    }

    public List<Question> getQuestionsByDifficulty(int difficulty) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        // Assuming get_questions_by_difficulty also includes category_id and difficulty in its return structure
        String sql = "SELECT question_id, question_text, opt_a, opt_b, opt_c, opt_d, correct_opt, difficulty, category_id FROM get_questions_by_difficulty(?)"; // Added category_id and difficulty
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, difficulty);
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
                    q.setCategoryName(rs.getString("category_name")); // This might need adjustment if your function doesn't return it
                    q.setDifficultyLevel(rs.getInt("difficulty")); // Set difficultyLevel
                    q.setCategoryId(rs.getInt("category_id")); // Set categoryId
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting questions by difficulty: " + e.getMessage(), e);
        }

        return questions;
    }

    //functia care selecteaza intrebari adaptate la nivelul utilizatorului->pe baza performantei sale
    public List<Question> getAdaptiveQuestions(int userId, int count) throws DatabaseException {
        List<Question> questions = new ArrayList<>();

        String sql = "SELECT question_id, question_text, opt_a, opt_b, opt_c, opt_d, correct_opt, difficulty, category_id FROM get_adaptive_questions(?, ?)";

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
                    q.setDifficultyLevel(rs.getInt("difficulty"));
                    q.setCategoryId(rs.getInt("category_id"));
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting adaptive questions: " + e.getMessage(), e);
        }
        return questions;
    }

    // NEW METHOD: Get questions by category
    public List<Question> getQuestionsByCategory(int categoryId, int count) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, category_id, text, option_a, option_b, option_c, option_d, correct_option, difficulty_level FROM questions WHERE category_id = ? ORDER BY RANDOM() LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.setInt(2, count);
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
            throw new DatabaseException("Error getting questions by category: " + e.getMessage(), e);
        }
        return questions;
    }
}