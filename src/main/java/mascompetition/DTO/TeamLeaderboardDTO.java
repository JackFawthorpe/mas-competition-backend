package mascompetition.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Representation of the data that is passed for leaderboard usage
 */
@Data
@Builder
public class TeamLeaderboardDTO {

    private UUID teamId;
    private String teamName;
    private UUID agentId;
    private Double agentRating;
    private String agentName;

    /**
     * Explicit Constructor to match the {@link jakarta.persistence.SqlResultSetMapping} within
     * {@link mascompetition.Entity.Team}. The ordering of parameters is too important to let
     * Lombok deal with it
     *
     * @param teamId      The id of the team
     * @param teamName    The name of the team
     * @param agentId     The ID of the highest rated agent in the team
     * @param agentRating The highest rating of the agents from the team
     * @param agentName   The formatted name of the agent "{agentName} v{verisonNumber}"
     */
    public TeamLeaderboardDTO(UUID teamId, String teamName, UUID agentId, Double agentRating, String agentName) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.agentId = agentId;
        this.agentRating = agentRating;
        this.agentName = agentName;
    }
}
