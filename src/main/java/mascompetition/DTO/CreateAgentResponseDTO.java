package mascompetition.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Data transfer object for the data sent back after creating an agent
 */
@Data
@Builder
@AllArgsConstructor
public class CreateAgentResponseDTO {

    private ZonedDateTime nextRound;

    private UUID agentID;
}
