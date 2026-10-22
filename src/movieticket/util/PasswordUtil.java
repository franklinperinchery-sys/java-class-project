package movieticket.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Hashes passwords with SHA-256 so plain text passwords are never stored in the database. */
public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                int unsigned = bytes[i] & 0xff;          // bitwise AND removes the sign
                if (unsigned < 16) {
                    hex.append('0');
                }
                hex.append(Integer.toHexString(unsigned));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
