package mascompetition.Exception;

/**
 * Exception to be thrown when the engine fails
 */
public class EngineFailureException extends Exception {

    private final int exitCode;

    public EngineFailureException(int exitCode) {
        super("Engine failed to complete game");
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}