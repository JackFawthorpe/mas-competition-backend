package mascompetition.Exception;

/**
 * Thrown if the submitted agent has an illegal import {@link mascompetition.BLL.AgentParser#validateImports}
 */
public class IllegalImportException extends Exception {
    public IllegalImportException(String message) {
        super(message);
    }
}