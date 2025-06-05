package quiz.ui;

import quiz.dao.CategoryDAO;
import quiz.dao.QuestionDAO;
import quiz.dao.QuizSessionDAO;
import quiz.dao.ScoreDAO;
import quiz.dao.UserDAO;
import quiz.model.Category;
import quiz.model.Question;
import quiz.model.User;
import quiz.exceptions.DatabaseException;
import quiz.controller.QuizController;
import quiz.controller.DashboardController;
import quiz.dao.AchievementDAO;
import quiz.dao.AnalyticsDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GameFrame extends JFrame {
    private User currentUser;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int quizSessionId;
    private long questionStartTime;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Welcome Panel components
    private JPanel welcomePanel;
    private JLabel welcomeLabel;
    private JButton startQuizFlowButton;
    private JButton profileButton;
    private JButton viewStatsButton;
    private JButton exitButton;

    private JPanel categoryPanel;
    private JComboBox<Category> categoryComboBox;
    private JButton startCategoryQuizButton;
    private JButton backToWelcomeButton;

    private JPanel quizPanel;
    private JLabel questionLabel;
    private JRadioButton optionA, optionB, optionC, optionD;
    private ButtonGroup optionsGroup;
    private JButton submitButton;
    private JLabel timerLabel;
    private Timer quizTimer;
    private int timeElapsed;

    private QuizController quizController;
    private DashboardController dashboardController;

    private QuestionDAO questionDAO;
    private QuizSessionDAO quizSessionDAO;
    private ScoreDAO scoreDAO;
    private CategoryDAO categoryDAO;
    private UserDAO userDAO;
    private AchievementDAO achievementDAO;
    private AnalyticsDAO analyticsDAO;

    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // SteelBlue
    private static final Color SECONDARY_COLOR = new Color(60, 179, 113); // MediumAquamarine
    private static final Color ACCENT_COLOR = new Color(255, 140, 0); // DarkOrange
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255); // AliceBlue
    private static final Color CARD_COLOR = Color.WHITE; // White for content cards
    private static final Color TEXT_COLOR = new Color(50, 50, 50);

    public GameFrame(User user) {
        this.currentUser = user;
        this.questionDAO = new QuestionDAO();
        this.quizSessionDAO = new QuizSessionDAO();
        this.scoreDAO = new ScoreDAO();
        this.categoryDAO = new CategoryDAO();
        this.userDAO = new UserDAO();
        this.achievementDAO = new AchievementDAO();
        this.analyticsDAO = new AnalyticsDAO();

        this.quizController = new QuizController(currentUser, questionDAO, quizSessionDAO, scoreDAO, achievementDAO, analyticsDAO, this); // Pass GameFrame reference

        setupFrame();
        initComponents();
        setupWelcomePanel();
        setupCategoryPanel();
        setupQuizPanel();

        cardPanel.add(welcomePanel, "Welcome");
        cardPanel.add(categoryPanel, "Category");
        cardPanel.add(quizPanel, "Quiz");

        add(cardPanel);
        showWelcomePanel();
    }

    private void setupFrame() {
        setTitle("QuizMaster - " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BACKGROUND_COLOR);
    }

    private void initComponents() {
        exitButton = createStyledButton("Exit", PRIMARY_COLOR);
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit QuizMaster?", "Exit Application",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    private void setupWelcomePanel() {
        welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        welcomePanel.setBackground(CARD_COLOR);
        welcomePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setForeground(PRIMARY_COLOR);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        startQuizFlowButton = createStyledButton("Start New Quiz", SECONDARY_COLOR);
        startQuizFlowButton.addActionListener(e -> cardLayout.show(cardPanel, "Category")); // Go to category selection

        profileButton = createStyledButton("View Profile", PRIMARY_COLOR);
        profileButton.addActionListener(e -> showProfilePanel()); // Show profile panel

        viewStatsButton = createStyledButton("View Dashboard", PRIMARY_COLOR);
        viewStatsButton.addActionListener(e -> showDashboardPanel()); // Show dashboard panel

        welcomePanel.add(Box.createVerticalGlue());
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        welcomePanel.add(startQuizFlowButton);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(profileButton);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(viewStatsButton);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(exitButton);
        welcomePanel.add(Box.createVerticalGlue());
    }

    private void setupCategoryPanel() {
        categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        categoryPanel.setBackground(CARD_COLOR);

        JLabel categoryLabel = new JLabel("Choose a Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 24));
        categoryLabel.setForeground(PRIMARY_COLOR);
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        categoryComboBox.setMaximumSize(new Dimension(300, 40));
        categoryComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadCategories(); // Populate categories

        startCategoryQuizButton = createStyledButton("Start Quiz in Category", SECONDARY_COLOR);
        startCategoryQuizButton.addActionListener(e -> startCategoryQuiz());

        JButton startRandomQuizButton = createStyledButton("Start Random Quiz", SECONDARY_COLOR);
        startRandomQuizButton.addActionListener(e -> startRandomQuiz());

        JButton startAIQuizButton = createStyledButton("Start AI Random Quiz", ACCENT_COLOR);
        startAIQuizButton.addActionListener(e -> startAIRandomQuiz());

        backToWelcomeButton = createStyledButton("Back to Welcome", PRIMARY_COLOR);
        backToWelcomeButton.addActionListener(e -> showWelcomePanel());

        categoryPanel.add(Box.createVerticalGlue());
        categoryPanel.add(categoryLabel);
        categoryPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        categoryPanel.add(categoryComboBox);
        categoryPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        categoryPanel.add(startCategoryQuizButton);
        categoryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        categoryPanel.add(startRandomQuizButton);
        categoryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        categoryPanel.add(startAIQuizButton);
        categoryPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        categoryPanel.add(backToWelcomeButton);
        categoryPanel.add(Box.createVerticalGlue());
    }

    private void setupQuizPanel() {
        quizPanel = new JPanel(new BorderLayout(20, 20));
        quizPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        quizPanel.setBackground(CARD_COLOR);

        questionLabel = new JLabel("Question text goes here.", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setForeground(TEXT_COLOR);
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(CARD_COLOR);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        optionsGroup = new ButtonGroup();
        optionA = createOptionRadioButton("A) Option A");
        optionB = createOptionRadioButton("B) Option B");
        optionC = createOptionRadioButton("C) Option C");
        optionD = createOptionRadioButton("D) Option D");

        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        optionsPanel.add(optionA);
        optionsPanel.add(optionB);
        optionsPanel.add(optionC);
        optionsPanel.add(optionD);

        submitButton = createStyledButton("Submit Answer", SECONDARY_COLOR);
        submitButton.addActionListener(e -> handleAnswerSubmission());
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerLabel = new JLabel("Time: 00:00", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timerLabel.setForeground(ACCENT_COLOR);

        quizPanel.add(questionLabel, BorderLayout.NORTH);
        quizPanel.add(optionsPanel, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(CARD_COLOR);
        southPanel.add(submitButton, BorderLayout.CENTER);
        southPanel.add(timerLabel, BorderLayout.EAST);
        quizPanel.add(southPanel, BorderLayout.SOUTH);
    }

    private JRadioButton createOptionRadioButton(String text) {
        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setFont(new Font("Arial", Font.PLAIN, 16));
        radioButton.setBackground(CARD_COLOR);
        radioButton.setForeground(TEXT_COLOR);
        radioButton.setFocusPainted(false);
        radioButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return radioButton;
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
        button.setMaximumSize(new Dimension(300, 50)); // Fixed size for consistency
        return button;
    }

    public void showWelcomePanel() {
        cardLayout.show(cardPanel, "Welcome");
        updateWelcomeLabel();
    }

    public void showQuizPanel() {
        cardLayout.show(cardPanel, "Quiz");
        startQuizTimer();
    }

    public void showProfilePanel() {
        ProfileFrame profileFrame = new ProfileFrame(currentUser, userDAO, this, exitButton);
        cardPanel.add(profileFrame, "Profile");
        cardLayout.show(cardPanel, "Profile");
        profileFrame.loadUserProfileData();
    }

    public void showDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BorderLayout());
        dashboardPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        dashboardPanel.setBackground(CARD_COLOR);

        JLabel dashboardTitle = new JLabel("User Dashboard", SwingConstants.CENTER);
        dashboardTitle.setFont(new Font("Arial", Font.BOLD, 28));
        dashboardTitle.setForeground(PRIMARY_COLOR);
        dashboardPanel.add(dashboardTitle, BorderLayout.NORTH);

        JLabel levelLabel = new JLabel("Loading user level...", SwingConstants.CENTER);
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        levelLabel.setForeground(TEXT_COLOR);
        dashboardPanel.add(levelLabel, BorderLayout.CENTER);

        JButton backButton = createStyledButton("Back to Welcome", PRIMARY_COLOR);
        backButton.addActionListener(e -> showWelcomePanel());
        JPanel southPanel = new JPanel();
        southPanel.setBackground(CARD_COLOR);
        southPanel.add(backButton);
        dashboardPanel.add(southPanel, BorderLayout.SOUTH);

        cardPanel.add(dashboardPanel, "Dashboard");
        cardLayout.show(cardPanel, "Dashboard");

        // Initialize and use DashboardController
        this.dashboardController = new DashboardController(analyticsDAO, currentUser, levelLabel);
        dashboardController.updateDashboard();
    }


    private void startRandomQuiz() {
        try {
            quizController.startQuiz(currentUser.getId(), "random", 10, -1); // 10 questions, no category
            this.questions = quizController.getQuestions();
            this.quizSessionId = quizController.getQuizSessionId();
            this.currentQuestionIndex = 0;
            this.score = 0;
            loadQuestion();
            showQuizPanel();
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error starting random quiz: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startCategoryQuiz() {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.", "No Category Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            quizController.startQuiz(currentUser.getId(), "category", 10, selectedCategory.getId());
            this.questions = quizController.getQuestions();
            this.quizSessionId = quizController.getQuizSessionId();
            this.currentQuestionIndex = 0;
            this.score = 0;
            loadQuestion();
            showQuizPanel();
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error starting category quiz: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question currentQuestion = questions.get(currentQuestionIndex);
            questionLabel.setText("<html><body style='text-align: center;'>" + currentQuestion.getText() + "</body></html>"); // Center align question text
            optionA.setText("A) " + currentQuestion.getOptionA());
            optionB.setText("B) " + currentQuestion.getOptionB());
            optionC.setText("C) " + currentQuestion.getOptionC());
            optionD.setText("D) " + currentQuestion.getOptionD());
            optionsGroup.clearSelection();
            questionStartTime = System.currentTimeMillis();
        } else {
            finishQuiz();
        }
    }

    private void handleAnswerSubmission() {
        char selectedOption = ' ';
        if (optionA.isSelected()) selectedOption = 'A';
        else if (optionB.isSelected()) selectedOption = 'B';
        else if (optionC.isSelected()) selectedOption = 'C';
        else if (optionD.isSelected()) selectedOption = 'D';

        if (selectedOption == ' ') {
            JOptionPane.showMessageDialog(this, "Please select an answer.", "No Answer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long timeTaken = System.currentTimeMillis() - questionStartTime;
        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = (selectedOption == currentQuestion.getCorrectOption());

        if (isCorrect) {
            score++;
        }

        try {
            quizController.handleAnswerSubmission(currentUser.getId(), quizSessionId, currentQuestion.getId(), selectedOption, isCorrect, (int) timeTaken);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error saving score: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            loadQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        stopQuizTimer();
        int totalQuestions = questions.size();
        double percentage = (double) score / totalQuestions * 100;
        int totalTimeTaken = timeElapsed; // Use the total time from the timer

        try {
            quizController.finishQuiz(quizSessionId, score, totalTimeTaken);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error completing quiz session: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        showQuizResultsDialog(score, totalQuestions, totalTimeTaken, percentage);
        showNewAchievements();
        updateDashboardFromGameFrame();
    }

    private void showQuizResultsDialog(int score, int totalQuestions, int totalTimeMillis, double percentage) {
        JDialog resultsDialog = new JDialog(this, "Quiz Results", true);
        resultsDialog.setLayout(new BorderLayout());
        resultsDialog.setSize(400, 300);
        resultsDialog.setLocationRelativeTo(this);
        resultsDialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel("Quiz Completed!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel(String.format("You scored: %d / %d (%.1f%%)", score, totalQuestions, percentage), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        scoreLabel.setForeground(TEXT_COLOR);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTimeMillis) - TimeUnit.MINUTES.toSeconds(minutes);
        JLabel timeLabel = new JLabel(String.format("Total Time: %02d:%02d", minutes, seconds), SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timeLabel.setForeground(ACCENT_COLOR);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String performanceMessage;
        if (percentage >= 90) {
            performanceMessage = "Excellent work!";
        } else if (percentage >= 70) {
            performanceMessage = "Good job!";
        } else if (percentage >= 50) {
            performanceMessage = "Not bad!";
        } else {
            performanceMessage = "Keep studying!";
        }

        JLabel performanceLabel = new JLabel(performanceMessage, SwingConstants.CENTER);
        performanceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        performanceLabel.setForeground(ACCENT_COLOR);
        performanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = createStyledButton("OK", PRIMARY_COLOR);
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> {
            resultsDialog.dispose();
            showWelcomePanel();
        });

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(scoreLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(timeLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(performanceLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(okButton);

        resultsDialog.add(contentPanel, BorderLayout.CENTER);
        resultsDialog.setVisible(true);
    }

    private void showNewAchievements() {
        try {
            List<quiz.model.Achievement> newAchievements = achievementDAO.getUserAchievements(currentUser.getId());
            if (!newAchievements.isEmpty()) {
                StringBuilder sb = new StringBuilder("Congratulations! You've earned new achievements:\n");
                for (quiz.model.Achievement ach : newAchievements) {
                    sb.append("- ").append(ach.getAchievementName()).append(": ").append(ach.getDescription()).append("\n");
                }
                JOptionPane.showMessageDialog(this, sb.toString(), "New Achievements!", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error fetching achievements: " + e.getMessage(), "Achievement Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateWelcomeLabel() {
        welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
    }

    private void updateDashboardFromGameFrame() {
        System.out.println("Dashboard data needs to be refreshed.");
    }


    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            categoryComboBox.removeAllItems(); // Clear existing items
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startQuizTimer() {
        timeElapsed = 0;
        if (quizTimer != null && quizTimer.isRunning()) {
            quizTimer.stop();
        }
        quizTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeElapsed++;
                long minutes = timeElapsed / 60;
                long seconds = timeElapsed % 60;
                timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
            }
        });
        quizTimer.start();
    }

    private void stopQuizTimer() {
        if (quizTimer != null && quizTimer.isRunning()) {
            quizTimer.stop();
        }
    }

    private void startAIRandomQuiz() {
        try {
            quizController.startQuiz(currentUser.getId(), "ai_random", 10, -1);
            this.questions = quizController.getQuestions();
            this.quizSessionId = quizController.getQuizSessionId();
            this.currentQuestionIndex = 0;
            this.score = 0;
            loadQuestion();
            showQuizPanel();
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error starting AI quiz: " + ex.getMessage(), "AI Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}