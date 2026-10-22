package movieticket.payment;

import movieticket.exception.PaymentFailedException;

/** Target interface. BookingService depends on this abstraction (Open/Closed + Dependency Inversion). */
public interface PaymentProcessor {
    String getName();

    String getInputHint();

    void pay(double amount, String details) throws PaymentFailedException;
}
