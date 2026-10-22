package movieticket.model;

import movieticket.util.AppLog;

/** A normal customer. Single inheritance from Person + implements the Discountable interface. */
public class Customer extends Person implements Discountable {
    public static final int LOYALTY_THRESHOLD = 3;
    protected int totalBookings;

    public Customer(int id, String username, String fullName, String email, String phone, int totalBookings) {
        super(id, username, fullName, email, phone);      // super class constructor runs FIRST
        this.totalBookings = totalBookings;
        AppLog.log("2. Customer constructor (sub class)");
    }

    /** Constructor overloading: used for a brand new customer. */
    public Customer(String username, String fullName, String email, String phone) {
        this(0, username, fullName, email, phone, 0);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public double getDiscountRate() {
        return (totalBookings >= LOYALTY_THRESHOLD) ? 0.10 : 0.0;
    }

    @Override
    public String describe() {
        return super.describe() + " | Bookings so far: " + totalBookings;     // super keyword
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void recordBooking() {
        totalBookings++;
    }

    public void recordCancellation() {
        if (totalBookings > 0) {
            totalBookings--;
        }
    }
}
