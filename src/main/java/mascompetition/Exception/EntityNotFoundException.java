package mascompetition.Exception;

/**
 * Error for when an action is performed against a entity that doesn't exist
 */
public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
