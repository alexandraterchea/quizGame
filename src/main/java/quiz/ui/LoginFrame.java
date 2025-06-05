// quiz/ui/LoginFrame.java - IMPROVED VERSION
package quiz.ui;

import quiz.dao.UserDAO;
import quiz.model.User;
import quiz.exceptions.DatabaseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton exitButton; // New exit button
    private JLabel statusLabel;

    // Color scheme matching the GameFrame
    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color SECONDARY_COLOR = new Color(60, 160, 60);
    private final Color ERROR_COLOR = new Color(220, 53, 69);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    public LoginFrame() {
        setupFrame();
        initializeComponents();
        setupEventListeners();
    }

    private void setupFrame() {
        setTitle("QuizMaster - Login");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void initializeComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(50, 40, 50, 40));
        panel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel("Welcome to QuizMaster!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Please log in to continue");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = createStyledTextField("Username");
        passwordField = createStyledPasswordField("Password");

        loginButton = createStyledButton("Login", PRIMARY_COLOR);
        registerButton = createStyledButton("Register", SECONDARY_COLOR);
        exitButton = createStyledButton("Exit", ERROR_COLOR); // Initialize exit button

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(subtitleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 40)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(loginButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(registerButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(exitButton); // Add exit button to panel
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(statusLabel);

        add(panel);
    }

    private void setupEventListeners() {
        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> showRegisterFrame());
        exitButton.addActionListener(e -> System.exit(0)); // Exit application on click

        // Add KeyListener for Enter key on password field
        KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(300, 40));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR.brighter(), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.putClientProperty("JTextField.placeholderText", placeholder); // For modern look & feel if supported
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(placeholder);
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(300, 40));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR.brighter(), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.putClientProperty("JTextField.placeholderText", placeholder); // For modern look & feel if supported
        return field;
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
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showStatusMessage("Username and password are required.", ERROR_COLOR);
            return;
        }

        showStatusMessage("Logging in...", PRIMARY_COLOR);

        // Use SwingWorker for background login operation
        SwingWorker<User, Void> loginWorker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws DatabaseException {
                UserDAO userDAO = new UserDAO();
                return userDAO.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User authenticatedUser = get(); // Get the result from doInBackground
                    if (authenticatedUser != null) {
                        showStatusMessage("Login successful! Welcome, " + authenticatedUser.getUsername() + "!", SECONDARY_COLOR);
                        // Open GameFrame
                        SwingUtilities.invokeLater(() -> {
                            GameFrame gameFrame = new GameFrame(authenticatedUser);
                            gameFrame.setVisible(true);
                            dispose(); // Close login frame
                        });
                    } else {
                        showStatusMessage("Invalid username or password.", ERROR_COLOR);
                        passwordField.selectAll();
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    if (ex.getCause() instanceof DatabaseException) {
                        showStatusMessage("Database connection error. Please try again.", ERROR_COLOR);
                    } else {
                        showStatusMessage("Login failed. Please try again.", ERROR_COLOR);
                    }
                    ex.printStackTrace();
                }
            }
        };

        loginWorker.execute();
    }

    private void showRegisterFrame() {
        new RegisterFrame().setVisible(true);
    }

    private void showStatusMessage(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);

        // Clear the message after 5 seconds if it's not an error
        if (!color.equals(ERROR_COLOR)) {
            Timer timer = new Timer(5000, e -> statusLabel.setText(" "));
            timer.setRepeats(false);
            timer.start();
        }
    }

    // Method to clear form (useful for testing)
    public void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText(" ");
        usernameField.requestFocus();
    }
}