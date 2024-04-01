package mascompetition.Exception;

/**
 * Error that is thrown when something goes wrong in the storage of the agent
 */
public class AgentStorageException extends Exception {
    public AgentStorageException(String message) {
        super(message);
    }
}
