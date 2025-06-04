// quiz/ui/GameFrame.java - MODIFIED
package quiz.ui;

import quiz.dao.QuestionDAO;
import quiz.dao.QuizSessionDAO;
import quiz.dao.ScoreDAO;
import quiz.dao.CategoryDAO; // Added import
import quiz.model.Category; // Added import
import quiz.model.Question;
import quiz.model.QuizSession;
import quiz.model.User;
import util.DatabaseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class GameFrame extends JFrame {
    private User currentUser;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int quizSessionId;
    private long questionStartTime;

    // UI Components for various panels
    private JPanel cardPanel; // Panel that uses CardLayout to switch views
    private CardLayout cardLayout;

    // Welcome Panel components
    private JPanel welcomePanel;
    private JLabel welcomeLabel;
    private JButton startQuizFlowButton; // Renamed to differentiate

    // Category Selection Panel components
    private JPanel categoryPanel;
    private JComboBox<Category> categoryComboBox;
    private JButton startCategoryQuizButton;
    private JButton backToWelcomeButton; // Added back button for categories

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
    private int timeLeftForQuestion; // Time limit for each question

    // Constructor modified to accept User object
    public GameFrame(User user) {
        this.currentUser = user;
        setTitle("Quiz Application");
        setSize(800, 500); // Increased size for better layout
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel); // Add the cardPanel to the JFrame

        // Initialize and add all panels
        createWelcomePanel();
        createCategoryPanel();
        createQuizPanel(); // This will be the main quiz logic panel

        cardPanel.add(welcomePanel, "Welcome");
        cardPanel.add(categoryPanel, "CategorySelection");
        cardPanel.add(quizPanel, "Quiz"); // Add the quiz panel

        showWelcomePanel(); // Start by showing the welcome panel
    }

    private void createWelcomePanel() {
        welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

        startQuizFlowButton = new JButton("Start Quiz");
        startQuizFlowButton.setFont(new Font("Arial", Font.BOLD, 20));
        startQuizFlowButton.setPreferredSize(new Dimension(200, 60)); // Make button larger
        JPanel buttonPanel = new JPanel(); // Use a panel to center the button
        buttonPanel.add(startQuizFlowButton);
        welcomePanel.add(buttonPanel, BorderLayout.SOUTH);

        startQuizFlowButton.addActionListener(e -> showCategorySelectionPanel());
    }

    private void createCategoryPanel() {
        categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Choose a Quiz Category:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        categoryPanel.add(titleLabel);
        categoryPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        categoryComboBox.setMaximumSize(new Dimension(300, 40));
        categoryComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        categoryPanel.add(categoryComboBox);
        categoryPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        startCategoryQuizButton = new JButton("Start Category Quiz");
        startCategoryQuizButton.setFont(new Font("Arial", Font.BOLD, 18));
        startCategoryQuizButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startCategoryQuizButton.addActionListener(e -> startCategoryQuiz());
        categoryPanel.add(startCategoryQuizButton);
        categoryPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        backToWelcomeButton = new JButton("Back to Welcome");
        backToWelcomeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backToWelcomeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToWelcomeButton.addActionListener(e -> showWelcomePanel());
        categoryPanel.add(backToWelcomeButton);

        loadCategories(); // Load categories when panel is created
    }

    private void createQuizPanel() {
        quizPanel = new JPanel(new BorderLayout(10, 10)); // Use BorderLayout for overall structure
        quizPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Panel for Timers
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
        sessionTimerLabel = new JLabel("Session Time: 0s");
        sessionTimerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionTimerLabel = new JLabel("Question Time: 30s");
        questionTimerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerPanel.add(sessionTimerLabel);
        timerPanel.add(questionTimerLabel);
        quizPanel.add(timerPanel, BorderLayout.NORTH);

        // Center Panel for Question and Options
        JPanel questionOptionsPanel = new JPanel();
        questionOptionsPanel.setLayout(new BoxLayout(questionOptionsPanel, BoxLayout.Y_AXIS));
        questionOptionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        questionLabel = new JLabel("Question Text", SwingConstants.LEFT);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionOptionsPanel.add(questionLabel);
        questionOptionsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        optionsGroup = new ButtonGroup();
        optionA = new JRadioButton("A. Option A");
        optionB = new JRadioButton("B. Option B");
        optionC = new JRadioButton("C. Option C");
        optionD = new JRadioButton("D. Option D");

        Font optionFont = new Font("Arial", Font.PLAIN, 16);
        optionA.setFont(optionFont);
        optionB.setFont(optionFont);
        optionC.setFont(optionFont);
        optionD.setFont(optionFont);

        optionA.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionB.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionC.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionD.setAlignmentX(Component.LEFT_ALIGNMENT);

        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        questionOptionsPanel.add(optionA);
        questionOptionsPanel.add(optionB);
        questionOptionsPanel.add(optionC);
        questionOptionsPanel.add(optionD);
        questionOptionsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        quizPanel.add(questionOptionsPanel, BorderLayout.CENTER);

        // Bottom Panel for Next Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nextButton = new JButton("Next Question");
        nextButton.setFont(new Font("Arial", Font.BOLD, 16));
        nextButton.addActionListener(e -> processAnswer());
        buttonPanel.add(nextButton);
        quizPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showWelcomePanel() {
        cardLayout.show(cardPanel, "Welcome");
    }

    private void showCategorySelectionPanel() {
        cardLayout.show(cardPanel, "CategorySelection");
    }

    private void showQuizPanel() {
        cardLayout.show(cardPanel, "Quiz");
        startQuizSession(); // Start the quiz session when quiz panel is shown
    }

    private void loadCategories() {
        CategoryDAO categoryDAO = new CategoryDAO();
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            categoryComboBox.removeAllItems(); // Clear existing items
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
            if (categories.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No quiz categories found in the database.", "No Categories", JOptionPane.INFORMATION_MESSAGE);
                startCategoryQuizButton.setEnabled(false);
            } else {
                startCategoryQuizButton.setEnabled(true);
            }
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            startCategoryQuizButton.setEnabled(false);
        }
    }

    private void startCategoryQuiz() {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        if (selectedCategory != null) {
            try {
                // Initialize questions for the selected category
                QuestionDAO questionDAO = new QuestionDAO();
                // We'll set a fixed number of questions for now, e.g., 10 questions per quiz
                int numberOfQuestions = 10;
                questions = questionDAO.getQuestionsByCategory(selectedCategory.getId(), numberOfQuestions);

                if (questions.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No questions found for the selected category (" + selectedCategory.getName() + "). Please choose another category.", "No Questions", JOptionPane.INFORMATION_MESSAGE);
                    return; // Stay on the category selection panel
                }

                currentQuestionIndex = 0;
                score = 0;
                totalSessionTimeElapsed = 0; // Reset session time for new quiz
                showQuizPanel(); // Switch to the quiz panel
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(this, "Error loading questions for category: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a category before starting the quiz.", "No Category Selected", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void startQuizSession() {
        // Create a new quiz session in the database
        QuizSessionDAO quizSessionDAO = new QuizSessionDAO();
        QuizSession newSession = new QuizSession(currentUser.getId(), questions.size(), "Category Quiz"); // "Category Quiz" as session type
        try {
            quizSessionId = quizSessionDAO.createQuizSession(newSession);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error creating quiz session: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            // Handle error, maybe return to main menu
            endQuiz(); // Consider what to do if session cannot be created
            return;
        }

        // Start session timer
        sessionTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                totalSessionTimeElapsed++;
                sessionTimerLabel.setText("Session Time: " + totalSessionTimeElapsed + "s");
            }
        });
        sessionTimer.start();

        showQuestion(); // Show the first question
    }

    private void showQuestion() {
        if (questionTimer != null) {
            questionTimer.stop(); // Stop previous question's timer
        }
        optionsGroup.clearSelection(); // Clear previous selection

        Question q = questions.get(currentQuestionIndex);
        questionLabel.setText("<html><p>" + q.getText() + "</p></html>"); // Use HTML for multi-line
        optionA.setText("A. " + q.getOptionA());
        optionB.setText("B. " + q.getOptionB());
        optionC.setText("C. " + q.getOptionC());
        optionD.setText("D. " + q.getOptionD());

        questionStartTime = System.currentTimeMillis(); // Record start time for the current question
        timeLeftForQuestion = 30; // Set time for each question (e.g., 30 seconds)
        questionTimerLabel.setText("Question Time: " + timeLeftForQuestion + "s");

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
                questionTimerLabel.setText("Question Time: " + timeLeftForQuestion + "s");
                if (timeLeftForQuestion <= 0) {
                    questionTimer.stop();
                    JOptionPane.showMessageDialog(GameFrame.this, "Time's up for this question! Auto-submitting.", "Time Up", JOptionPane.WARNING_MESSAGE);
                    processAnswer(); // Automatically process answer if time runs out
                }
            }
        });
        questionTimer.start();
    }

    private void processAnswer() {
        if (questionTimer != null) {
            questionTimer.stop(); // Stop the current question's timer
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

        // Calculate time taken for this question
        long timeTakenForQuestion = (System.currentTimeMillis() - questionStartTime) / 1000; // in seconds

        // Save score for this question
        ScoreDAO scoreDAO = new ScoreDAO();
        try {
            scoreDAO.saveScore(currentUser.getId(), quizSessionId, currentQuestion.getId(), selectedOption, isCorrect, (int) timeTakenForQuestion);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error saving question score: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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
            quizSessionDAO.completeQuizSession(quizSessionId, score, score * 10, totalSessionTimeElapsed); // Assuming 10 points per correct answer
            JOptionPane.showMessageDialog(this, "Quiz finished! Your score: " + score + "/" + questions.size() +
                    "\nTotal Session Time: " + totalSessionTimeElapsed + " seconds.", "Quiz Complete", JOptionPane.INFORMATION_MESSAGE);
            // After quiz, return to the welcome screen or a results screen
            showWelcomePanel(); // Go back to welcome screen
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error completing quiz session: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            showWelcomePanel(); // Still go back even on error
        }
    }
}