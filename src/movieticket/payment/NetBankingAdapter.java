package movieticket.payment;

import movieticket.exception.PaymentFailedException;

/**
 * ADAPTER design pattern: makes the incompatible LegacyBankGateway usable through the
 * PaymentProcessor interface the rest of the application expects.
 */
public class NetBankingAdapter implements PaymentProcessor {
    private final LegacyBankGateway gateway;

    public NetBankingAdapter(LegacyBankGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public String getName() {
        return "NET BANKING";
    }

    @Override
    public String getInputHint() {
        return "account number (9 to 18 digits)";
    }

    @Override
    public void pay(double amount, String details) throws PaymentFailedException {
        int paise = (int) Math.round(amount * 100);            // double -> int casting
        int code = gateway.makeTransaction(details, paise);
        switch (code) {
            case LegacyBankGateway.OK:
                return;
            case LegacyBankGateway.INVALID_ACCOUNT:
                throw new PaymentFailedException("invalid account number");
            case LegacyBankGateway.LIMIT_EXCEEDED:
                throw new PaymentFailedException("net banking limit exceeded");
            default:
                throw new PaymentFailedException("bank error code " + code);
        }
    }
}
