package mascompetition.Entity;

/**
 * Enumeration of the different states an agent can be in during the competition
 */
public enum AgentStatus {
    AVAILABLE, // The agent is still viable and competing
    UNVALIDATED, // If the agents source code has not been validated
    ILLEGAL_MOVE, // At some point the agent made an illegal move
    TIMED_OUT, // The agent took too long for its turn
    INVALID_SUBMISSION, // Thrown if the ActionController Interface (within the engine) isn't correctly implemented
    OUT_OF_MEMORY, // The agent was detected using too much memory
    ILLEGAL_IMPORTS  // The compilation of the agent or the imports it used were invalid
}
