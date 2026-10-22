package movieticket.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import movieticket.model.Booking;
import movieticket.model.Show;
import movieticket.util.DatabaseConnection;
import movieticket.util.DbUtil;

/** JDBC access to bookings and booked_seats. Booking + seats are saved in ONE transaction. */
public class BookingDao implements BookingRepository {
    private static final String SELECT = "SELECT b.id, b.reference, b.user_id, u.full_name, b.show_id, b.seats, "
            + "b.total, b.payment_method, b.status, b.booked_at, s.movie_id, m.title, s.screen, s.show_date, "
            + "s.show_time, s.price FROM bookings b JOIN users u ON u.id = b.user_id "
            + "JOIN shows s ON s.id = b.show_id JOIN movies m ON m.id = s.movie_id ";

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Booking map(ResultSet rs) throws SQLException {
        Show show = new Show(rs.getInt("show_id"), rs.getInt("movie_id"), rs.getString("title"),
                rs.getString("screen"), rs.getString("show_date"), rs.getString("show_time"), rs.getDouble("price"));
        return new Booking(rs.getInt("id"), rs.getString("reference"), rs.getInt("user_id"),
                rs.getString("full_name"), show, rs.getString("seats"), rs.getDouble("total"),
                rs.getString("payment_method"), rs.getString("status"), rs.getString("booked_at"));
    }

    @Override
    public int insert(Booking booking) throws SQLException {
        Connection con = connection();
        PreparedStatement bookingStmt = null;
        PreparedStatement seatStmt = null;
        ResultSet keys = null;
        boolean oldAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);                       // start transaction
            bookingStmt = con.prepareStatement(
                    "INSERT INTO bookings (reference, user_id, show_id, seats, total, payment_method, status) "
                    + "VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            bookingStmt.setString(1, booking.getReference());
            bookingStmt.setInt(2, booking.getUserId());
            bookingStmt.setInt(3, booking.getShow().getId());
            bookingStmt.setString(4, booking.getSeats());
            bookingStmt.setDouble(5, booking.getTotal());
            bookingStmt.setString(6, booking.getPaymentMethod());
            bookingStmt.setString(7, booking.getStatus());
            bookingStmt.executeUpdate();
            keys = bookingStmt.getGeneratedKeys();
            int bookingId = keys.next() ? keys.getInt(1) : 0;

            seatStmt = con.prepareStatement(
                    "INSERT INTO booked_seats (booking_id, show_id, seat_label) VALUES (?,?,?)");
            String[] seats = booking.getSeatArray();
            for (int i = 0; i < seats.length; i++) {
                seatStmt.setInt(1, bookingId);
                seatStmt.setInt(2, booking.getShow().getId());
                seatStmt.setString(3, seats[i]);
                seatStmt.executeUpdate();
            }
            con.commit();
            return bookingId;
        } catch (SQLException e) {
            con.rollback();                                 // undo everything if any seat fails
            throw e;
        } finally {
            DbUtil.close(keys, bookingStmt, seatStmt);
            con.setAutoCommit(oldAutoCommit);
        }
    }

    @Override
    public Booking findById(int id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection().prepareStatement(SELECT + "WHERE b.id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } finally {
            DbUtil.close(rs, ps);
        }
    }

    @Override
    public Vector<Booking> findAll() throws SQLException {
        Vector<Booking> list = new Vector<Booking>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection().createStatement();
            rs = st.executeQuery(SELECT + "ORDER BY b.id DESC");
            while (rs.next()) {
                list.add(map(rs));
            }
        } finally {
            DbUtil.close(rs, st);
        }
        return list;
    }

    @Override
    public Vector<Booking> findByUser(int userId) throws SQLException {
        Vector<Booking> list = new Vector<Booking>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection().prepareStatement(SELECT + "WHERE b.user_id = ? ORDER BY b.id DESC");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } finally {
            DbUtil.close(rs, ps);
        }
        return list;
    }

    @Override
    public Vector<String> findBookedSeats(int showId) throws SQLException {
        Vector<String> seats = new Vector<String>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection().prepareStatement("SELECT seat_label FROM booked_seats WHERE show_id = ?");
            ps.setInt(1, showId);
            rs = ps.executeQuery();
            while (rs.next()) {
                seats.add(rs.getString(1));
            }
        } finally {
            DbUtil.close(rs, ps);
        }
        return seats;
    }

    @Override
    public boolean update(Booking booking) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection().prepareStatement("UPDATE bookings SET status = ?, payment_method = ? WHERE id = ?");
            ps.setString(1, booking.getStatus());
            ps.setString(2, booking.getPaymentMethod());
            ps.setInt(3, booking.getId());
            return ps.executeUpdate() > 0;
        } finally {
            DbUtil.close(ps);
        }
    }

    /** Deletes the booking; its rows in booked_seats disappear too (ON DELETE CASCADE). */
    @Override
    public boolean delete(int id) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection().prepareStatement("DELETE FROM bookings WHERE id = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            DbUtil.close(ps);
        }
    }

    /** Marks a booking CANCELLED and frees its seats. */
    @Override
    public boolean cancel(int bookingId, int userId) throws SQLException {
        Connection con = connection();
        PreparedStatement update = null;
        PreparedStatement free = null;
        boolean oldAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            update = con.prepareStatement(
                    "UPDATE bookings SET status = 'CANCELLED' WHERE id = ? AND user_id = ? AND status = 'CONFIRMED'");
            update.setInt(1, bookingId);
            update.setInt(2, userId);
            boolean changed = update.executeUpdate() > 0;
            if (changed) {
                free = con.prepareStatement("DELETE FROM booked_seats WHERE booking_id = ?");
                free.setInt(1, bookingId);
                free.executeUpdate();
            }
            con.commit();
            return changed;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            DbUtil.close(update, free);
            con.setAutoCommit(oldAutoCommit);
        }
    }

    @Override
    public double totalRevenue() throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection().createStatement();
            rs = st.executeQuery("SELECT COALESCE(SUM(total), 0) FROM bookings WHERE status = 'CONFIRMED'");
            return rs.next() ? rs.getDouble(1) : 0.0;
        } finally {
            DbUtil.close(rs, st);
        }
    }
}
