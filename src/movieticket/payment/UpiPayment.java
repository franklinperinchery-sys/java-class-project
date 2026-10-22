package movieticket.payment;

import movieticket.exception.PaymentFailedException;

/** Simulated UPI payment. The UPI id must look like name@bank. */
public class UpiPayment implements PaymentProcessor {

    @Override
    public String getName() {
        return "UPI";
    }

    @Override
    public String getInputHint() {
        return "UPI id, e.g. name@bank";
    }

    @Override
    public void pay(double amount, String details) throws PaymentFailedException {
        if (details == null || details.indexOf('@') < 1 || details.indexOf('@') == details.length() - 1) {
            throw new PaymentFailedException("invalid UPI id");
        }
    }
}
