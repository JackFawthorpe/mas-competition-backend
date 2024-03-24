package mascompetition.API;

import mascompetition.Exception.BadInformationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * Contains functions that will be used in many controllers
 */
public class BaseController {

    /**
     * Validates that the provided information matches the DTO validation tags
     *
     * @param result The results of the validation
     * @throws BadInformationException Thrown when the provided information is invalid
     */
    public void validateEndpoint(BindingResult result) throws BadInformationException {
        if (result.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            for (FieldError error : result.getFieldErrors()) {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }
            throw new BadInformationException(errorMessage.toString());
        }
    }

}
