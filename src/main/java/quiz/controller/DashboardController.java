package quiz.controller;

import quiz.dao.AnalyticsDAO;
import quiz.exceptions.DatabaseException;
import quiz.model.User;

import javax.swing.*;

public class DashboardController {
    private AnalyticsDAO analyticsDAO;
    private User currentUser;
    private JLabel levelLabel;

    public DashboardController(AnalyticsDAO analyticsDAO, User currentUser, JLabel levelLabel) {
        this.analyticsDAO = analyticsDAO;
        this.currentUser = currentUser;
        this.levelLabel = levelLabel;
    }

    public void updateDashboard() {
        try {
            String userLevel = analyticsDAO.getPerformanceLevel(currentUser.getId());
            double performance = analyticsDAO.getUserPerformance(currentUser.getId());
            
            String displayText = String.format("<html><center>Your Level: %s<br>Performance: %.1f%%</center></html>", 
                                             userLevel, performance * 100);
            levelLabel.setText(displayText);
        } catch (DatabaseException e) {
            levelLabel.setText("Error loading dashboard data");
            System.err.println("Error updating dashboard: " + e.getMessage());
        }
    }
}