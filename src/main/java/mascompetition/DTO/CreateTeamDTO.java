package mascompetition.DTO;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Data transfer object for {@link mascompetition.Entity.Team}
 */
@Data
@Builder
@AllArgsConstructor
public class CreateTeamDTO {

    @NotEmpty(message = "Password is a required field")
    @Size(min = 6, max = 20, message = "Team name must be between 8 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Team name can only contain alphanumerics and spaces")
    private String name;

}
