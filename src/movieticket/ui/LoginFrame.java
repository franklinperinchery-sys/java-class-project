package movieticket.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import movieticket.dao.UserDao;
import movieticket.exception.AuthenticationException;
import movieticket.exception.InvalidInputException;
import movieticket.model.Admin;
import movieticket.model.Customer;
import movieticket.model.Person;
import movieticket.service.AuthService;

/** First screen: JFrame + JLabel + JTextField + JPasswordField + JButton, BorderLayout/GridLayout/FlowLayout. */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final AuthService authService = new AuthService(new UserDao());

    public LoginFrame() {
        super("Movie Ticket Booking System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildScreen();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildScreen() {
        JLabel title = new JLabel("CINEMA TICKET BOOKING", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 30, 5, 30));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("New Customer? Register");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttons.add(loginButton);
        buttons.add(registerButton);

        statusLabel.setForeground(Color.RED);
        JLabel hint = new JLabel("Demo logins:  admin / admin123    demo / demo123", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));

        JPanel south = new JPanel(new GridLayout(3, 1));
        south.add(buttons);
        south.add(statusLabel);
        south.add(hint);

        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        loginButton.addActionListener(new LoginListener());
        registerButton.addActionListener(new ActionListener() {          // anonymous inner class
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterDialog(LoginFrame.this, authService).setVisible(true);
            }
        });
        getRootPane().setDefaultButton(loginButton);
    }

    /** Named inner class acting as the event listener (Delegation Event Model). */
    private class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            try {
                Person person = authService.login(username, password);
                openDashboard(person);
            } catch (AuthenticationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (InvalidInputException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
            }
        }
    }

    /** Polymorphism: the screen is chosen from the real type of the Person object. */
    private void openDashboard(Person person) {
        dispose();
        if (person instanceof Admin) {
            new AdminFrame((Admin) person).setVisible(true);             // casting
        } else {
            new MainFrame((Customer) person).setVisible(true);
        }
    }
}
