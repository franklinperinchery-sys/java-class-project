package movieticket.dao;

import java.sql.SQLException;
import java.util.Vector;

import movieticket.model.Booking;

/** Interface that EXTENDS another interface and adds booking specific operations. */
public interface BookingRepository extends CrudDao<Booking> {
    Vector<String> findBookedSeats(int showId) throws SQLException;

    Vector<Booking> findByUser(int userId) throws SQLException;

    boolean cancel(int bookingId, int userId) throws SQLException;

    double totalRevenue() throws SQLException;
}
