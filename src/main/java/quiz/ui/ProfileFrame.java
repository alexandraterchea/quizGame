// quiz/ui/ProfilePanel.java - FINAL IMPROVED VERSION (as a JPanel to be used in CardLayout)
package quiz.ui;

import quiz.dao.UserDAO;
import quiz.dao.QuizSessionDAO;
import quiz.dao.ScoreDAO;
import quiz.model.User;
import quiz.model.QuizSession;
import quiz.exceptions.DatabaseException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder; // Import EmptyBorder
import java.awt.*;
import java.util.List;


public class ProfileFrame extends JPanel {

    private User currentUser;
    private UserDAO userDAO;
    private GameFrame parentFrame; // Reference to GameFrame for navigation
    private JButton exitButton; // Reference to the shared exit button

    // Profile editing components
    private JTextField usernameField;
    private JTextField emailField;
    private JButton saveProfileButton;

    // Statistics components
    private JLabel totalQuizzesLabel;
    private JLabel averageScoreLabel;
    private JLabel bestScoreLabel;
    private JLabel totalTimeLabel;
    private JTable recentQuizzesTable;
    private JScrollPane tableScrollPane;

    // Navigation
    private JButton backToGameButton;

    // Constructor now ensures parentFrame and exitButton are always passed
    public ProfileFrame(User user, UserDAO userDAO, GameFrame parentFrame, JButton exitButton) {
        this.currentUser = user;
        this.userDAO = userDAO;
        this.parentFrame = parentFrame; // Assign the parentFrame
        this.exitButton = exitButton; // Assign the shared exit button

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 250));

        initializeComponents();
        // Data loading methods are called from GameFrame.showProfilePanel() now
        // so that data is refreshed every time the panel is shown.
    }

    private void initializeComponents() {
        // Header Panel with Exit Button
        JPanel headerAndExitPanel = new JPanel(new BorderLayout());
        headerAndExitPanel.setOpaque(false);
        headerAndExitPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel headerPanel = createHeaderPanel(); // Your existing header
        headerAndExitPanel.add(headerPanel, BorderLayout.CENTER);

        JPanel exitButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exitButtonWrapper.setOpaque(false);
        exitButtonWrapper.add(exitButton);
        headerAndExitPanel.add(exitButtonWrapper, BorderLayout.EAST);

        add(headerAndExitPanel, BorderLayout.NORTH);

        // Main content with profile and stats
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setOpaque(false);

        // Profile edit panel
        JPanel profilePanel = createProfileEditPanel();
        mainPanel.add(profilePanel);

        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        mainPanel.add(statsPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Bottom navigation panel
        JPanel navigationPanel = createNavigationPanel();
        add(navigationPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        // headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); // Removed, now part of headerAndExitPanel

        JLabel titleLabel = new JLabel("User Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 100));

        JLabel welcomeLabel = new JLabel("Welcome back, " + currentUser.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setForeground(new Color(100, 100, 100));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(welcomeLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createProfileEditPanel() {
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(15, 15, 15, 15),
                        "Profile Information",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16),
                        new Color(50, 50, 100)
                )
        ));

        // Username field
        JPanel usernamePanel = createFieldPanel("Username:", currentUser.getUsername());
        usernameField = (JTextField) usernamePanel.getComponent(1);
        profilePanel.add(usernamePanel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Email field
        JPanel emailPanel = createFieldPanel("Email:", currentUser.getEmail());
        emailField = (JTextField) emailPanel.getComponent(1);
        profilePanel.add(emailPanel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        saveProfileButton = createStyledButton("Save Changes", new Color(70, 130, 180), Color.WHITE);
        saveProfileButton.addActionListener(e -> saveProfile());

        buttonPanel.add(saveProfileButton);

        profilePanel.add(buttonPanel);
        profilePanel.add(Box.createVerticalGlue());

        return profilePanel;
    }

    private JPanel createFieldPanel(String labelText, String fieldValue) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));

        JTextField field = new JTextField(fieldValue);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(0, 35));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(15, 15, 15, 15),
                        "Quiz Statistics",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16),
                        new Color(50, 50, 100)
                )
        ));

        // Statistics summary
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 15, 10));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        totalQuizzesLabel = createStatLabel("Total Quizzes:", "0");
        averageScoreLabel = createStatLabel("Average Score:", "0%");
        bestScoreLabel = createStatLabel("Best Score:", "0%");
        totalTimeLabel = createStatLabel("Total Time:", "0 min");

        summaryPanel.add(totalQuizzesLabel);
        summaryPanel.add(averageScoreLabel);
        summaryPanel.add(bestScoreLabel);
        summaryPanel.add(totalTimeLabel);

        statsPanel.add(summaryPanel, BorderLayout.NORTH);

        // Recent quizzes table
        JLabel recentLabel = new JLabel("Recent Quiz Results:");
        recentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        recentLabel.setForeground(new Color(80, 80, 80));
        recentLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        statsPanel.add(recentLabel, BorderLayout.CENTER); // Will be replaced by JScrollPane later

        recentQuizzesTable = new JTable(new Object[][]{}, new String[]{"Date", "Category", "Score", "Time"});
        recentQuizzesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        recentQuizzesTable.setRowHeight(25);
        recentQuizzesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        recentQuizzesTable.setFillsViewportHeight(true);

        tableScrollPane = new JScrollPane(recentQuizzesTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        statsPanel.add(tableScrollPane, BorderLayout.SOUTH);

        return statsPanel;
    }

    private JLabel createStatLabel(String title, String value) {
        JLabel label = new JLabel("<html><body style='text-align:center;'><b>" + title + "</b><br>" + value + "</body></html>", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navigationPanel.setOpaque(false);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        backToGameButton = createStyledButton("Back to Game", new Color(70, 130, 180), Color.WHITE);
        // The action listener now simply tells the parent frame to show the welcome panel
        backToGameButton.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.showWelcomePanel();
            }
        });
        navigationPanel.add(backToGameButton);

        return navigationPanel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    public void loadUserData() {
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            // Update welcome label if it's part of ProfileFrame's header
            // (You might need a reference to that label if it's not directly in this class)
        }
    }

    public void loadStatistics() {
        if (currentUser == null) return;

        QuizSessionDAO quizSessionDAO = new QuizSessionDAO();
        try {
            List<QuizSession> sessions = quizSessionDAO.getUserQuizSessions(currentUser.getId());
            int totalQuizzes = sessions.size();
            double totalScore = 0;
            int totalTime = 0;
            int bestScore = 0;

            for (QuizSession session : sessions) {
                totalScore += session.getFinalScore();
                totalTime += session.getTimeTaken();
                if (session.getFinalScore() > bestScore) {
                    bestScore = session.getFinalScore();
                }
            }

            totalQuizzesLabel.setText(createStatLabel("Total Quizzes:", String.valueOf(totalQuizzes)).getText());
            if (totalQuizzes > 0) {
                averageScoreLabel.setText(createStatLabel("Average Score:", String.format("%.1f%%", (totalScore / (totalQuizzes * 10.0)) * 100)).getText()); // Assuming 10 points per question
                bestScoreLabel.setText(createStatLabel("Best Score:", String.format("%d points", bestScore)).getText());
            } else {
                averageScoreLabel.setText(createStatLabel("Average Score:", "N/A").getText());
                bestScoreLabel.setText(createStatLabel("Best Score:", "N/A").getText());
            }
            totalTimeLabel.setText(createStatLabel("Total Time:", String.format("%d min %02d sec", totalTime / 60, totalTime % 60)).getText());

            // Populate recent quizzes table
            String[] columnNames = {"Date", "Category", "Score", "Time"};
            Object[][] data = new Object[sessions.size()][4];
            for (int i = 0; i < sessions.size(); i++) {
                QuizSession session = sessions.get(i);
                data[i][0] = session.getCompletedAt() != null ? session.getCompletedAt().toLocalDate() : "Ongoing";
                data[i][1] = session.getSessionType(); // Assuming session type is category name
                data[i][2] = session.getFinalScore();
                data[i][3] = String.format("%d:%02d", session.getTimeTaken() / 60, session.getTimeTaken() % 60);
            }
            recentQuizzesTable.setModel(new JTable(data, columnNames).getModel());


        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading quiz statistics: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void saveProfile() {
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();

        if (newUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username cannot be empty.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newEmail.isEmpty() || !isValidEmail(newEmail)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);

        try {
            boolean updated = userDAO.updateUserProfile(currentUser);
            if (updated) {
                JOptionPane.showMessageDialog(this,
                        "Profile updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Update the welcome label in GameFrame if it's passed as a reference
                if (parentFrame != null) {
                    parentFrame.setTitle("QuizMaster - " + newUsername);
                    parentFrame.showWelcomePanel(); // Refresh welcome label
                }

            } else {
                JOptionPane.showMessageDialog(this,
                        "Profile update failed.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating profile: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}