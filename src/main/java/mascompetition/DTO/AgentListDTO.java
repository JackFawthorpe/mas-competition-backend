package mascompetition.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Data transfer object for the display of agents on the leaderboard
 */
@Data
@Builder
@AllArgsConstructor
public class AgentListDTO {

    public Integer rating;
    private String agentName;
    private String teamName;
    private UUID agentId;
    private UUID teamId;

}
