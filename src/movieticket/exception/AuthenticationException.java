package movieticket.exception;

/** Custom checked exception: wrong credentials or duplicate username. */
public class AuthenticationException extends Exception {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
