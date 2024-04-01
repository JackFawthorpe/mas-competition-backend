package mascompetition.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Data transfer object for creating an agent
 */
@Data
@Builder
@AllArgsConstructor
public class CreateAgentDTO {

    @NotNull(message = "designTime is a required field")
    public Integer designTime;

    @NotBlank(message = "name is a required field")
    @NotEmpty(message = "name is a required field")
    @NotNull(message = "name is a required field")
    @Size(min = 6, max = 64, message = "name must be between 6 and 64 characters")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "name can only contain letters")
    private String name;

    @NotNull(message = "versionNumber is a required field")
    private Integer versionNumber;

    @NotNull(message = "You must provide a list of emails from your team that authored the agent")
    private List<String> emails;

}
