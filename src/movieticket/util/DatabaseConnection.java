package movieticket.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SINGLETON design pattern: only one DatabaseConnection object exists in the whole program,
 * created lazily by getInstance(). It owns the one JDBC Connection to the SQLite file.
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:movieticket.db";
    private static DatabaseConnection instance;      // the single instance (static member)

    private Connection connection;

    private DatabaseConnection() {                   // private constructor: nobody else can create one
        AppLog.log("DatabaseConnection singleton created");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** JDBC steps: load driver -> open connection -> (statements are created by the DAOs). */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found. Copy sqlite-jdbc-x.y.z.jar into the lib folder.", e);
            }
            connection = DriverManager.getConnection(DB_URL);
            Statement statement = null;
            try {
                statement = connection.createStatement();
                statement.execute("PRAGMA foreign_keys = ON");
            } finally {
                DbUtil.close(statement);
            }
        }
        return connection;
    }

    /** Creates the tables if they do not exist and inserts demo data the first time. */
    public void initializeSchema() throws SQLException {
        Connection con = getConnection();
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = con.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, "
                    + "password_hash TEXT NOT NULL, full_name TEXT NOT NULL, email TEXT, phone TEXT, "
                    + "role TEXT NOT NULL CHECK (role IN ('ADMIN','CUSTOMER')))");
            statement.execute("CREATE TABLE IF NOT EXISTS movies ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, genre TEXT, language TEXT, "
                    + "duration_min INTEGER NOT NULL, rating REAL DEFAULT 0)");
            statement.execute("CREATE TABLE IF NOT EXISTS shows ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE, "
                    + "screen TEXT NOT NULL, show_date TEXT NOT NULL, show_time TEXT NOT NULL, price REAL NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS bookings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, reference TEXT NOT NULL UNIQUE, "
                    + "user_id INTEGER NOT NULL REFERENCES users(id), "
                    + "show_id INTEGER NOT NULL REFERENCES shows(id), "
                    + "seats TEXT NOT NULL, total REAL NOT NULL, payment_method TEXT NOT NULL, "
                    + "status TEXT NOT NULL, booked_at TEXT DEFAULT CURRENT_TIMESTAMP)");
            // UNIQUE(show_id, seat_label) makes double booking impossible, even with two users at once.
            statement.execute("CREATE TABLE IF NOT EXISTS booked_seats ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "booking_id INTEGER NOT NULL REFERENCES bookings(id) ON DELETE CASCADE, "
                    + "show_id INTEGER NOT NULL, seat_label TEXT NOT NULL, UNIQUE (show_id, seat_label))");

            rs = statement.executeQuery("SELECT COUNT(*) FROM users");
            boolean empty = rs.next() && rs.getInt(1) == 0;
            if (empty) {
                seedData(con, statement);
            }
        } finally {
            DbUtil.close(rs, statement);
        }
    }

    /** Deletes everything and recreates the demo data (command line argument --reset). */
    public void resetDatabase() throws SQLException {
        Connection con = getConnection();
        Statement statement = null;
        try {
            statement = con.createStatement();
            statement.execute("DROP TABLE IF EXISTS booked_seats");
            statement.execute("DROP TABLE IF EXISTS bookings");
            statement.execute("DROP TABLE IF EXISTS shows");
            statement.execute("DROP TABLE IF EXISTS movies");
            statement.execute("DROP TABLE IF EXISTS users");
        } finally {
            DbUtil.close(statement);
        }
        initializeSchema();
    }

    private void seedData(Connection con, Statement statement) throws SQLException {
        PreparedStatement userInsert = null;
        try {
            userInsert = con.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, email, phone, role) VALUES (?,?,?,?,?,?)");
            String[][] users = {
                {"admin", "admin123", "System Administrator", "admin@cinema.com", "9000000001", "ADMIN"},
                {"demo", "demo123", "Demo Customer", "demo@mail.com", "9000000002", "CUSTOMER"}
            };
            for (int i = 0; i < users.length; i++) {
                userInsert.setString(1, users[i][0]);
                userInsert.setString(2, PasswordUtil.hash(users[i][1]));
                userInsert.setString(3, users[i][2]);
                userInsert.setString(4, users[i][3]);
                userInsert.setString(5, users[i][4]);
                userInsert.setString(6, users[i][5]);
                userInsert.executeUpdate();
            }
        } finally {
            DbUtil.close(userInsert);
        }

        statement.executeUpdate("INSERT INTO movies (title, genre, language, duration_min, rating) VALUES "
                + "('Drishyam','Thriller','Malayalam',160,8.2),"
                + "('Premam','Romance','Malayalam',156,8.0),"
                + "('Inception','Sci-Fi','English',148,8.8),"
                + "('Interstellar','Sci-Fi','English',169,8.7),"
                + "('Dangal','Drama','Hindi',161,8.3)");

        // movie id, screen, days from today, time, base price
        int[] movieIds = {1, 1, 2, 3, 3, 4, 5};
        String[] screens = {"Screen 1", "Screen 2", "Screen 1", "Screen 3", "Screen 2", "Screen 3", "Screen 1"};
        int[] dayOffsets = {1, 2, 1, 1, 3, 2, 3};
        String[] times = {"10:30", "18:00", "14:00", "21:15", "11:00", "17:30", "20:00"};
        double[] prices = {150, 180, 150, 220, 200, 220, 160};
        for (int i = 0; i < movieIds.length; i++) {
            statement.executeUpdate("INSERT INTO shows (movie_id, screen, show_date, show_time, price) VALUES ("
                    + movieIds[i] + ",'" + screens[i] + "', date('now','+" + dayOffsets[i] + " day'),'"
                    + times[i] + "'," + prices[i] + ")");
        }
        AppLog.log("Demo data inserted");
    }
}
