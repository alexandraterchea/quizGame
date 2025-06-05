package quiz.controller;

import quiz.dao.AchievementDAO;
import quiz.exceptions.DatabaseException;

public class AdminController {
    public void manuallyAwardAchievements(int userId) {
        AchievementDAO achievementDAO = new AchievementDAO();
        try {
            int newAchievements = achievementDAO.checkAndAwardAchievements(userId);
            System.out.println("Awarding " + newAchievements + " new achievements!"); // Display message on console
        } catch (DatabaseException e) {
            System.err.println("Error awarding achievements: " + e.getMessage());
            e.printStackTrace();
        }
    }
}