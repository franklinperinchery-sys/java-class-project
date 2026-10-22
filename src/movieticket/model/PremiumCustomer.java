package movieticket.model;

import movieticket.util.AppLog;

/** Multilevel inheritance: Person -> Customer -> PremiumCustomer (10 or more bookings). */
public class PremiumCustomer extends Customer {
    public static final int PREMIUM_THRESHOLD = 10;

    public PremiumCustomer(int id, String username, String fullName, String email, String phone, int totalBookings) {
        super(id, username, fullName, email, phone, totalBookings);
        AppLog.log("3. PremiumCustomer constructor (sub-sub class)");
    }

    @Override
    public String getRole() {
        return "PREMIUM CUSTOMER";
    }

    @Override
    public double getDiscountRate() {
        return 0.20;
    }
}
