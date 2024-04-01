package mascompetition.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull
    public Integer designTime;

    @NotBlank
    @NotEmpty
    @NotNull
    @Size(min = 6, max = 64, message = "Agent name must be between 6 and 64 characters")
    private String name;

    @NotNull
    private Integer versionNumber;

    @Valid
    private List<String> emails;


}
