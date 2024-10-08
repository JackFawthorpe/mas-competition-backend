package mascompetition.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for logging in
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    @Email(message = "Please provide a valid email address")
    @NotEmpty(message = "Email is a required field")
    private String email;

    @NotEmpty(message = "Password is a required field")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).*$", message = "Password must contain at least one uppercase letter and one number")
    private String password;

}