package movieticket.util;

/** Static input validation helpers used by every form. */
public final class Validator {
    private Validator() {
    }

    public static boolean isNotBlank(String text) {
        return text != null && text.trim().length() > 0;
    }

    /** Variable length arguments: true only if every field has some text. */
    public static boolean allFilled(String... fields) {
        for (int i = 0; i < fields.length; i++) {
            if (!isNotBlank(fields[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean allDigits(String text) {
        if (text == null || text.length() == 0) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidEmail(String email) {
        if (!isNotBlank(email)) {
            return false;
        }
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1 && email.indexOf(' ') < 0;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.length() == 10 && allDigits(phone);
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /** Date format yyyy-MM-dd. */
    public static boolean isValidDate(String date) {
        if (date == null || date.length() != 10 || date.charAt(4) != '-' || date.charAt(7) != '-') {
            return false;
        }
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8);
        if (!allDigits(year) || !allDigits(month) || !allDigits(day)) {
            return false;
        }
        int m = Integer.parseInt(month);
        int d = Integer.parseInt(day);
        return m >= 1 && m <= 12 && d >= 1 && d <= 31;
    }

    /** Time format HH:mm (24 hour). */
    public static boolean isValidTime(String time) {
        if (time == null || time.length() != 5 || time.charAt(2) != ':') {
            return false;
        }
        String hour = time.substring(0, 2);
        String minute = time.substring(3);
        if (!allDigits(hour) || !allDigits(minute)) {
            return false;
        }
        return Integer.parseInt(hour) < 24 && Integer.parseInt(minute) < 60;
    }
}
