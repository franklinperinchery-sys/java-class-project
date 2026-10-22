package movieticket.model;

import java.util.Vector;

import movieticket.exception.InvalidInputException;

/**
 * Seat layout of one screen: 5 rows (A-E) x 8 columns = 40 seats.
 * Booked seats are stored in ONE long value, one bit per seat, using the bitwise operators
 * | (OR), &amp; (AND), ~ (NOT), &lt;&lt; (shift left) and &gt;&gt;&gt; (unsigned shift right).
 */
public class SeatMap {
    public static final int ROWS = 5;
    public static final int COLUMNS = 8;
    private static final String ROW_LETTERS = "ABCDE";

    private long bookedMask;

    public SeatMap() {
        bookedMask = 0L;
    }

    /** Builds the map from a list of already booked seat labels. */
    public SeatMap(Vector<String> bookedSeats) {
        this();
        for (int i = 0; i < bookedSeats.size(); i++) {
            markBooked(bookedSeats.get(i));
        }
    }

    public static String labelOf(int row, int column) {
        return ROW_LETTERS.charAt(row) + String.valueOf(column + 1);
    }

    /** Converts a label such as "C4" into a bit position 0..39. */
    public static int indexOf(String label) {
        if (label == null || label.length() < 2) {
            throw new InvalidInputException("Invalid seat label: " + label);
        }
        int row = ROW_LETTERS.indexOf(label.charAt(0));
        int column;
        try {
            column = Integer.parseInt(label.substring(1)) - 1;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid seat label: " + label);
        }
        if (row < 0 || column < 0 || column >= COLUMNS) {
            throw new InvalidInputException("Seat does not exist: " + label);
        }
        return row * COLUMNS + column;
    }

    public boolean isBooked(String label) {
        return (bookedMask & (1L << indexOf(label))) != 0;
    }

    public void markBooked(String label) {
        bookedMask |= (1L << indexOf(label));
    }

    public void release(String label) {
        bookedMask &= ~(1L << indexOf(label));
    }

    public int countBooked() {
        int count = 0;
        long temp = bookedMask;
        while (temp != 0) {
            count += (int) (temp & 1L);
            temp >>>= 1;
        }
        return count;
    }

    public int countAvailable() {
        return ROWS * COLUMNS - countBooked();
    }
}
