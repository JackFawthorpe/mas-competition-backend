package mascompetition.Exception;

/**
 * Exception to be thrown when there is an issue loading the player from storage into game context
 */
public class LoadAgentException extends Exception {
    public LoadAgentException(String message) {
        super(message);
    }
}