package quiz.dao;

import quiz.model.Achievement;
import util.DBUtil;
import quiz.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AchievementDAO {

    public int checkAndAwardAchievements(int userId) throws DatabaseException {
        String sql = "SELECT award_achievements(?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int achievementsAwarded = rs.getInt(1);
                    System.out.println("Utilizatorul " + userId + " a primit " +
                            achievementsAwarded + " realizări noi!");
                    return achievementsAwarded;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error awarding achievements: " + e.getMessage(), e);
        }
        return 0;
    }

    public List<Achievement> getUserAchievements(int userId) throws DatabaseException {
        List<Achievement> achievements = new ArrayList<>();
        String sql = "SELECT * FROM user_achievements WHERE user_id = ? ORDER BY achieved_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Achievement achievement = new Achievement();
                    achievement.setId(rs.getInt("id"));
                    achievement.setUserId(rs.getInt("user_id"));
                    achievement.setAchievementType(rs.getString("achievement_type"));
                    achievement.setAchievementName(rs.getString("achievement_name"));
                    achievement.setDescription(rs.getString("description"));
                    achievement.setAchievedAt(rs.getTimestamp("achieved_at").toLocalDateTime());
                    achievement.setPointsAwarded(rs.getInt("points_awarded"));
                    achievements.add(achievement);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting user achievements: " + e.getMessage(), e);
        }
        return achievements;
    }
}