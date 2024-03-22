package mascompetition.Exception;

/**
 * Error for when a user provides invalid information
 */
public class BadInformationException extends Exception {
    public BadInformationException(String message) {
        super(message);
    }
}