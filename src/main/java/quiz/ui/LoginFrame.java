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
        // setSize(450, 550); // Removed fixed size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen (before maximizing)
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Set to fullscreen
        setResizable(true);
        setLayout(new BorderLayout()); // Use BorderLayout for consistent layout
    }

    private void initializeComponents() {
        // Create a main panel to hold all components
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(50, 50, 50, 50)); // Padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0); // Padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Each component takes a new row

        // Title
        JLabel titleLabel = new JLabel("Welcome to QuizMaster", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        // Subtitle/Welcome message
        JLabel subtitleLabel = new JLabel("Login to continue", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        gbc.gridy = 1;
        mainPanel.add(subtitleLabel, gbc);

        // Spacer
        gbc.insets = new Insets(20, 0, 20, 0); // More space before fields
        gbc.gridy = 2;
        mainPanel.add(Box.createVerticalStrut(1), gbc); // Invisible component for spacing

        // Username Field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(new Color(80, 80, 80));
        gbc.gridy = 3;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridy = 4;
        mainPanel.add(usernameField, gbc);

        // Password Field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(new Color(80, 80, 80));
        gbc.gridy = 5;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridy = 6;
        mainPanel.add(passwordField, gbc);

        // Status Label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(ERROR_COLOR);
        gbc.insets = new Insets(10, 0, 10, 0); // Reset insets
        gbc.gridy = 7;
        mainPanel.add(statusLabel, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // FlowLayout to center buttons
        buttonPanel.setOpaque(false);

        loginButton = createStyledButton("Login", PRIMARY_COLOR);
        registerButton = createStyledButton("Register", SECONDARY_COLOR);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.NONE; // Don't stretch buttons
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
        exitButtonPanel.setBorder(new EmptyBorder(20, 0, 0, 0)); // Padding above button
        exitButtonPanel.add(exitButton);

        // Add mainPanel to the center of the frame
        add(mainPanel, BorderLayout.CENTER);
        add(exitButtonPanel, BorderLayout.SOUTH); // Add exit button to the bottom
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

    private void setupEventListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterFrame();
            }
        });

        // Allow pressing Enter in password field to log in
        passwordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginUser();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        // Allow pressing Enter in username field to focus password field or log in
        usernameField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (usernameField.getText().isEmpty()) {
                        showStatusMessage("Username cannot be empty.", ERROR_COLOR);
                    } else {
                        passwordField.requestFocus();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showStatusMessage("Please enter both username and password.", ERROR_COLOR);
            return;
        }

        // Use SwingWorker for background login process
        SwingWorker<User, Void> loginWorker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                UserDAO userDAO = new UserDAO();
                return userDAO.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User loggedInUser = get();
                    if (loggedInUser != null) {
                        showStatusMessage("Login successful! Redirecting...", SECONDARY_COLOR);
                        dispose(); // Close login frame
                        new GameFrame(loggedInUser).setVisible(true); // Open game frame
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