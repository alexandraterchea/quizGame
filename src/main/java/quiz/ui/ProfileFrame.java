// quiz/ui/ProfilePanel.java - FINAL IMPROVED VERSION (as a JPanel to be used in CardLayout)
package quiz.ui;

import quiz.dao.UserDAO;
import quiz.dao.QuizSessionDAO;
import quiz.model.User;
import quiz.model.QuizSession;

import quiz.controller.ProfileController; // Import ProfileController
import quiz.dao.AnalyticsDAO; // For AnalyticsDAO in ProfileController

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder; // Import EmptyBorder
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ProfileFrame extends JPanel {

    private User currentUser;
    private UserDAO userDAO;
    private final GameFrame parentFrame; // Reference to GameFrame for navigation
    private JButton exitButton; // Reference to the shared exit button

    // Profile editing components
    private JTextField usernameField;
    private JTextField emailField;
    private JButton saveProfileButton;
    private JButton changePasswordButton; // Placeholder for future password change

    // Statistics components
    private JLabel totalQuizzesLabel;
    private JLabel averageScoreLabel;
    private JLabel bestScoreLabel;
    private JLabel totalTimeLabel;
    private JTable recentQuizzesTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;

    // Navigation
    private JButton backToGameButton;

    // Controller
    private ProfileController profileController;

    // UI Colors
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // SteelBlue
    private static final Color SECONDARY_COLOR = new Color(60, 179, 113); // MediumAquamarine
    private static final Color ACCENT_COLOR = new Color(255, 140, 0); // DarkOrange
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255); // AliceBlue
    private static final Color CARD_COLOR = Color.WHITE; // White for content cards
    private static final Color TEXT_COLOR = new Color(50, 50, 50);


    // Constructor now ensures parentFrame and exitButton are always passed
    public ProfileFrame(User user, UserDAO userDAO, GameFrame parentFrame, JButton exitButton) {
        this.currentUser = user;
        this.userDAO = userDAO;
        this.parentFrame = parentFrame; // Assign the parentFrame
        this.exitButton = exitButton; // Assign the shared exit button

        // Initialize ProfileController
        // AnalyticsDAO and QuizSessionDAO are needed for statistics
        this.profileController = new ProfileController(
                user,
                new AnalyticsDAO(), // You might want to pass these as arguments if they are singletons
                new QuizSessionDAO(),
                new UserDAO(), // UserDAO is also needed for updating profile in controller
                this // Pass reference to ProfileFrame itself to update UI
        );


        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        setBackground(BACKGROUND_COLOR);

        // --- Profile Editing Section ---
        JPanel profileEditPanel = new JPanel(new GridBagLayout());
        profileEditPanel.setBackground(CARD_COLOR);
        profileEditPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "User Profile",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                PRIMARY_COLOR
        ));
        profileEditPanel.setMaximumSize(new Dimension(600, 200)); // Limit size
        profileEditPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField = new JTextField(currentUser.getUsername());
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailField = new JTextField(currentUser.getEmail());
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));

        saveProfileButton = createStyledButton("Save Profile", SECONDARY_COLOR);
        changePasswordButton = createStyledButton("Change Password", PRIMARY_COLOR); // Placeholder

        gbc.gridx = 0;
        gbc.gridy = 0;
        profileEditPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        profileEditPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        profileEditPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        profileEditPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 8, 5, 8);
        profileEditPanel.add(saveProfileButton, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 8, 8, 8);
        profileEditPanel.add(changePasswordButton, gbc); // Add change password button


        // --- Statistics Section ---
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(CARD_COLOR);
        statsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "Quiz Statistics",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                PRIMARY_COLOR
        ));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        totalQuizzesLabel = createStatLabel("Total Quizzes: 0");
        averageScoreLabel = createStatLabel("Average Score: 0%");
        bestScoreLabel = createStatLabel("Best Score: 0%");
        totalTimeLabel = createStatLabel("Total Time Spent: 00:00:00");

        statsPanel.add(totalQuizzesLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(averageScoreLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(bestScoreLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(totalTimeLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));


        // --- Recent Quizzes Table ---
        String[] columnNames = {"Date", "Category", "Score", "Time", "Result"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells uneditable
            }
        };
        recentQuizzesTable = new JTable(tableModel);
        recentQuizzesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        recentQuizzesTable.setRowHeight(25);
        recentQuizzesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        recentQuizzesTable.setBackground(CARD_COLOR);
        recentQuizzesTable.setFillsViewportHeight(true);

        tableScrollPane = new JScrollPane(recentQuizzesTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "Recent Quizzes",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                PRIMARY_COLOR
        ));
        tableScrollPane.setPreferredSize(new Dimension(500, 200)); // Set preferred size
        tableScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);


        // --- Navigation ---
        backToGameButton = createStyledButton("Back to Quiz", PRIMARY_COLOR);
        backToGameButton.addActionListener(e -> parentFrame.showWelcomePanel());


        // Layout
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.add(profileEditPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topPanel.add(statsPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topPanel.add(tableScrollPane);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(backToGameButton);
        buttonPanel.add(exitButton); // Add shared exit button

        add(topPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setupEventListeners();
        loadUserProfileData(); // Load data when panel is constructed
    }

    private void setupEventListeners() {
        saveProfileButton.addActionListener(e -> saveProfile());
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(300, 50));
        return button;
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(2, 0, 2, 0)); // Small vertical padding
        return label;
    }


    public void loadUserProfileData() {
        profileController.loadProfileData(currentUser.getId());
    }

    public void updateProfileStats(int totalQuizzes, double averageScore, int bestScore, long totalTimeMillis) {
        totalQuizzesLabel.setText("Total Quizzes: " + totalQuizzes);
        averageScoreLabel.setText(String.format("Average Score: %.1f%%", averageScore));
        bestScoreLabel.setText("Best Score: " + bestScore + "%");

        long hours = TimeUnit.MILLISECONDS.toHours(totalTimeMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTimeMillis) % 60;
        totalTimeLabel.setText(String.format("Total Time Spent: %02d:%02d:%02d", hours, minutes, seconds));
    }

    public void updateRecentQuizzesTable(List<QuizSession> sessions) {
        tableModel.setRowCount(0); // Clear existing data
        for (QuizSession session : sessions) {
            String date = session.getStartedAt().toLocalDate().toString();
            String category = "N/A"; // You might need to fetch category name if session stores category_id
            if (session.getCategoryId() != -1) { // Assuming -1 means no specific category
                try {
                    // This is a place where we might need a DAO call or more data in QuizSession model
                    // For simplicity, let's assume getSessionType includes category info or we add a getCategoryName
                    category = session.getSessionType(); // Using session type as a placeholder
                } catch (Exception e) {
                    category = "Error";
                }
            }
            int score = session.getCorrectAnswers();
            int totalQuestions = session.getTotalQuestions();
            String scoreText = score + "/" + totalQuestions;
            double percentage = (totalQuestions > 0) ? ((double) score / totalQuestions * 100) : 0;
            String result = String.format("%.1f%%", percentage);

            long minutes = TimeUnit.MILLISECONDS.toMinutes(session.getTimeTaken());
            long seconds = TimeUnit.MILLISECONDS.toSeconds(session.getTimeTaken()) - TimeUnit.MINUTES.toSeconds(minutes);
            String timeText = String.format("%02d:%02d", minutes, seconds);

            tableModel.addRow(new Object[]{date, category, scoreText, timeText, result});
        }
    }

    private void saveProfile() {
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();

        profileController.updateUserProfile(currentUser.getId(), newUsername, newEmail);
    }

    public void showProfileUpdateSuccess(String newUsername) {
        JOptionPane.showMessageDialog(this,
                "Profile updated successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        // Refresh the welcome label in GameFrame if it's passed as a reference
        if (parentFrame != null) {
            parentFrame.setTitle("QuizMaster - " + newUsername);
            parentFrame.updateWelcomeLabel(); // Refresh welcome label
        }
    }

    public void showProfileUpdateError(String message) {
        JOptionPane.showMessageDialog(this,
                "Profile update failed: " + message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}