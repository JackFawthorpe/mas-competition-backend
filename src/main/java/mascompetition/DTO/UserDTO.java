package mascompetition.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Data transfer object for {@link mascompetition.Entity.User}
 */
@Data
@Builder
@AllArgsConstructor
public class UserDTO {

    private UUID id;

    private String email;

}
