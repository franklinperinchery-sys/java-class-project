package movieticket.exception;

/** Custom checked exception: thrown when a requested seat is already booked. */
public class SeatUnavailableException extends BookingException {
    private static final long serialVersionUID = 1L;
    private final String seatLabel;

    public SeatUnavailableException(String seatLabel) {
        super("Seat " + seatLabel + " is already booked. Please choose another seat.");
        this.seatLabel = seatLabel;
    }

    public String getSeatLabel() {
        return seatLabel;
    }
}
