package movieticket.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import movieticket.model.Admin;
import movieticket.model.Customer;
import movieticket.model.Person;
import movieticket.model.PremiumCustomer;
import movieticket.util.DatabaseConnection;
import movieticket.util.DbUtil;

/** JDBC access to the users table. */
public class UserDao implements UserRepository {

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Returns an Admin, Customer or PremiumCustomer (all are Persons), or null if login fails. */
    @Override
    public Person authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT u.id, u.username, u.full_name, u.email, u.phone, u.role, "
                + "(SELECT COUNT(*) FROM bookings b WHERE b.user_id = u.id AND b.status = 'CONFIRMED') AS cnt "
                + "FROM users u WHERE u.username = ? AND u.password_hash = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection().prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            int id = rs.getInt("id");
            String user = rs.getString("username");
            String fullName = rs.getString("full_name");
            String email = rs.getString("email");
            String phone = rs.getString("phone");
            int bookings = rs.getInt("cnt");
            if ("ADMIN".equals(rs.getString("role"))) {
                return new Admin(id, user, fullName, email, phone);
            }
            if (bookings >= PremiumCustomer.PREMIUM_THRESHOLD) {
                return new PremiumCustomer(id, user, fullName, email, phone, bookings);
            }
            return new Customer(id, user, fullName, email, phone, bookings);
        } finally {
            DbUtil.close(rs, ps);
        }
    }

    @Override
    public boolean usernameExists(String username) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection().prepareStatement("SELECT 1 FROM users WHERE username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            DbUtil.close(rs, ps);
        }
    }

    @Override
    public int insertCustomer(Customer customer, String passwordHash) throws SQLException {
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            ps = connection().prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, email, phone, role) "
                    + "VALUES (?,?,?,?,?, 'CUSTOMER')", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, customer.getUsername());
            ps.setString(2, passwordHash);
            ps.setString(3, customer.getFullName());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getPhone());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        } finally {
            DbUtil.close(keys, ps);
        }
    }
}
