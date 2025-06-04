package quiz.ui;

import quiz.dao.QuestionDAO;
import quiz.dao.QuizSessionDAO;
import quiz.dao.ScoreDAO;
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
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private User currentUser;
    private int quizSessionId;
    private long questionStartTime;

    private JLabel questionLabel;
    private JRadioButton optionA, optionB, optionC, optionD;
    private ButtonGroup optionsGroup;
    private JButton nextButton;
    private JLabel timerLabel;

    private Timer sessionTimer; // Timer pentru întreaga sesiune de quiz
    private int totalSessionTimeElapsed = 0; // Timpul total al sesiunii în secunde

    public GameFrame(User user) {
        this.currentUser = user;
        setTitle("Game - Welcome " + currentUser.getUsername());
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        addListeners();
        loadQuestions();
    }

    private void initComponents() {
        // Panou de titlu și informații
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        topPanel.add(welcomeLabel, BorderLayout.NORTH);

        timerLabel = new JLabel("Time: 0s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        topPanel.add(timerLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Panou pentru întrebare și opțiuni
        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
        quizPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        questionLabel = new JLabel("<html><p>Question Text Here</p></html>");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        quizPanel.add(questionLabel);
        quizPanel.add(Box.createRigidArea(new Dimension(0, 15)));

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

        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        quizPanel.add(optionA);
        quizPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        quizPanel.add(optionB);
        quizPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        quizPanel.add(optionC);
        quizPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        quizPanel.add(optionD);

        add(quizPanel, BorderLayout.CENTER);

        // Panou pentru butonul Next
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nextButton = new JButton("Next Question");
        nextButton.setFont(new Font("Arial", Font.BOLD, 16));
        buttonPanel.add(nextButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        nextButton.addActionListener(e -> processAnswer());
    }

    private void loadQuestions() {
        QuestionDAO questionDAO = new QuestionDAO();
        try {
            // Exemplu: 10 întrebări aleatorii
            questions = questionDAO.getRandomQuestions(10);

            // Dacă doriți întrebări adaptive, folosiți:
            // questions = questionDAO.getAdaptiveQuestions(currentUser.getId(), 10);

            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No questions available. Please add some questions to the database.", "No Questions", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }

            // Creează o sesiune nouă de quiz în baza de date
            QuizSession quizSession = new QuizSession(currentUser.getId(), questions.size(), "STANDARD"); // Sau "ADAPTIVE"
            quizSessionId = new QuizSessionDAO().createQuizSession(quizSession);

            showQuestion();
            startSessionTimer(); // Începe timer-ul pentru întreaga sesiune
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error loading questions or creating quiz session: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            dispose();
        }
    }

    private void showQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);
            questionLabel.setText("<html><p>" + (currentQuestionIndex + 1) + ". " + q.getText() + "</p></html>");
            optionA.setText("A. " + q.getOptionA());
            optionB.setText("B. " + q.getOptionB());
            optionC.setText("C. " + q.getOptionC());
            optionD.setText("D. " + q.getOptionD());
            optionsGroup.clearSelection(); // Resetează selecția la fiecare întrebare nouă
            questionStartTime = System.currentTimeMillis(); // Înregistrează timpul de început al întrebării
        }
    }

    private void processAnswer() {
        if (optionsGroup.getSelection() == null) {
            JOptionPane.showMessageDialog(this, "Please select an answer.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
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

        // Calculează timpul petrecut pe această întrebare și adaugă-l la timpul total al sesiunii
        long questionEndTime = System.currentTimeMillis();
        int timeTakenForQuestion = (int) ((questionEndTime - questionStartTime) / 1000);
        // totalSessionTimeElapsed este actualizat de sessionTimer, nu la fiecare întrebare.
        // Timpul individual pe întrebare e salvat în `scores` table.

        // Salvează scorul pentru întrebarea curentă
        ScoreDAO scoreDAO = new ScoreDAO();
        try {
            scoreDAO.saveScore(currentUser.getId(), quizSessionId, currentQuestion.getId(), selectedOption, isCorrect, timeTakenForQuestion);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error saving score for question: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        currentQuestionIndex++;
        if (currentQuestionIndex >= questions.size()) {
            endQuiz();
        } else {
            showQuestion();
        }
    }

    private void startSessionTimer() {
        if (sessionTimer != null) {
            sessionTimer.stop();
        }
        totalSessionTimeElapsed = 0; // Resetează timer-ul la începutul unei noi sesiuni
        sessionTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                totalSessionTimeElapsed++;
                timerLabel.setText("Time: " + totalSessionTimeElapsed + "s");
            }
        });
        sessionTimer.start();
    }

    private void endQuiz() {
        if (sessionTimer != null) {
            sessionTimer.stop();
        }

        // Finalizează sesiunea de quiz în baza de date
        QuizSessionDAO quizSessionDAO = new QuizSessionDAO();
        try {
            // Scorul final poate fi scor * 10 de exemplu, sau orice altă logică
            quizSessionDAO.completeQuizSession(quizSessionId, score, score * 10, totalSessionTimeElapsed);
            JOptionPane.showMessageDialog(this, "Quiz finished! Your score: " + score + "/" + questions.size() +
                    "\nTotal Time: " + totalSessionTimeElapsed + " seconds.", "Quiz Complete", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            // Aici s-ar putea naviga înapoi la un meniu principal sau la un ecran de rezultate (ex: new MainMenuFrame(currentUser).setVisible(true);)
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error completing quiz session: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}