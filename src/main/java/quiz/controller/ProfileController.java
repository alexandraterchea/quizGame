package quiz.controller;

import quiz.dao.AnalyticsDAO;
import quiz.dao.QuizSessionDAO;
import quiz.dao.UserDAO;
import quiz.exceptions.DatabaseException;
import quiz.model.User;
import quiz.model.QuizSession;
import quiz.ui.ProfileFrame;

import java.util.List;
import java.util.regex.Pattern;

public class ProfileController {
    private User currentUser;
    private AnalyticsDAO analyticsDAO;
    private QuizSessionDAO quizSessionDAO;
    private UserDAO userDAO;
    private ProfileFrame profileFrame;

    public ProfileController(User currentUser, AnalyticsDAO analyticsDAO, QuizSessionDAO quizSessionDAO, UserDAO userDAO, ProfileFrame profileFrame) {
        this.currentUser = currentUser;
        this.analyticsDAO = analyticsDAO;
        this.quizSessionDAO = quizSessionDAO;
        this.userDAO = userDAO;
        this.profileFrame = profileFrame;
    }

    //incarca datele profilului utilizatorului
    public void loadProfileData(int userId) {
        try {
            double performance = analyticsDAO.getUserPerformance(userId);
            String level = analyticsDAO.getPerformanceLevel(userId);

            List<QuizSession> sessions = quizSessionDAO.getUserQuizSessions(userId);

            int totalQuizzes = sessions.size();
            int totalCorrectAnswers = 0;
            int totalQuestionsAttempted = 0;
            int bestScore = 0; // In percentage
            long totalTimeMillis = 0;

            for (QuizSession session : sessions) {
                if (session.getCompletedAt() != null) {
                    totalCorrectAnswers += session.getCorrectAnswers();
                    totalQuestionsAttempted += session.getTotalQuestions();
                    totalTimeMillis += session.getTimeTaken();

                    if (session.getTotalQuestions() > 0) {
                        int sessionPercentage = (int) ((double) session.getCorrectAnswers() / session.getTotalQuestions() * 100);
                        if (sessionPercentage > bestScore) {
                            bestScore = sessionPercentage;
                        }
                    }
                }
            }

            double averageScore = (totalQuestionsAttempted > 0) ?
                    ((double) totalCorrectAnswers / totalQuestionsAttempted * 100) : 0.0;

            //actualizeaza ui
            profileFrame.updateProfileStats(totalQuizzes, averageScore, bestScore, totalTimeMillis);
            profileFrame.updateRecentQuizzesTable(sessions);

            System.out.printf("User %d performance: %.1f%% (%s)%n", userId, performance * 100, level);
            System.out.println("Total Quizzes: " + totalQuizzes);

        } catch (DatabaseException e) {
            System.err.println("Error loading user profile data: " + e.getMessage());
            e.printStackTrace();
            profileFrame.showProfileUpdateError("Failed to load profile data.");
        }
    }

    //actualizare info personale
    public void updateUserProfile(int userId, String newUsername, String newEmail) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            profileFrame.showProfileUpdateError("Username cannot be empty.");
            return;
        }
        if (newEmail == null || newEmail.trim().isEmpty() || !isValidEmail(newEmail)) {
            profileFrame.showProfileUpdateError("Please enter a valid email address.");
            return;
        }

        try {
            User userToUpdate = userDAO.getUserById(userId);
            if (userToUpdate == null) {
                profileFrame.showProfileUpdateError("User not found.");
                return;
            }

            userToUpdate.setUsername(newUsername);
            userToUpdate.setEmail(newEmail);

            boolean updated = userDAO.updateUserProfile(userToUpdate);
            if (updated) {
                currentUser.setUsername(newUsername);
                currentUser.setEmail(newEmail);
                profileFrame.showProfileUpdateSuccess(newUsername);
            } else {
                profileFrame.showProfileUpdateError("Profile update failed. Username or email already used.");
            }
        } catch (DatabaseException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            profileFrame.showProfileUpdateError("Database error during profile update: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}