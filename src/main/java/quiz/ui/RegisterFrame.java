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
        // setSize(500, 650); // Removed fixed size
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Only dispose this frame, not exit app
        setLocationRelativeTo(null); // Center on screen (before maximizing)
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Set to fullscreen
        setResizable(true);
        setLayout(new BorderLayout());
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Each component takes a new row

        JLabel titleLabel = new JLabel("Create Your Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Join QuizMaster today!", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        gbc.insets = new Insets(5, 0, 20, 0); // More space after subtitle
        gbc.gridy = 1;
        mainPanel.add(subtitleLabel, gbc);

        gbc.insets = new Insets(8, 0, 8, 0); // Reset insets

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(new Color(80, 80, 80));
        gbc.gridy = 2;
        mainPanel.add(usernameLabel, gbc);
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        gbc.gridy = 3;
        mainPanel.add(usernameField, gbc);

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 14));
        emailLabel.setForeground(new Color(80, 80, 80));
        gbc.gridy = 4;
        mainPanel.add(emailLabel, gbc);
        emailField = new JTextField(20);
        styleTextField(emailField);
        gbc.gridy = 5;
        mainPanel.add(emailField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(new Color(80, 80, 80));
        gbc.gridy = 6;
        mainPanel.add(passwordLabel, gbc);
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        gbc.gridy = 7;
        mainPanel.add(passwordField, gbc);

        // Password Strength
        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthBar.setFont(new Font("Arial", Font.PLAIN, 12));
        strengthBar.setBorderPainted(false);
        gbc.insets = new Insets(0, 0, 5, 0); // Closer to password field
        gbc.gridy = 8;
        mainPanel.add(strengthBar, gbc);

        strengthLabel = new JLabel("Strength: Very Weak", SwingConstants.RIGHT);
        strengthLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        strengthLabel.setForeground(new Color(150, 150, 150));
        gbc.insets = new Insets(0, 0, 15, 0); // Space after strength label
        gbc.gridy = 9;
        mainPanel.add(strengthLabel, gbc);

        gbc.insets = new Insets(8, 0, 8, 0); // Reset insets

        // Confirm Password
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        confirmPasswordLabel.setForeground(new Color(80, 80, 80));
        gbc.gridy = 10;
        mainPanel.add(confirmPasswordLabel, gbc);
        confirmPasswordField = new JPasswordField(20);
        styleTextField(confirmPasswordField);
        gbc.gridy = 11;
        mainPanel.add(confirmPasswordField, gbc);

        // Status Label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(ERROR_COLOR);
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridy = 12;
        mainPanel.add(statusLabel, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        registerButton = createStyledButton("Register", SECONDARY_COLOR);
        cancelButton = createStyledButton("Cancel", PRIMARY_COLOR);

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 13;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(buttonPanel, gbc);

        // Exit Button
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
        JPanel exitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exitButtonPanel.setOpaque(false);
        exitButtonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        exitButtonPanel.add(exitButton);

        add(mainPanel, BorderLayout.CENTER);
        add(exitButtonPanel, BorderLayout.SOUTH);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(0, 35)); // Give it a preferred height
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

    private void setupEventListeners() {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the register frame
            }
        });

        // Add KeyListener for password strength
        passwordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { }

            @Override
            public void keyPressed(KeyEvent e) { }

            @Override
            public void keyReleased(KeyEvent e) {
                updatePasswordStrength(new String(passwordField.getPassword()));
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    registerUser();
                }
            }
        });

        // Allow pressing Enter in confirm password field to register
        confirmPasswordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    registerUser();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        // Allow pressing Enter in email field to focus password field
        emailField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        // Allow pressing Enter in username field to focus email field
        usernameField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    emailField.requestFocus();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);
        strengthBar.setValue(strength);

        String strengthText;
        Color strengthColor;

        if (strength == 0) {
            strengthText = "Strength: Very Weak";
            strengthColor = ERROR_COLOR;
        } else if (strength < 40) {
            strengthText = "Strength: Weak";
            strengthColor = ERROR_COLOR;
        } else if (strength < 60) {
            strengthText = "Strength: Moderate";
            strengthColor = WARNING_COLOR;
        } else if (strength < 80) {
            strengthText = "Strength: Strong";
            strengthColor = SECONDARY_COLOR.darker();
        } else {
            strengthText = "Strength: Very Strong";
            strengthColor = SECONDARY_COLOR;
        }

        strengthLabel.setText(strengthText);
        strengthLabel.setForeground(strengthColor);
        strengthBar.setForeground(strengthColor); // Set bar color
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;
        if (password.isEmpty()) return 0;
        if (password.length() < 6) return 0; // Too short

        score += password.length() * 4; // Length points

        if (password.matches(".*[0-9].*")) score += 10; // Digits
        if (password.matches(".*[a-z].*")) score += 10; // Lowercase
        if (password.matches(".*[A-Z].*")) score += 10; // Uppercase
        if (password.matches(".*[!@#$%^&*()].*")) score += 20; // Special characters

        // Deductions for common patterns (simple example)
        if (password.matches(".*(?:password|123456|qwerty).*")) score -= 20;

        return Math.max(0, Math.min(100, score)); // Ensure between 0 and 100
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!validateRegistrationInput(username, email, password, confirmPassword)) {
            return;
        }

        UserDAO userDAO = new UserDAO();
        try {
            // Check if username exists
            if (userDAO.findByUsername(username) != null) {
                showErrorMessage("Username already exists. Please choose a different one.");
                usernameField.requestFocus();
                return;
            }
            // Check if email exists
            if (userDAO.findByEmail(email) != null) {
                showErrorMessage("Email already registered. Please use a different one or login.");
                emailField.requestFocus();
                return;
            }

            // Create new user
            User newUser = new User(username, password, email);
            // Save the new user
            userDAO.save(newUser);

            showSuccessMessage();
            Timer timer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); // Close register frame
                    // Optionally open login frame again or main frame if needed
                }
            });
            timer.setRepeats(false);
            timer.start();

        } catch (DatabaseException ex) {
            showErrorMessage("Registration failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean validateRegistrationInput(String username, String email, String password, String confirmPassword) {
        // Username validation
        if (username.isEmpty()) {
            showErrorMessage("Username is required");
            usernameField.requestFocus();
            return false;
        }
        if (username.length() < 3) {
            showErrorMessage("Username must be at least 3 characters long");
            usernameField.requestFocus();
            return false;
        }
        if (!Pattern.matches("^[a-zA-Z0-9_]+$", username)) {
            showErrorMessage("Username can only contain letters, numbers, and underscores");
            usernameField.requestFocus();
            return false;
        }

        // Email validation
        if (email.isEmpty()) {
            showErrorMessage("Email is required");
            emailField.requestFocus();
            return false;
        }
        if (!isValidEmail(email)) {
            showErrorMessage("Invalid email format");
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
        return email.matches(emailRegex);
    }
}