package movieticket.dao;

import java.sql.SQLException;

import movieticket.model.Customer;
import movieticket.model.Person;

/** Abstraction used by AuthService (Dependency Inversion Principle). */
public interface UserRepository {
    Person authenticate(String username, String passwordHash) throws SQLException;

    boolean usernameExists(String username) throws SQLException;

    int insertCustomer(Customer customer, String passwordHash) throws SQLException;
}
