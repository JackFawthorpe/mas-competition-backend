package mascompetition.Exception;

/**
 * An exception to be thrown when an error is detected with the agent during parsing from {@link mascompetition.BLL.AgentParser}
 */
public class AgentParseException extends Exception {
    public AgentParseException(String message) {
        super(message);
    }
}