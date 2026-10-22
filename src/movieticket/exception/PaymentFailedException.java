package movieticket.exception;

/** Custom checked exception: thrown when a payment is rejected. */
public class PaymentFailedException extends BookingException {
    private static final long serialVersionUID = 1L;

    public PaymentFailedException(String message) {
        super("Payment failed: " + message);
    }
}
