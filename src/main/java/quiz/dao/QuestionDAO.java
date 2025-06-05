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
    public int addQuestion(Question question) throws DatabaseException {
        String sql = "INSERT INTO questions (category_id, text, option_a, option_b, option_c, option_d, correct_option, difficulty_level) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, question.getCategoryId());
            ps.setString(2, question.getText());
            ps.setString(3, question.getOptionA());
            ps.setString(4, question.getOptionB());
            ps.setString(5, question.getOptionC());
            ps.setString(6, question.getOptionD());
            ps.setString(7, String.valueOf(question.getCorrectOption()));
            ps.setInt(8, question.getDifficultyLevel());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt("id");
                    System.out.println("✅ Question saved to database with ID: " + generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Failed to get generated question ID");
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error adding question to database: " + e.getMessage(), e);
        }
    }


    public List<Question> getQuestionsByDifficulty(int difficulty) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT question_id, question_text, opt_a, opt_b, opt_c, opt_d, correct_opt, difficulty, category_id FROM get_questions_by_difficulty(?)";
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
                    q.setCategoryName(rs.getString("category_name"));
                    q.setDifficultyLevel(rs.getInt("difficulty"));
                    q.setCategoryId(rs.getInt("category_id"));
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting questions by difficulty: " + e.getMessage(), e);
        }

        return questions;
    }

    //intrebari pe categorie
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