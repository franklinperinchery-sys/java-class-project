package movieticket.exception;

/** Base CHECKED exception for every problem that can occur while booking or cancelling tickets. */
public class BookingException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookingException(String message) {
        super(message);
    }

    public BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
