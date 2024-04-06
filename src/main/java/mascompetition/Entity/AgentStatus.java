package mascompetition.Entity;

/**
 * Enumeration of the different states an agent can be in during the competition
 */
public enum AgentStatus {
    AVAILABLE, // The agent is still viable and competing
    ILLEGAL_MOVE, // At some point the agent made an illegal move
    TIMED_OUT, // The agent took too long for its turn
    INVALID_SUBMISSION, // The compilation of the agent or the imports it used were invalid
}
