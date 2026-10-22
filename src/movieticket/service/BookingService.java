package movieticket.service;

import java.sql.SQLException;
import java.util.Vector;

import movieticket.dao.BookingRepository;
import movieticket.exception.BookingException;
import movieticket.exception.InvalidInputException;
import movieticket.exception.PaymentFailedException;
import movieticket.exception.SeatUnavailableException;
import movieticket.model.Booking;
import movieticket.model.Customer;
import movieticket.model.SeatMap;
import movieticket.model.Show;
import movieticket.payment.PaymentProcessor;
import movieticket.util.AppLog;
import movieticket.util.PriceCalculator;

/**
 * Business rules for booking and cancelling.
 * SRP: only booking logic lives here (SQL is in the DAO, screens are in the ui package).
 * DIP: depends on the BookingRepository and PaymentProcessor interfaces.
 */
public class BookingService {
    public static final int MAX_SEATS_PER_BOOKING = 6;
    public static final double CANCELLATION_FEE_RATE = 0.10;

    private static int sequence = 0;                       // static member shared by all objects

    private final BookingRepository repository;
    private final ReferenceGenerator referenceGenerator = new ReferenceGenerator();

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public SeatMap loadSeatMap(int showId) throws SQLException {
        return new SeatMap(repository.findBookedSeats(showId));       // returning an object
    }

    public Booking bookTickets(Customer customer, Show show, Vector<String> seats, PaymentProcessor payment,
            String paymentDetails) throws BookingException, SQLException {
        if (seats == null || seats.isEmpty()) {
            throw new InvalidInputException("Select at least one seat.");
        }
        if (seats.size() > MAX_SEATS_PER_BOOKING) {
            throw new InvalidInputException("You can book at most " + MAX_SEATS_PER_BOOKING + " seats at a time.");
        }

        SeatMap seatMap = loadSeatMap(show.getId());                  // object used as a parameter below
        checkAvailability(seatMap, seats);

        PriceCalculator.Breakdown price = PriceCalculator.calculate(show, seats, customer);
        Booking booking = new Booking(referenceGenerator.next(show.getId()), customer.getId(),
                customer.getFullName(), show, joinSeats(seats), price.getTotal(), payment.getName());

        int bookingId;
        try {
            bookingId = repository.insert(booking);               // reserves the seats atomically
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new SeatUnavailableException("one of your selected seats");
            }
            throw e;
        }
        booking.setId(bookingId);

        try {
            try {
                payment.pay(price.getTotal(), paymentDetails);     // may throw PaymentFailedException
            } catch (PaymentFailedException e) {                   // inner catch: undo the reservation
                repository.delete(bookingId);
                throw e;                                           // re-throw to the caller
            }
        } finally {
            AppLog.log("Booking attempt " + booking.getReference() + " finished");
        }
        return booking;
    }

    /** Cancels a booking and returns the refund amount (total minus the cancellation fee). */
    public double cancelBooking(Booking booking, int requestingUserId) throws BookingException, SQLException {
        if (!booking.isConfirmed()) {
            throw new BookingException("This booking is already cancelled.");
        }
        if (!repository.cancel(booking.getId(), requestingUserId)) {
            throw new BookingException("Unable to cancel this booking.");
        }
        return Math.round(booking.getTotal() * (1 - CANCELLATION_FEE_RATE) * 100) / 100.0;
    }

    private void checkAvailability(SeatMap seatMap, Vector<String> seats) throws SeatUnavailableException {
        for (int i = 0; i < seats.size(); i++) {
            if (seatMap.isBooked(seats.get(i))) {
                throw new SeatUnavailableException(seats.get(i));
            }
        }
    }

    private String joinSeats(Vector<String> seats) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(seats.get(i));
        }
        return builder.toString();
    }

    /** Non-static INNER CLASS: it can use the outer class' members. */
    private class ReferenceGenerator {
        String next(int showId) {
            sequence++;
            long stamp = System.currentTimeMillis() % 1000000;
            return "MTB" + showId + stamp + sequence;
        }
    }
}
