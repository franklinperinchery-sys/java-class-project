package movieticket.model;

/** Entity class: one ticket booking (one or more seats for a show). */
public class Booking {
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int id;
    private String reference;
    private int userId;
    private String customerName;
    private Show show;
    private String seats;              // comma separated, e.g. "C3,C4,C5"
    private double total;
    private String paymentMethod;
    private String status;
    private String bookedAt;

    public Booking(int id, String reference, int userId, String customerName, Show show, String seats,
            double total, String paymentMethod, String status, String bookedAt) {
        this.id = id;
        this.reference = reference;
        this.userId = userId;
        this.customerName = customerName;
        this.show = show;
        this.seats = seats;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.bookedAt = bookedAt;
    }

    /** Constructor overloading: a new confirmed booking. */
    public Booking(String reference, int userId, String customerName, Show show, String seats, double total,
            String paymentMethod) {
        this(0, reference, userId, customerName, show, seats, total, paymentMethod, STATUS_CONFIRMED, "");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public int getUserId() {
        return userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Show getShow() {
        return show;
    }

    public String getSeats() {
        return seats;
    }

    /** Returns the seat labels as an array. */
    public String[] getSeatArray() {
        return seats.split(",");
    }

    public double getTotal() {
        return total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getBookedAt() {
        return bookedAt;
    }

    public boolean isConfirmed() {
        return STATUS_CONFIRMED.equals(status);
    }
}
