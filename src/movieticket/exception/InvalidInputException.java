package movieticket.exception;

/** Custom UNCHECKED exception: thrown when user input fails validation. */
public class InvalidInputException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
