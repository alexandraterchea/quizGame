// quiz/ui/RegisterFrame.java - IMPROVED VERSION
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
import java.util.regex.Pattern;

public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JProgressBar strengthBar;
    private JLabel strengthLabel;
    private JButton exitButton; // New exit button

    // Color scheme matching the GameFrame
    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color SECONDARY_COLOR = new Color(60, 160, 60);
    private final Color ERROR_COLOR = new Color(220, 53, 69);
    private final Color WARNING_COLOR = new Color(255, 193, 7);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    public RegisterFrame() {
        setupFrame();
        initializeComponents();
        setupEventListeners();
    }

    private void setupFrame() {
        setTitle("QuizMaster - Register");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this frame
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void initializeComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        panel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = createStyledTextField("Username");
        emailField = createStyledTextField("Email");
        passwordField = createStyledPasswordField("Password");
        confirmPasswordField = createStyledPasswordField("Confirm Password");

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthBar.setMinimumSize(new Dimension(300, 20));
        strengthBar.setMaximumSize(new Dimension(300, 20));
        strengthBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        strengthLabel = new JLabel("Password Strength: Weak", SwingConstants.CENTER);
        strengthLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        strengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        registerButton = createStyledButton("Register", SECONDARY_COLOR);
        cancelButton = createStyledButton("Cancel", PRIMARY_COLOR);
        exitButton = createStyledButton("Exit", ERROR_COLOR); // Initialize exit button

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(strengthBar);
        panel.add(strengthLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(confirmPasswordField);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(registerButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(cancelButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(exitButton); // Add exit button to panel
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(statusLabel);

        add(panel);
    }

    private void setupEventListeners() {
        registerButton.addActionListener(e -> registerUser());
        cancelButton.addActionListener(e -> dispose()); // Close register frame
        exitButton.addActionListener(e -> System.exit(0)); // Exit application on click

        passwordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                updatePasswordStrength();
            }
        });
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
        field.putClientProperty("JTextField.placeholderText", placeholder);
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
        field.putClientProperty("JTextField.placeholderText", placeholder);
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

    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        int strength = calculatePasswordStrength(password);
        strengthBar.setValue(strength);

        if (strength >= 80) {
            strengthLabel.setText("Password Strength: Strong 💪");
            strengthBar.setForeground(new Color(0, 150, 0)); // Dark Green
        } else if (strength >= 50) {
            strengthLabel.setText("Password Strength: Medium 👍");
            strengthBar.setForeground(new Color(255, 165, 0)); // Orange
        } else {
            strengthLabel.setText("Password Strength: Weak 👎");
            strengthBar.setForeground(new Color(200, 0, 0)); // Red
        }
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength += 20;
        if (password.matches(".*[A-Z].*")) strength += 20;
        if (password.matches(".*[a-z].*")) strength += 20;
        if (password.matches(".*\\d.*")) strength += 20;
        if (password.matches(".*[!@#$%^&*()].*")) strength += 20;
        return strength;
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!validateInput(username, email, password, confirmPassword)) {
            return; // Validation failed, message already shown
        }

        showSuccessMessage(); // Temporarily show success before starting worker

        SwingWorker<Boolean, Void> registerWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws DatabaseException {
                UserDAO userDAO = new UserDAO();
                User newUser = new User(username, password, email);
                return userDAO.registerUser(newUser);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                                "Registration successful! You can now log in.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Close registration frame
                    } else {
                        showErrorMessage("Registration failed. Username or email might already exist.");
                    }
                } catch (Exception ex) {
                    if (ex.getCause() instanceof DatabaseException) {
                        showErrorMessage("Database connection error. Please try again.");
                    } else {
                        showErrorMessage("Registration failed: " + ex.getMessage());
                    }
                    ex.printStackTrace();
                }
            }
        };
        registerWorker.execute();
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword) {
        if (username.isEmpty()) {
            showErrorMessage("Username is required");
            usernameField.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            showErrorMessage("Email is required");
            emailField.requestFocus();
            return false;
        }
        if (!isValidEmail(email)) {
            showErrorMessage("Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }

        // Password validation
        if (password.isEmpty()) {
            showErrorMessage("Password is required");
            passwordField.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            showErrorMessage("Password must be at least 6 characters long");
            passwordField.requestFocus();
            return false;
        }
        if (calculatePasswordStrength(password) < 1) { // Check if strength is above 0
            showErrorMessage("Password is too weak. Please choose a stronger password");
            passwordField.requestFocus();
            return false;
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            showErrorMessage("Please confirm your password");
            confirmPasswordField.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showErrorMessage("Passwords do not match");
            confirmPasswordField.requestFocus();
            return false;
        }

        return true;
    }

    private void showErrorMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(ERROR_COLOR);
    }

    private void showSuccessMessage() {
        statusLabel.setText("Account created successfully! Redirecting to login...");
        statusLabel.setForeground(SECONDARY_COLOR);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}