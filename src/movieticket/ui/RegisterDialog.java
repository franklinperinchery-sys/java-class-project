package movieticket.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import movieticket.exception.AuthenticationException;
import movieticket.exception.InvalidInputException;
import movieticket.service.AuthService;

/** Registration form for new customers. */
public class RegisterDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final AuthService authService;
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JPasswordField confirmField = new JPasswordField(16);
    private final JTextField nameField = new JTextField(16);
    private final JTextField emailField = new JTextField(16);
    private final JTextField phoneField = new JTextField(16);

    public RegisterDialog(JFrame owner, AuthService authService) {
        super(owner, "Customer Registration", true);
        this.authService = authService;

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password (min 6):"));
        form.add(passwordField);
        form.add(new JLabel("Confirm password:"));
        form.add(confirmField);
        form.add(new JLabel("Full name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Phone (10 digits):"));
        form.add(phoneField);

        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(registerButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pack();
        setLocationRelativeTo(owner);
    }

    private void register() {
        try {
            authService.register(usernameField.getText(), new String(passwordField.getPassword()),
                    new String(confirmField.getPassword()), nameField.getText(), emailField.getText(),
                    phoneField.getText());
            JOptionPane.showMessageDialog(this, "Registration successful. You can log in now.");
            dispose();
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot register", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
