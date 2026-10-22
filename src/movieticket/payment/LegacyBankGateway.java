package movieticket.payment;

/**
 * ADAPTEE for the Adapter design pattern: an "old" bank API that we cannot change.
 * It wants the amount in paise (int) and returns a numeric status code instead of throwing exceptions.
 */
public class LegacyBankGateway {
    public static final int OK = 0;
    public static final int INVALID_ACCOUNT = 1;
    public static final int LIMIT_EXCEEDED = 2;

    public int makeTransaction(String accountNumber, int amountInPaise) {
        if (accountNumber == null || accountNumber.length() < 9 || accountNumber.length() > 18) {
            return INVALID_ACCOUNT;
        }
        if (amountInPaise > 5000000) {          // Rs 50,000
            return LIMIT_EXCEEDED;
        }
        return OK;
    }
}
