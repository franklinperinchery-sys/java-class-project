package movieticket.util;

/** Tiny logger. Run the program with the --debug command line argument to see the messages. */
public final class AppLog {
    private static boolean debugEnabled = false;

    private AppLog() {
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static void log(String message) {
        if (debugEnabled) {
            System.out.println("[LOG] " + message);
        }
    }
}
