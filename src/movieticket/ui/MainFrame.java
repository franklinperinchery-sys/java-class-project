package movieticket.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import movieticket.dao.BookingDao;
import movieticket.dao.MovieDao;
import movieticket.dao.ShowDao;
import movieticket.exception.BookingException;
import movieticket.model.Booking;
import movieticket.model.Customer;
import movieticket.model.Movie;
import movieticket.model.Show;
import movieticket.service.BookingService;

/** Customer dashboard: browse shows, book seats, view and cancel own bookings. */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Customer customer;
    private final ShowDao showDao = new ShowDao();
    private final MovieDao movieDao = new MovieDao();
    private final BookingDao bookingDao = new BookingDao();
    private final BookingService bookingService = new BookingService(bookingDao);

    private final SimpleTableModel showModel = new SimpleTableModel(
            new String[] {"Movie", "Screen", "Date", "Time", "Base Price (Rs)"});
    private final SimpleTableModel bookingModel = new SimpleTableModel(
            new String[] {"Reference", "Movie", "Date", "Time", "Seats", "Total (Rs)", "Paid by", "Status"});
    private final JTable showTable = new JTable(showModel);
    private final JTable bookingTable = new JTable(bookingModel);
    private final JTextField searchField = new JTextField(18);
    private final JLabel movieInfoLabel = new JLabel("Select a show to see movie details.");
    private final JLabel profileLabel = new JLabel();
    private final JTabbedPane tabs = new JTabbedPane();

    private Vector<Show> allShows = new Vector<Show>();
    private Vector<Show> visibleShows = new Vector<Show>();
    private Vector<Booking> myBookings = new Vector<Booking>();

    public MainFrame(Customer customer) {
        super("Movie Ticket Booking - " + customer.getDisplayName());
        this.customer = customer;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        buildScreen();
        loadShows();
        loadBookings();
    }

    private void buildScreen() {
        JLabel welcome = new JLabel("Welcome, " + customer.getFullName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        profileLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        refreshProfile();
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        JPanel titles = new JPanel(new BorderLayout());
        titles.add(welcome, BorderLayout.NORTH);
        titles.add(profileLabel, BorderLayout.SOUTH);
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        header.add(titles, BorderLayout.CENTER);
        header.add(logoutButton, BorderLayout.EAST);

        tabs.addTab("Now Showing", buildShowsTab());
        tabs.addTab("My Bookings", buildBookingsTab());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildShowsTab() {
        showTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    showMovieInfo();
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {                  // adapter class: only one method needed
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("Search movie:"));
        search.add(searchField);

        JButton bookButton = new JButton("Book Seats for Selected Show");
        bookButton.addActionListener(new BookListener());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottom.add(movieInfoLabel, BorderLayout.CENTER);
        bottom.add(bookButton, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(search, BorderLayout.NORTH);
        panel.add(new JScrollPane(showTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBookingsTab() {
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton cancelButton = new JButton("Cancel Selected Booking");
        JButton refreshButton = new JButton("Refresh");
        cancelButton.addActionListener(new CancelListener());
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBookings();
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(cancelButton);
        buttons.add(refreshButton);
        buttons.add(new JLabel("(10% cancellation fee is deducted from the refund)"));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshProfile() {
        // describe() is overridden in Customer: dynamic method dispatch
        profileLabel.setText(customer.describe() + " | Discount: " + (int) (customer.getDiscountRate() * 100) + "%");
    }

    private void loadShows() {
        try {
            allShows = showDao.findAll();
            applyFilter();
        } catch (SQLException e) {
            showError("Could not load shows: " + e.getMessage());
        }
    }

    private void applyFilter() {
        String text = searchField.getText().trim().toLowerCase();
        visibleShows = new Vector<Show>();
        Vector<Object[]> rows = new Vector<Object[]>();
        for (int i = 0; i < allShows.size(); i++) {
            Show s = allShows.get(i);
            if (text.length() == 0 || s.getMovieTitle().toLowerCase().contains(text)) {
                visibleShows.add(s);
                rows.add(new Object[] {s.getMovieTitle(), s.getScreen(), s.getShowDate(), s.getShowTime(),
                    s.getBasePrice()});                                  // autoboxing double -> Double
            }
        }
        showModel.setRows(rows);
    }

    private void loadBookings() {
        try {
            myBookings = bookingDao.findByUser(customer.getId());
            Vector<Object[]> rows = new Vector<Object[]>();
            for (int i = 0; i < myBookings.size(); i++) {
                Booking b = myBookings.get(i);
                rows.add(new Object[] {b.getReference(), b.getShow().getMovieTitle(), b.getShow().getShowDate(),
                    b.getShow().getShowTime(), b.getSeats(), b.getTotal(), b.getPaymentMethod(), b.getStatus()});
            }
            bookingModel.setRows(rows);
        } catch (SQLException e) {
            showError("Could not load bookings: " + e.getMessage());
        }
    }

    private void showMovieInfo() {
        int row = showTable.getSelectedRow();
        if (row < 0) {
            movieInfoLabel.setText("Select a show to see movie details.");
            return;
        }
        try {
            Movie movie = movieDao.findById(visibleShows.get(row).getMovieId());
            if (movie != null) {
                movieInfoLabel.setText(movie.getTitle() + "  |  " + movie.getGenre() + "  |  " + movie.getLanguage()
                        + "  |  " + movie.getDurationText() + "  |  Rating " + movie.getRating() + "/10");
            }
        } catch (SQLException e) {
            showError("Could not load movie: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private class BookListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = showTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(MainFrame.this, "Please select a show first.");
                return;
            }
            SeatSelectionDialog dialog = new SeatSelectionDialog(MainFrame.this, customer, visibleShows.get(row),
                    bookingService);
            dialog.setVisible(true);                                     // modal: waits until it is closed
            if (dialog.isBookingDone()) {
                loadBookings();
                refreshProfile();
                tabs.setSelectedIndex(1);
            }
        }
    }

    private class CancelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = bookingTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(MainFrame.this, "Please select a booking first.");
                return;
            }
            Booking booking = myBookings.get(row);
            int answer = JOptionPane.showConfirmDialog(MainFrame.this,
                    "Cancel booking " + booking.getReference() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                double refund = bookingService.cancelBooking(booking, customer.getId());
                customer.recordCancellation();
                JOptionPane.showMessageDialog(MainFrame.this, "Booking cancelled. Refund: Rs " + refund);
                loadBookings();
                refreshProfile();
            } catch (BookingException ex) {
                showError(ex.getMessage());
            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
            }
        }
    }
}
