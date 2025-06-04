// quiz/ui/LoginFrame.java - MODIFIED
package quiz.ui;

import quiz.dao.UserDAO;
import quiz.model.User;
import util.DatabaseException;

import javax.swing.*;
import java.awt.*;
// import java.sql.SQLException; // No longer needed directly

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            UserDAO userDAO = new UserDAO();

            try {
                User loggedInUser = userDAO.login(username, password); // Get User object
                if (loggedInUser != null) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login successful!");
                    dispose();
                    new GameFrame(loggedInUser).setVisible(true); // Pass the User object to GameFrame
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Invalid username or password.");
                }
            } catch (DatabaseException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LoginFrame.this, "Database error during login: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
        });
    }
}