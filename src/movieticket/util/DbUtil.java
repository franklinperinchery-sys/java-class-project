package movieticket.util;

/** JDBC helper that closes any number of resources (variable length arguments). */
public final class DbUtil {
    private DbUtil() {
    }

    public static void close(AutoCloseable... resources) {
        for (int i = 0; i < resources.length; i++) {
            if (resources[i] != null) {
                try {
                    resources[i].close();
                } catch (Exception e) {
                    AppLog.log("Could not close resource: " + e.getMessage());
                }
            }
        }
    }
}
