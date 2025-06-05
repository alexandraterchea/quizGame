package quiz.ui;

import quiz.dao.UserDAO;
import quiz.dao.QuizSessionDAO;
import quiz.model.User;
import quiz.model.QuizSession;

import quiz.controller.ProfileController;
import quiz.dao.AnalyticsDAO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import quiz.dao.CategoryDAO;
import quiz.model.Category;


public class ProfileFrame extends JPanel {

    private final User currentUser;
    private UserDAO userDAO;
    private final GameFrame parentFrame;
    private JButton exitButton;

    private final JTextField usernameField;
    private final JTextField emailField;
    private final JButton saveProfileButton;

    private final JLabel totalQuizzesLabel;
    private final JLabel averageScoreLabel;
    private final JLabel bestScoreLabel;
    private final JLabel totalTimeLabel;
    private JTable recentQuizzesTable;
    private final DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;

    private JButton backToGameButton;

    private final ProfileController profileController;

    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(60, 179, 113);
    private static final Color ACCENT_COLOR = new Color(255, 140, 0);
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(50, 50, 50);


    public ProfileFrame(User user, UserDAO userDAO, GameFrame parentFrame, JButton exitButton) {
        this.currentUser = user;
        this.userDAO = userDAO;
        this.parentFrame = parentFrame;
        this.exitButton = exitButton;

        this.profileController = new ProfileController(
                user,
                new AnalyticsDAO(),
                new QuizSessionDAO(),
                new UserDAO(),
                this
        );

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        setBackground(BACKGROUND_COLOR);

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
        profileEditPanel.setMaximumSize(new Dimension(600, 200));
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


        String[] columnNames = {"Date", "Category", "Score", "Time", "Result"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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
        tableScrollPane.setPreferredSize(new Dimension(500, 200));
        tableScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        backToGameButton = createStyledButton("Back to Quiz", PRIMARY_COLOR);
        backToGameButton.addActionListener(e -> parentFrame.showWelcomePanel());

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
        buttonPanel.add(exitButton);

        add(topPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setupEventListeners();
        loadUserProfileData();
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
        label.setBorder(new EmptyBorder(2, 0, 2, 0));
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


    // Adaugă metoda aici
    public String formatTime(long timeInMillis) {
        if (timeInMillis == 0) {
            return "N/A";
        }

        long seconds = timeInMillis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public void updateRecentQuizzesTable(List<QuizSession> sessions) {
        DefaultTableModel model = (DefaultTableModel) recentQuizzesTable.getModel();
        model.setRowCount(0);

        for (QuizSession session : sessions) {
            if (session.getStartedAt() != null) {
                String dateStr = session.getStartedAt().toLocalDate().toString();

                // Obține numele categoriei în funcție de tipul de quiz
                String categoryName = getCategoryDisplayName(session);

                double percentage = 0.0;
                if (session.getTotalQuestions() > 0) {
                    percentage = (double)session.getCorrectAnswers() / session.getTotalQuestions() * 100;
                }

                Object[] row = {
                        dateStr,
                        categoryName,
                        session.getCorrectAnswers() + "/" + session.getTotalQuestions(),
                        formatTime(session.getTimeTaken()),
                        String.format("%.1f%%", percentage)
                };
                model.addRow(row);
            }
        }
    }

    // Metodă helper pentru a obține numele categoriei
    private String getCategoryDisplayName(QuizSession session) {
        String quizType = session.getQuizType();

        // Verifică dacă quizType este null
        if (quizType == null) {
            return "Unknown";
        }

        switch (quizType) {
            case "ai_random":
                return "AI Generated";
            case "random":
                return "Random Questions";
            case "category":
                // Aici trebuie să obții numele real al categoriei din DB
                if (session.getCategoryId() > 0) {
                    try {
                        CategoryDAO categoryDAO = new CategoryDAO();
                        Category category = categoryDAO.getCategoryById(session.getCategoryId());
                        return (category != null) ? category.getName() : "Unknown Category";
                    } catch (Exception e) {
                        return "Category " + session.getCategoryId();
                    }
                }
                return "General Category";
            default:
                return quizType;
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
        if (parentFrame != null) {
            parentFrame.setTitle("QuizMaster - " + newUsername);
            parentFrame.updateWelcomeLabel();
        }
    }

    public void showProfileUpdateError(String message) {
        JOptionPane.showMessageDialog(this,
                "Profile update failed: " + message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}