package movieticket.service;

import java.sql.SQLException;

import movieticket.dao.UserRepository;
import movieticket.exception.AuthenticationException;
import movieticket.exception.InvalidInputException;
import movieticket.model.Customer;
import movieticket.model.Person;
import movieticket.util.PasswordUtil;
import movieticket.util.Validator;

/** Login and registration rules. Depends on the UserRepository interface, not on JDBC (DIP). */
public class AuthService {
    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public Person login(String username, String password) throws AuthenticationException, SQLException {
        if (!Validator.allFilled(username, password)) {
            throw new InvalidInputException("Please enter username and password.");
        }
        Person person = users.authenticate(username.trim(), PasswordUtil.hash(password));
        if (person == null) {
            throw new AuthenticationException("Invalid username or password.");
        }
        return person;
    }

    public Customer register(String username, String password, String confirm, String fullName, String email,
            String phone) throws AuthenticationException, SQLException {
        if (!Validator.allFilled(username, password, fullName, email, phone)) {
            throw new InvalidInputException("All fields are required.");
        }
        if (!Validator.isValidPassword(password)) {
            throw new InvalidInputException("Password must have at least 6 characters.");
        }
        if (!password.equals(confirm)) {
            throw new InvalidInputException("Passwords do not match.");
        }
        if (!Validator.isValidEmail(email)) {
            throw new InvalidInputException("Enter a valid email address.");
        }
        if (!Validator.isValidPhone(phone)) {
            throw new InvalidInputException("Phone number must have exactly 10 digits.");
        }
        if (users.usernameExists(username.trim())) {
            throw new AuthenticationException("Username '" + username.trim() + "' is already taken.");
        }
        Customer customer = new Customer(username.trim(), fullName.trim(), email.trim(), phone.trim());
        users.insertCustomer(customer, PasswordUtil.hash(password));
        return customer;
    }
}
