package quiz.dao;

import util.DBUtil;
import quiz.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//creeaza un DAO pentru Analytics care va calcula performanta utilizatorilor
public class AnalyticsDAO {
    public double getUserPerformance(int userId) throws DatabaseException {
        String sql = "SELECT calculate_user_performance(?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating user performance: " + e.getMessage(), e);
        }
        return 0.0;
    }

    public String getPerformanceLevel(int userId) throws DatabaseException {
        double performance = getUserPerformance(userId);

        if (performance >= 0.8) return "EXPERT";
        else if (performance >= 0.6) return "ADVANCED";
        else if (performance >= 0.4) return "INTERMEDIATE";
        else return "BEGINNER";
    }
}