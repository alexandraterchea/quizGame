package quiz.ui;

import quiz.dao.UserDAO;
import quiz.model.User;
import util.DatabaseException; // Importă excepția personalizată

import javax.swing.*;
import java.awt.*;
// import java.sql.SQLException; // Nu mai este nevoie de SQLException direct, ci de DatabaseException

public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField; // Adăugat câmp pentru email

    public RegisterFrame() {
        setTitle("Register");
        setSize(350, 260); // Mărit dimensiunea pentru noul câmp
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10)); // 5 rânduri acum
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        panel.add(new JLabel("Email:")); // Câmpul pentru email
        emailField = new JTextField();
        panel.add(emailField);

        JButton registerButton = new JButton("Register");
        panel.add(registerButton);

        add(panel);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText().trim(); // Preluăm email-ul

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(RegisterFrame.this, "Please fill all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(RegisterFrame.this, "Passwords do not match.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validare simplă de email (poate fi îmbunătățită cu RegEx)
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(RegisterFrame.this, "Please enter a valid email address.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }


            UserDAO userDAO = new UserDAO();
            try {
                if (userDAO.userExists(username)) {
                    JOptionPane.showMessageDialog(RegisterFrame.this, "Username already exists.", "Registration Error", JOptionPane.WARNING_MESSAGE);
                } else {
                    User newUser = new User(username, password, email); // Folosim constructorul cu email
                    boolean success = userDAO.register(newUser);
                    if(success) {
                        JOptionPane.showMessageDialog(RegisterFrame.this, "Registration successful! You can now log in.");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(RegisterFrame.this, "Registration failed. Please try again.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (DatabaseException ex) { // Prindem excepția personalizată
                ex.printStackTrace();
                JOptionPane.showMessageDialog(RegisterFrame.this, "Database error during registration: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}