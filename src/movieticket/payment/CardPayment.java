package movieticket.payment;

import movieticket.exception.PaymentFailedException;
import movieticket.util.Validator;

/** Simulated debit/credit card payment. A card number must contain exactly 16 digits. */
public class CardPayment implements PaymentProcessor {

    @Override
    public String getName() {
        return "CARD";
    }

    @Override
    public String getInputHint() {
        return "16 digit card number";
    }

    @Override
    public void pay(double amount, String details) throws PaymentFailedException {
        String digits = (details == null) ? "" : details.replace(" ", "");
        if (digits.length() != 16 || !Validator.allDigits(digits)) {
            throw new PaymentFailedException("card number must have 16 digits");
        }
        if (amount > 100000) {
            throw new PaymentFailedException("amount exceeds the card limit");
        }
    }
}
