package mascompetition.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Data transfer object for assigning users to teams {@link mascompetition.API.v1.AdminController#addUsersToTeams}
 */
@Data
@Builder
@AllArgsConstructor
public class UserToTeamDTO {

    @NotBlank(message = "Missing userEmail")
    @NotEmpty(message = "Missing userEmail")
    @Email(message = "userEmail must be a valid email")
    String userEmail;

    @NotNull(message = "Missing teamId")
    UUID teamId;
}
