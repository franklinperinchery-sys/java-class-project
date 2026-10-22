package movieticket;

import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import movieticket.ui.LoginFrame;
import movieticket.util.AppLog;
import movieticket.util.DatabaseConnection;

/**
 * Entry point.
 * Command line arguments:  --reset  (recreate the database with demo data)
 *                          --debug  (print constructor calling order and other log lines)
 */
public class Main {

    public static void main(String[] args) {
        boolean reset = false;
        for (int i = 0; i < args.length; i++) {
            if ("--reset".equals(args[i])) {
                reset = true;
            } else if ("--debug".equals(args[i])) {
                AppLog.setDebugEnabled(true);
            } else {
                System.out.println("Ignoring unknown argument: " + args[i]);
            }
        }

        try {
            DatabaseConnection database = DatabaseConnection.getInstance();
            if (reset) {
                database.resetDatabase();
                System.out.println("Database reset with demo data.");
            } else {
                database.initializeSchema();
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Cannot open the database:\n" + e.getMessage(),
                    "Startup error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
