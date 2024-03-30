package mascompetition.Exception;

/**
 * Error for when a user attempts to complete an action they don't have permission for
 */
public class ActionForbiddenException extends Exception {
    public ActionForbiddenException(String message) {
        super(message);
    }
}
