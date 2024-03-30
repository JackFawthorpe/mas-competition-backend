package mascompetition.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for {@link mascompetition.API.v1.UserController#changePassword}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {

    @NotEmpty(message = "newPassword is a required field")
    @Size(min = 8, max = 20, message = "newPassword must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).*$", message = "newPassword must contain at least one uppercase letter and one number")
    private String currentPassword;

    @NotEmpty(message = "newPassword is a required field")
    @Size(min = 8, max = 20, message = "newPassword must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).*$", message = "newPassword must contain at least one uppercase letter and one number")
    private String newPassword;

    @NotEmpty(message = "confirmPassword is a required field")
    @Size(min = 8, max = 20, message = "confirmPassword must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).*$", message = "confirmPassword must contain at least one uppercase letter and one number")
    private String confirmPassword;

}
