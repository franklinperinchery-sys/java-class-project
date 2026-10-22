package movieticket.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import movieticket.exception.InvalidInputException;
import movieticket.model.Admin;
import movieticket.model.Booking;
import movieticket.model.Movie;
import movieticket.model.Show;
import movieticket.service.BookingService;
import movieticket.util.Validator;

/** Admin dashboard: CRUD for movies and shows, plus a bookings and revenue report. */
public class AdminFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Admin admin;
    private final MovieDao movieDao = new MovieDao();
    private final ShowDao showDao = new ShowDao();
    private final BookingDao bookingDao = new BookingDao();
    private final BookingService bookingService = new BookingService(bookingDao);

    // ----- movies tab -----
    private final SimpleTableModel movieModel = new SimpleTableModel(
            new String[] {"ID", "Title", "Genre", "Language", "Duration (min)", "Rating"});
    private final JTable movieTable = new JTable(movieModel);
    private final JTextField titleField = new JTextField(15);
    private final JTextField genreField = new JTextField(15);
    private final JTextField languageField = new JTextField(15);
    private final JTextField durationField = new JTextField(15);
    private final JTextField ratingField = new JTextField(15);
    private Vector<Movie> movies = new Vector<Movie>();

    // ----- shows tab -----
    private final SimpleTableModel showModel = new SimpleTableModel(
            new String[] {"ID", "Movie", "Screen", "Date", "Time", "Base Price (Rs)"});
    private final JTable showTable = new JTable(showModel);
    private final JComboBox<Movie> movieCombo = new JComboBox<Movie>();
    private final JTextField screenField = new JTextField(12);
    private final JTextField dateField = new JTextField(12);
    private final JTextField timeField = new JTextField(12);
    private final JTextField priceField = new JTextField(12);
    private Vector<Show> shows = new Vector<Show>();

    // ----- bookings tab -----
    private final SimpleTableModel bookingModel = new SimpleTableModel(new String[] {"Reference", "Customer",
        "Movie", "Date", "Time", "Seats", "Total (Rs)", "Paid by", "Status"});
    private final JTable bookingTable = new JTable(bookingModel);
    private final JLabel revenueLabel = new JLabel(" ");
    private Vector<Booking> bookings = new Vector<Booking>();

    public AdminFrame(Admin admin) {
        super("Movie Ticket Booking - Admin Panel");
        this.admin = admin;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 580);
        setLocationRelativeTo(null);
        buildScreen();
        loadMovies();
        loadShows();
        loadBookings();
    }

    private void buildScreen() {
        JLabel welcome = new JLabel("Admin: " + admin.getFullName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        header.add(welcome, BorderLayout.CENTER);
        header.add(logoutButton, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Movies", buildMoviesTab());
        tabs.addTab("Shows", buildShowsTab());
        tabs.addTab("Bookings & Revenue", buildBookingsTab());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // ===================== MOVIES =====================
    private JPanel buildMoviesTab() {
        JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Genre:"));
        form.add(genreField);
        form.add(new JLabel("Language:"));
        form.add(languageField);
        form.add(new JLabel("Duration (minutes):"));
        form.add(durationField);
        form.add(new JLabel("Rating (0-10):"));
        form.add(ratingField);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JPanel buttons = new JPanel(new GridLayout(1, 4, 6, 6));
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        left.add(form, BorderLayout.NORTH);
        left.add(buttons, BorderLayout.CENTER);

        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = movieTable.getSelectedRow();
                if (!e.getValueIsAdjusting() && row >= 0) {
                    Movie m = movies.get(row);
                    titleField.setText(m.getTitle());
                    genreField.setText(m.getGenre());
                    languageField.setText(m.getLanguage());
                    durationField.setText(String.valueOf(m.getDurationMinutes()));
                    ratingField.setText(String.valueOf(m.getRating()));
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMovie(true);
            }
        });
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMovie(false);
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteMovie();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearMovieForm();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(left, BorderLayout.WEST);
        panel.add(new JScrollPane(movieTable), BorderLayout.CENTER);
        return panel;
    }

    private Movie readMovieForm() {
        if (!Validator.allFilled(titleField.getText(), genreField.getText(), languageField.getText(),
                durationField.getText(), ratingField.getText())) {
            throw new InvalidInputException("Fill in all movie fields.");
        }
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            double rating = Double.parseDouble(ratingField.getText().trim());
            if (duration <= 0 || rating < 0 || rating > 10) {
                throw new InvalidInputException("Duration must be positive and rating between 0 and 10.");
            }
            return new Movie(titleField.getText().trim(), genreField.getText().trim(),
                    languageField.getText().trim(), duration, rating);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Duration and rating must be numbers.");
        }
    }

    private void saveMovie(boolean isNew) {
        try {
            Movie movie = readMovieForm();
            if (isNew) {
                movieDao.insert(movie);
            } else {
                int row = movieTable.getSelectedRow();
                if (row < 0) {
                    throw new InvalidInputException("Select a movie in the table to update.");
                }
                movie.setId(movies.get(row).getId());
                movieDao.update(movie);
            }
            clearMovieForm();
            loadMovies();
            loadShows();
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void deleteMovie() {
        int row = movieTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a movie to delete.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete '" + movies.get(row).getTitle() + "' and all its shows?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            try {
                movieDao.delete(movies.get(row).getId());
                clearMovieForm();
                loadMovies();
                loadShows();
            } catch (SQLException ex) {
                showError("Cannot delete this movie because some of its shows already have bookings.\n("
                        + ex.getMessage() + ")");
            }
        }
    }

    private void clearMovieForm() {
        titleField.setText("");
        genreField.setText("");
        languageField.setText("");
        durationField.setText("");
        ratingField.setText("");
        movieTable.clearSelection();
    }

    private void loadMovies() {
        try {
            movies = movieDao.findAll();
            Vector<Object[]> rows = new Vector<Object[]>();
            movieCombo.removeAllItems();
            for (int i = 0; i < movies.size(); i++) {
                Movie m = movies.get(i);
                rows.add(new Object[] {m.getId(), m.getTitle(), m.getGenre(), m.getLanguage(),
                    m.getDurationMinutes(), m.getRating()});
                movieCombo.addItem(m);
            }
            movieModel.setRows(rows);
        } catch (SQLException e) {
            showError("Could not load movies: " + e.getMessage());
        }
    }

    // ===================== SHOWS =====================
    private JPanel buildShowsTab() {
        JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
        form.add(new JLabel("Movie:"));
        form.add(movieCombo);
        form.add(new JLabel("Screen:"));
        form.add(screenField);
        form.add(new JLabel("Date (yyyy-MM-dd):"));
        form.add(dateField);
        form.add(new JLabel("Time (HH:mm):"));
        form.add(timeField);
        form.add(new JLabel("Base price (Rs):"));
        form.add(priceField);

        JButton addButton = new JButton("Add Show");
        JButton deleteButton = new JButton("Delete Show");
        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 6));
        buttons.add(addButton);
        buttons.add(deleteButton);

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        left.add(form, BorderLayout.NORTH);
        left.add(buttons, BorderLayout.CENTER);

        showTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addShow();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteShow();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(left, BorderLayout.WEST);
        panel.add(new JScrollPane(showTable), BorderLayout.CENTER);
        return panel;
    }

    private void addShow() {
        try {
            Movie movie = (Movie) movieCombo.getSelectedItem();              // casting from Object
            if (movie == null) {
                throw new InvalidInputException("Add a movie first.");
            }
            if (!Validator.allFilled(screenField.getText(), dateField.getText(), timeField.getText(),
                    priceField.getText())) {
                throw new InvalidInputException("Fill in all show fields.");
            }
            if (!Validator.isValidDate(dateField.getText().trim())) {
                throw new InvalidInputException("Date must be in the format yyyy-MM-dd.");
            }
            if (!Validator.isValidTime(timeField.getText().trim())) {
                throw new InvalidInputException("Time must be in the format HH:mm (24 hour).");
            }
            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
            } catch (NumberFormatException e) {
                throw new InvalidInputException("Price must be a number.");
            }
            if (price <= 0) {
                throw new InvalidInputException("Price must be greater than zero.");
            }
            showDao.insert(new Show(movie.getId(), screenField.getText().trim(), dateField.getText().trim(),
                    timeField.getText().trim(), price));
            screenField.setText("");
            dateField.setText("");
            timeField.setText("");
            priceField.setText("");
            loadShows();
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void deleteShow() {
        int row = showTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a show to delete.");
            return;
        }
        try {
            showDao.delete(shows.get(row).getId());
            loadShows();
        } catch (SQLException ex) {
            showError("Cannot delete this show because it already has bookings.\n(" + ex.getMessage() + ")");
        }
    }

    private void loadShows() {
        try {
            shows = showDao.findAll();
            Vector<Object[]> rows = new Vector<Object[]>();
            for (int i = 0; i < shows.size(); i++) {
                Show s = shows.get(i);
                rows.add(new Object[] {s.getId(), s.getMovieTitle(), s.getScreen(), s.getShowDate(),
                    s.getShowTime(), s.getBasePrice()});
            }
            showModel.setRows(rows);
        } catch (SQLException e) {
            showError("Could not load shows: " + e.getMessage());
        }
    }

    // ===================== BOOKINGS =====================
    private JPanel buildBookingsTab() {
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton refreshButton = new JButton("Refresh");
        JButton cancelButton = new JButton("Cancel Selected Booking");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBookings();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSelectedBooking();
            }
        });
        revenueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        bottom.add(refreshButton);
        bottom.add(cancelButton);
        bottom.add(revenueLabel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void cancelSelectedBooking() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking first.");
            return;
        }
        Booking booking = bookings.get(row);
        try {
            double refund = bookingService.cancelBooking(booking, booking.getUserId());
            JOptionPane.showMessageDialog(this, "Booking cancelled. Refund to customer: Rs " + refund);
            loadBookings();
        } catch (BookingException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void loadBookings() {
        try {
            bookings = bookingDao.findAll();
            Vector<Object[]> rows = new Vector<Object[]>();
            int confirmed = 0;
            for (int i = 0; i < bookings.size(); i++) {
                Booking b = bookings.get(i);
                if (b.isConfirmed()) {
                    confirmed++;
                }
                rows.add(new Object[] {b.getReference(), b.getCustomerName(), b.getShow().getMovieTitle(),
                    b.getShow().getShowDate(), b.getShow().getShowTime(), b.getSeats(), b.getTotal(),
                    b.getPaymentMethod(), b.getStatus()});
            }
            bookingModel.setRows(rows);
            revenueLabel.setText("Total bookings: " + bookings.size() + "  |  Confirmed: " + confirmed
                    + "  |  Revenue: Rs " + bookingDao.totalRevenue());
        } catch (SQLException e) {
            showError("Could not load bookings: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
