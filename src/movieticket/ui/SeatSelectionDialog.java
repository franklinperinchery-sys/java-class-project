package movieticket.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import movieticket.exception.BookingException;
import movieticket.exception.InvalidInputException;
import movieticket.exception.PaymentFailedException;
import movieticket.exception.SeatUnavailableException;
import movieticket.model.Booking;
import movieticket.model.Customer;
import movieticket.model.SeatMap;
import movieticket.model.Show;
import movieticket.payment.CardPayment;
import movieticket.payment.LegacyBankGateway;
import movieticket.payment.NetBankingAdapter;
import movieticket.payment.PaymentProcessor;
import movieticket.payment.UpiPayment;
import movieticket.service.BookingService;
import movieticket.util.PriceCalculator;

/** Seat grid (JToggleButton) + payment choice (JRadioButton) + price summary (JLabel). */
public class SeatSelectionDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Customer customer;
    private final Show show;
    private final BookingService service;

    private final Vector<String> selectedSeats = new Vector<String>();
    private final JToggleButton[][] seatButtons = new JToggleButton[SeatMap.ROWS][SeatMap.COLUMNS];   // 2-D array
    private final JLabel summaryLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JRadioButton cardRadio = new JRadioButton("Card", true);
    private final JRadioButton upiRadio = new JRadioButton("UPI");
    private final JRadioButton bankRadio = new JRadioButton("Net Banking");
    private final JLabel hintLabel = new JLabel();
    private final JTextField detailsField = new JTextField(20);
    private boolean bookingDone = false;

    public SeatSelectionDialog(JFrame owner, Customer customer, Show show, BookingService service) {
        super(owner, "Select Seats - " + show.getMovieTitle(), true);
        this.customer = customer;
        this.show = show;
        this.service = service;
        buildScreen();
        refreshSeats();
        updateSummary();
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isBookingDone() {
        return bookingDone;
    }

    private void buildScreen() {
        JLabel info = new JLabel(show.toString() + "   |   Base price Rs " + show.getBasePrice(),
                SwingConstants.CENTER);
        info.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel screenLabel = new JLabel("=========== SCREEN THIS WAY ===========", SwingConstants.CENTER);
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.DARK_GRAY);
        screenLabel.setForeground(Color.WHITE);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 5));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        top.add(info);
        top.add(screenLabel);

        JPanel grid = new JPanel(new GridLayout(SeatMap.ROWS, SeatMap.COLUMNS, 5, 5));
        grid.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        for (int r = 0; r < SeatMap.ROWS; r++) {
            for (int c = 0; c < SeatMap.COLUMNS; c++) {
                String label = SeatMap.labelOf(r, c);
                JToggleButton button = new JToggleButton(label);
                button.setToolTipText(PriceCalculator.seatClass(label) + " - Rs "
                        + PriceCalculator.seatPrice(show.getBasePrice(), label));
                button.addActionListener(new SeatListener(label));
                seatButtons[r][c] = button;
                grid.add(button);
            }
        }

        JLabel legend = new JLabel("Grey = booked | Green = your selection | Rows A-B Silver, C-D Gold (+25%), E Platinum (+50%)",
                SwingConstants.CENTER);
        legend.setFont(new Font("SansSerif", Font.ITALIC, 11));

        ButtonGroup group = new ButtonGroup();
        group.add(cardRadio);
        group.add(upiRadio);
        group.add(bankRadio);
        PaymentTypeListener typeListener = new PaymentTypeListener();
        cardRadio.addItemListener(typeListener);
        upiRadio.addItemListener(typeListener);
        bankRadio.addItemListener(typeListener);

        JPanel radios = new JPanel(new FlowLayout(FlowLayout.CENTER));
        radios.add(new JLabel("Pay by:"));
        radios.add(cardRadio);
        radios.add(upiRadio);
        radios.add(bankRadio);

        JPanel details = new JPanel(new FlowLayout(FlowLayout.CENTER));
        details.add(hintLabel);
        details.add(detailsField);
        hintLabel.setText(new CardPayment().getInputHint() + ":");

        JButton payButton = new JButton("Pay & Confirm Booking");
        JButton closeButton = new JButton("Close");
        payButton.addActionListener(new PayListener());
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(payButton);
        buttons.add(closeButton);

        JPanel south = new JPanel(new GridLayout(5, 1));
        south.add(legend);
        south.add(summaryLabel);
        south.add(radios);
        south.add(details);
        south.add(buttons);

        add(top, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    /** Reads the latest booked seats from the database and updates the buttons. */
    private void refreshSeats() {
        SeatMap seatMap;
        try {
            seatMap = service.loadSeatMap(show.getId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load seats: " + e.getMessage());
            seatMap = new SeatMap();
        }
        for (int r = 0; r < SeatMap.ROWS; r++) {
            for (int c = 0; c < SeatMap.COLUMNS; c++) {
                String label = SeatMap.labelOf(r, c);
                JToggleButton button = seatButtons[r][c];
                if (seatMap.isBooked(label)) {
                    button.setSelected(false);
                    button.setEnabled(false);
                    button.setBackground(Color.GRAY);
                    selectedSeats.remove(label);
                }
            }
        }
        updateSummary();
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            summaryLabel.setText("No seats selected (max " + BookingService.MAX_SEATS_PER_BOOKING + ")");
            return;
        }
        PriceCalculator.Breakdown price = PriceCalculator.calculate(show, selectedSeats, customer);
        summaryLabel.setText("Seats " + selectedSeats + "  |  Subtotal Rs " + price.getSubtotal()
                + "  |  Discount Rs " + price.getDiscount() + "  |  GST Rs " + price.getTax()
                + "  |  TOTAL Rs " + price.getTotal());
    }

    private PaymentProcessor createProcessor() {
        if (cardRadio.isSelected()) {
            return new CardPayment();
        } else if (upiRadio.isSelected()) {
            return new UpiPayment();
        }
        return new NetBankingAdapter(new LegacyBankGateway());            // Adapter pattern in use
    }

    /** Event source: a seat button. Event class: ActionEvent. Listener: this inner class. */
    private class SeatListener implements ActionListener {
        private final String label;

        SeatListener(String label) {
            this.label = label;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JToggleButton source = (JToggleButton) e.getSource();
            if (source.isSelected()) {
                if (selectedSeats.size() >= BookingService.MAX_SEATS_PER_BOOKING) {
                    source.setSelected(false);
                    JOptionPane.showMessageDialog(SeatSelectionDialog.this,
                            "You can select at most " + BookingService.MAX_SEATS_PER_BOOKING + " seats.");
                    return;
                }
                selectedSeats.add(label);
                source.setBackground(Color.GREEN);
            } else {
                selectedSeats.remove(label);
                source.setBackground(null);
            }
            updateSummary();
        }
    }

    /** ItemListener: changes the hint text when another payment method is chosen. */
    private class PaymentTypeListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                hintLabel.setText(createProcessor().getInputHint() + ":");
                detailsField.setText("");
                pack();
            }
        }
    }

    private class PayListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Booking booking = service.bookTickets(customer, show, selectedSeats, createProcessor(),
                        detailsField.getText().trim());
                customer.recordBooking();
                bookingDone = true;
                showReceipt(booking);
                dispose();
            } catch (SeatUnavailableException ex) {                       // most specific exceptions first
                JOptionPane.showMessageDialog(SeatSelectionDialog.this, ex.getMessage(), "Seat unavailable",
                        JOptionPane.WARNING_MESSAGE);
                refreshSeats();
            } catch (PaymentFailedException ex) {
                JOptionPane.showMessageDialog(SeatSelectionDialog.this, ex.getMessage(), "Payment",
                        JOptionPane.ERROR_MESSAGE);
            } catch (BookingException ex) {
                JOptionPane.showMessageDialog(SeatSelectionDialog.this, ex.getMessage(), "Booking",
                        JOptionPane.ERROR_MESSAGE);
            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(SeatSelectionDialog.this, ex.getMessage(), "Invalid input",
                        JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(SeatSelectionDialog.this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showReceipt(Booking booking) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("BOOKING CONFIRMED\n\n");
        receipt.append("Reference : ").append(booking.getReference()).append('\n');
        receipt.append("Movie     : ").append(show.getMovieTitle()).append('\n');
        receipt.append("Screen    : ").append(show.getScreen()).append('\n');
        receipt.append("Date/Time : ").append(show.getShowDate()).append(' ').append(show.getShowTime()).append('\n');
        receipt.append("Seats     : ").append(booking.getSeats()).append('\n');
        receipt.append("Paid      : Rs ").append(booking.getTotal()).append(" (").append(booking.getPaymentMethod())
                .append(")");
        JOptionPane.showMessageDialog(this, receipt.toString(), "Ticket", JOptionPane.INFORMATION_MESSAGE);
    }
}
