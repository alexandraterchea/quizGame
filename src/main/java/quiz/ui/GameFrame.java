// quiz/ui/GameFrame.java - FINAL IMPROVED VERSION with CardLayout for all views
package quiz.ui;

import quiz.dao.QuestionDAO;
import quiz.dao.QuizSessionDAO;
import quiz.dao.ScoreDAO;
import quiz.dao.CategoryDAO;
import quiz.model.Category;
import quiz.model.Question;
import quiz.model.QuizSession;
import quiz.model.User;
import quiz.exceptions.DatabaseException;
import quiz.dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GameFrame extends JFrame {
    private User currentUser;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int quizSessionId;
    private long questionStartTime;

    // UI Components for various panels
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Welcome Panel components
    private JPanel welcomePanel;
    private JLabel welcomeLabel;
    private JButton startQuizFlowButton;
    private JButton profileButton;
    private JButton viewStatsButton; // New stats button

    // Category Selection Panel components
    private JPanel categoryPanel;
    private JComboBox<Category> categoryComboBox;
    private JButton startCategoryQuizButton;
    private JButton backToWelcomeButtonCategory;

    // Quiz Panel components
    private JPanel quizPanel;
    private JLabel questionLabel;
    private JRadioButton optionA, optionB, optionC, optionD;
    private ButtonGroup optionsGroup;
    private JButton nextButton;
    private JLabel sessionTimerLabel;
    private Timer sessionTimer;
    private int totalSessionTimeElapsed = 0;
    private JLabel questionTimerLabel;
    private Timer questionTimer;
    private int timeLeftForQuestion;

    // Profile Panel components (now managed within CardLayout)
    private ProfileFrame profilePanel; // Instance of ProfileFrame

    private UserDAO userDAO = new UserDAO();

    // Color scheme
    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color SECONDARY_COLOR = new Color(60, 160, 60);
    private final Color ACCENT_COLOR = new Color(255, 140, 0);
    private final Color ERROR_COLOR = new Color(220, 53, 69);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    // Exit Button
    private JButton exitButton;

    public GameFrame(User user) {
        this.currentUser = user;
        setupFrame();
        initializeComponents();
        showWelcomePanel();
    }

    private void setupFrame() {
        setTitle("QuizMaster - " + currentUser.getUsername());
        // setSize(900, 600); // Removed fixed size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen (before maximizing)
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Set to fullscreen
        setResizable(true);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BACKGROUND_COLOR);
        add(cardPanel);
    }

    private void initializeComponents() {
        createExitButton(); // Initialize exit button once

        createWelcomePanel();
        createCategoryPanel();
        createQuizPanel();
        createProfileView(); // Initialize the ProfileFrame as a panel

        cardPanel.add(welcomePanel, "Welcome");
        cardPanel.add(categoryPanel, "CategorySelection");
        cardPanel.add(quizPanel, "Quiz");
        cardPanel.add(profilePanel, "Profile"); // Add profilePanel as a card
    }

    private JButton createExitButton() {
        exitButton = createStyledButton("Exit", ERROR_COLOR);
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit the application?",
                    "Exit Application",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        return exitButton;
    }

    private JPanel createCardBase(String title, String description, Color color) { // Removed icon parameter
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Add subtle shadow effect
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                card.getBorder()
        ));

        // Removed iconLabel
        // JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        // iconLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        // iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(color);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // card.add(iconLabel); // Removed iconLabel
        // card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(descLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        return card;
    }

    private void createWelcomePanel() {
        welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(BACKGROUND_COLOR);
        welcomePanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("QuizMaster", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(PRIMARY_COLOR);

        welcomeLabel = new JLabel("Welcome back, " + currentUser.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        welcomeLabel.setForeground(new Color(100, 100, 100));
        welcomeLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(welcomeLabel, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        welcomePanel.add(headerPanel, BorderLayout.NORTH);

        // Center section with cards
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        // cardsPanel.setPreferredSize(new Dimension(700, 200)); // Removed fixed preferred size

        // Start Quiz Card
        JPanel startQuizCard = createCardBase(
                "Start Quiz",
                "Begin a new quiz challenge",
                SECONDARY_COLOR
        );
        startQuizFlowButton = createStyledButton("Start Quiz", SECONDARY_COLOR);
        startQuizFlowButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startQuizFlowButton.addActionListener(e -> showCategorySelectionPanel());
        startQuizCard.add(startQuizFlowButton);

        // Profile Card
        JPanel profileCard = createCardBase(
                "Profile",
                "View and edit your profile",
                PRIMARY_COLOR
        );
        profileButton = createStyledButton("Profile", PRIMARY_COLOR);
        profileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileButton.addActionListener(e -> showProfilePanel());
        profileCard.add(profileButton);

        // Statistics Card
        JPanel statsCard = createCardBase(
                "Statistics",
                "View your quiz performance",
                ACCENT_COLOR
        );
        viewStatsButton = createStyledButton("Statistics", ACCENT_COLOR);
        viewStatsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewStatsButton.addActionListener(e -> showProfilePanel());
        statsCard.add(viewStatsButton);

        cardsPanel.add(startQuizCard);
        cardsPanel.add(profileCard);
        cardsPanel.add(statsCard);

        centerPanel.add(cardsPanel);
        welcomePanel.add(centerPanel, BorderLayout.CENTER);

        // Footer section with Exit button
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0)); // Add some padding

        JLabel footerLabel = new JLabel("Ready to test your knowledge?", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        footerLabel.setForeground(new Color(120, 120, 120));
        footerPanel.add(footerLabel, BorderLayout.CENTER);

        JPanel exitButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align to right
        exitButtonWrapper.setOpaque(false);
        exitButtonWrapper.add(exitButton);
        footerPanel.add(exitButtonWrapper, BorderLayout.EAST);

        welcomePanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void createCategoryPanel() {
        categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBackground(BACKGROUND_COLOR);
        categoryPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Choose Your Quiz Category", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel subtitleLabel = new JLabel("Select a category to begin your quiz challenge", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        subtitleLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        categoryPanel.add(headerPanel, BorderLayout.NORTH);

        // Center section with category selection
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.setBackground(CARD_COLOR);
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(30, 30, 30, 30)
        ));
        // selectionPanel.setPreferredSize(new Dimension(400, 250)); // Removed fixed preferred size

        JLabel categoryLabel = new JLabel("Category:", SwingConstants.LEFT);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        categoryLabel.setForeground(new Color(80, 80, 80));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        categoryComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        categoryComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryComboBox.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Custom renderer for better appearance
        categoryComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(8, 12, 8, 12));
                if (isSelected) {
                    setBackground(PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });

        startCategoryQuizButton = createStyledButton("Start Quiz", SECONDARY_COLOR);
        startCategoryQuizButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startCategoryQuizButton.addActionListener(e -> startCategoryQuiz());

        backToWelcomeButtonCategory = createStyledButton("Back", new Color(108, 117, 125));
        backToWelcomeButtonCategory.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToWelcomeButtonCategory.addActionListener(e -> showWelcomePanel());

        selectionPanel.add(categoryLabel);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        selectionPanel.add(categoryComboBox);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        selectionPanel.add(startCategoryQuizButton);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        selectionPanel.add(backToWelcomeButtonCategory);

        centerPanel.add(selectionPanel);
        categoryPanel.add(centerPanel, BorderLayout.CENTER);

        // Add Exit button to the bottom of the category panel
        JPanel categoryFooterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        categoryFooterPanel.setOpaque(false);
        categoryFooterPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        categoryFooterPanel.add(exitButton);
        categoryPanel.add(categoryFooterPanel, BorderLayout.SOUTH);

        loadCategories();
    }

    private void createQuizPanel() {
        quizPanel = new JPanel(new BorderLayout(15, 15));
        quizPanel.setBackground(BACKGROUND_COLOR);
        quizPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Panel for Timers and Exit Button
        JPanel topControlPanel = new JPanel(new BorderLayout());
        topControlPanel.setOpaque(false);
        topControlPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timerPanel.setOpaque(false);
        sessionTimerLabel = new JLabel("Session Time: 0:00", SwingConstants.LEFT);
        sessionTimerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        sessionTimerLabel.setForeground(PRIMARY_COLOR);
        timerPanel.add(sessionTimerLabel);

        questionTimerLabel = new JLabel("Question Time: 0:30", SwingConstants.RIGHT);
        questionTimerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionTimerLabel.setForeground(ACCENT_COLOR);
        timerPanel.add(questionTimerLabel);

        topControlPanel.add(timerPanel, BorderLayout.WEST);

        JPanel exitButtonQuizWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exitButtonQuizWrapper.setOpaque(false);
        exitButtonQuizWrapper.add(exitButton);
        topControlPanel.add(exitButtonQuizWrapper, BorderLayout.EAST);

        quizPanel.add(topControlPanel, BorderLayout.NORTH);


        // Center Panel for Question and Options
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBackground(CARD_COLOR);
        questionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(25, 25, 25, 25)
        ));

        // Question text
        questionLabel = new JLabel("Question will appear here", SwingConstants.LEFT);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        questionLabel.setForeground(Color.BLACK); // Set question color to black
        questionLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
        questionPanel.add(questionLabel, BorderLayout.NORTH);

        // Options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);

        optionsGroup = new ButtonGroup();
        Font optionFont = new Font("Arial", Font.PLAIN, 16);

        optionA = createStyledRadioButton("A. Option A", optionFont);
        optionB = createStyledRadioButton("B. Option B", optionFont);
        optionC = createStyledRadioButton("C. Option C", optionFont);
        optionD = createStyledRadioButton("D. Option D", optionFont);

        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        optionsPanel.add(optionA);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        optionsPanel.add(optionB);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        optionsPanel.add(optionC);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        optionsPanel.add(optionD);

        questionPanel.add(optionsPanel, BorderLayout.CENTER);
        quizPanel.add(questionPanel, BorderLayout.CENTER);

        // Bottom Panel for Navigation
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel progressLabel = new JLabel("Question 1 of 10", SwingConstants.LEFT);
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        progressLabel.setForeground(new Color(120, 120, 120));

        nextButton = createStyledButton("Next Question", PRIMARY_COLOR);
        nextButton.addActionListener(e -> processAnswer());

        bottomPanel.add(progressLabel, BorderLayout.WEST);
        bottomPanel.add(nextButton, BorderLayout.EAST);
        quizPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JRadioButton createStyledRadioButton(String text, Font font) {
        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setFont(font);
        radioButton.setOpaque(false);
        radioButton.setForeground(new Color(60, 60, 60));
        radioButton.setBorder(new EmptyBorder(8, 5, 8, 5));
        radioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        radioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!radioButton.isSelected()) {
                    radioButton.setForeground(PRIMARY_COLOR);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!radioButton.isSelected()) {
                    radioButton.setForeground(new Color(60, 60, 60));
                }
            }
        });

        return radioButton;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
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

    public void showWelcomePanel() {
        // Update welcome label in case username was changed in profile
        welcomeLabel.setText("Welcome back, " + currentUser.getUsername() + "!");
        cardLayout.show(cardPanel, "Welcome");
    }

    private void showCategorySelectionPanel() {
        cardLayout.show(cardPanel, "CategorySelection");
    }

    private void showQuizPanel() {
        cardLayout.show(cardPanel, "Quiz");
        startQuizSession();
    }

    // New method to create and manage the ProfileFrame as a card
    private void createProfileView() {
        profilePanel = new ProfileFrame(currentUser, userDAO, this, exitButton); // Pass exitButton
        // Add the back to game button listener directly within ProfileFrame,
        // which now calls GameFrame.showWelcomePanel()
    }

    private void showProfilePanel() {
        // When showing the profile panel, refresh its data
        profilePanel.loadUserData(); // Ensure it loads the latest user data
        profilePanel.loadStatistics(); // Ensure it loads the latest statistics
        cardLayout.show(cardPanel, "Profile");
    }

    private void loadCategories() {
        CategoryDAO categoryDAO = new CategoryDAO();
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            categoryComboBox.removeAllItems();
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
            if (categories.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No quiz categories found in the database.",
                        "No Categories",
                        JOptionPane.INFORMATION_MESSAGE);
                startCategoryQuizButton.setEnabled(false);
            } else {
                startCategoryQuizButton.setEnabled(true);
            }
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading categories: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            startCategoryQuizButton.setEnabled(false);
        }
    }

    private void startCategoryQuiz() {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        if (selectedCategory != null) {
            try {
                QuestionDAO questionDAO = new QuestionDAO();
                int numberOfQuestions = 10;
                questions = questionDAO.getQuestionsByCategory(selectedCategory.getId(), numberOfQuestions);

                if (questions.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "No questions found for the selected category (" + selectedCategory.getName() +
                                    "). Please choose another category.",
                            "No Questions",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                currentQuestionIndex = 0;
                score = 0;
                totalSessionTimeElapsed = 0;
                showQuizPanel();
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading questions for category: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a category before starting the quiz.",
                    "No Category Selected",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void startQuizSession() {
        QuizSessionDAO quizSessionDAO = new QuizSessionDAO();
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        String sessionType = (selectedCategory != null) ? selectedCategory.getName() : "General Quiz"; // Use category name
        QuizSession newSession = new QuizSession(currentUser.getId(), questions.size(), sessionType);

        try {
            quizSessionId = quizSessionDAO.createQuizSession(newSession);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error creating quiz session: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            endQuiz();
            return;
        }

        // Start session timer
        sessionTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                totalSessionTimeElapsed++;
                int minutes = totalSessionTimeElapsed / 60;
                int seconds = totalSessionTimeElapsed % 60;
                sessionTimerLabel.setText(String.format("Session Time: %d:%02d", minutes, seconds));
            }
        });
        sessionTimer.start();

        showQuestion();
    }

    private void showQuestion() {
        if (questionTimer != null) {
            questionTimer.stop();
        }
        optionsGroup.clearSelection();

        Question q = questions.get(currentQuestionIndex);
        questionLabel.setText("<html><p style='line-height:1.4;'>" + q.getText() + "</p></html>");
        optionA.setText("A. " + q.getOptionA());
        optionB.setText("B. " + q.getOptionB());
        optionC.setText("C. " + q.getOptionC());
        optionD.setText("D. " + q.getOptionD());

        // Update progress
        // These lines are outside the scope of the original error but are good practice to ensure component access is safe.
        // If these lines also cause issues, a similar refactoring to ensure direct references would be ideal.
        // Since quizPanel.getComponent(2) might return different components based on layout changes,
        // it's safer to directly access a JLabel if it's a field, or find it by name/iterate if needed.
        // For now, assuming progressLabel is a direct child of bottomPanel (which is part of quizPanel)
        // If progressLabel was a field, this could be simplified.
        JPanel bottomPanel = (JPanel) quizPanel.getComponent(2); // Assuming it's still at index 2
        // It's better to declare progressLabel as a field in GameFrame to avoid this casting.
        // For this example, I'm just adapting to the existing structure.
        JLabel progressLabel = (JLabel) bottomPanel.getComponent(0); // Assuming it's the first component in bottomPanel
        progressLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());


        // Update next button text
        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setText("Finish Quiz");
        } else {
            nextButton.setText("Next Question");
        }

        questionStartTime = System.currentTimeMillis();
        timeLeftForQuestion = 30;
        updateQuestionTimer();
        startQuestionTimer();
    }

    private void startQuestionTimer() {
        if (questionTimer != null) {
            questionTimer.stop();
        }
        questionTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeftForQuestion--;
                updateQuestionTimer();

                if (timeLeftForQuestion <= 0) {
                    questionTimer.stop();
                    JOptionPane.showMessageDialog(GameFrame.this,
                            "Time's up for this question!",
                            "Time Up",
                            JOptionPane.WARNING_MESSAGE);
                    processAnswer();
                }
            }
        });
        questionTimer.start();
    }

    private void updateQuestionTimer() {
        int minutes = timeLeftForQuestion / 60;
        int seconds = timeLeftForQuestion % 60;
        questionTimerLabel.setText(String.format("Question Time: %d:%02d", minutes, seconds));

        // Change color as time runs out
        if (timeLeftForQuestion <= 10) {
            questionTimerLabel.setForeground(ERROR_COLOR);
        } else {
            questionTimerLabel.setForeground(ACCENT_COLOR);
        }
    }

    private void processAnswer() {
        if (questionTimer != null) {
            questionTimer.stop();
        }

        char selectedOption = ' ';
        if (optionA.isSelected()) selectedOption = 'A';
        else if (optionB.isSelected()) selectedOption = 'B';
        else if (optionC.isSelected()) selectedOption = 'C';
        else if (optionD.isSelected()) selectedOption = 'D';

        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = (selectedOption == currentQuestion.getCorrectOption());
        if (isCorrect) {
            score++;
        }

        long timeTakenForQuestion = (System.currentTimeMillis() - questionStartTime) / 1000;

        ScoreDAO scoreDAO = new ScoreDAO();
        try {
            scoreDAO.saveScore(currentUser.getId(), quizSessionId, currentQuestion.getId(),
                    selectedOption, isCorrect, (int) timeTakenForQuestion);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving question score: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            showQuestion();
        } else {
            endQuiz();
        }
    }

    private void endQuiz() {
        if (sessionTimer != null) {
            sessionTimer.stop();
        }
        if (questionTimer != null) {
            questionTimer.stop();
        }

        QuizSessionDAO quizSessionDAO = new QuizSessionDAO();
        try {
            quizSessionDAO.completeQuizSession(quizSessionId, score, score * 10, totalSessionTimeElapsed);

            // Create a nice results dialog
            showQuizResultsDialog();

            showWelcomePanel();
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error completing quiz session: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            showWelcomePanel();
        }
    }

    private void showQuizResultsDialog() {
        JDialog resultsDialog = new JDialog(this, "Quiz Results", true);
        resultsDialog.setSize(400, 300);
        resultsDialog.setLocationRelativeTo(this);
        resultsDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        contentPanel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel("Quiz Complete!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(SECONDARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        double percentage = (double) score / questions.size() * 100;
        JLabel scoreLabel = new JLabel(String.format("Score: %d/%d (%.1f%%)", score, questions.size(), percentage), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel.setForeground(PRIMARY_COLOR);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        int minutes = totalSessionTimeElapsed / 60;
        int seconds = totalSessionTimeElapsed % 60;
        JLabel timeLabel = new JLabel(String.format("Time: %d:%02d", minutes, seconds), SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timeLabel.setForeground(new Color(100, 100, 100));
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
        okButton.addActionListener(e -> resultsDialog.dispose());

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
}