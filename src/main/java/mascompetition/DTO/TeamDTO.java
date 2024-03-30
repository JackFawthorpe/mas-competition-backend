package mascompetition.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Data transfer object for {@link mascompetition.Entity.Team}
 */
@Data
@Builder
@AllArgsConstructor
public class TeamDTO {

    private String name;

    private UUID id;

    private List<UUID> users;

}
