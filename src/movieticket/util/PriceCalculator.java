package movieticket.util;

import java.util.Vector;

import movieticket.model.Discountable;
import movieticket.model.Show;

/** Calculates ticket prices. Declared final, so it cannot be extended. */
public final class PriceCalculator {
    public static final double GST_RATE = 0.18;

    private PriceCalculator() {
    }

    /** Rows A-B are Silver (base price), C-D are Gold (+25%), row E is Platinum (+50%). */
    public static double seatPrice(double basePrice, String seatLabel) {
        double multiplier;
        switch (seatLabel.charAt(0)) {
            case 'A':
            case 'B':
                multiplier = 1.0;
                break;
            case 'C':
            case 'D':
                multiplier = 1.25;
                break;
            default:
                multiplier = 1.5;
                break;
        }
        return basePrice * multiplier;
    }

    public static String seatClass(String seatLabel) {
        char row = seatLabel.charAt(0);
        return (row <= 'B') ? "Silver" : (row <= 'D') ? "Gold" : "Platinum";   // nested ternary
    }

    /** Recursion: adds prices[index] + prices[index+1] + ... */
    public static double sumPrices(double[] prices, int index) {
        if (index >= prices.length) {
            return 0.0;
        }
        return prices[index] + sumPrices(prices, index + 1);
    }

    /** Method overloading: no discount. */
    public static Breakdown calculate(Show show, Vector<String> seats) {
        return calculate(show, seats, null);
    }

    /** Method overloading: with a discount (interface reference, dynamic method dispatch). */
    public static Breakdown calculate(Show show, Vector<String> seats, Discountable discountable) {
        double[] prices = new double[seats.size()];
        for (int i = 0; i < prices.length; i++) {
            prices[i] = seatPrice(show.getBasePrice(), seats.get(i));
        }
        double subtotal = sumPrices(prices, 0);
        double rate = (discountable == null) ? 0.0 : discountable.getDiscountRate();
        double discount = subtotal * rate;
        double tax = (subtotal - discount) * GST_RATE;
        return new Breakdown(round(subtotal), round(discount), round(tax), round(subtotal - discount + tax));
    }

    private static double round(double value) {
        return (double) Math.round(value * 100) / 100;     // explicit casting
    }

    /** Static nested class that holds the result of a price calculation (returned as an object). */
    public static class Breakdown {
        private final double subtotal;
        private final double discount;
        private final double tax;
        private final double total;

        public Breakdown(double subtotal, double discount, double tax, double total) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.tax = tax;
            this.total = total;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public double getDiscount() {
            return discount;
        }

        public double getTax() {
            return tax;
        }

        public double getTotal() {
            return total;
        }
    }
}
