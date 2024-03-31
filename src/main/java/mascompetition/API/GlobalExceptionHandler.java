package mascompetition.API;

import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Maps exceptions that can be thrown within the application to Status codes
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * {@link BadInformationException} is thrown when data given doesn't match required validations
     *
     * @param ex The error thrown
     * @return 401 error
     */
    @ExceptionHandler(BadInformationException.class)
    public ResponseEntity<String> handleBadInformationException(BadInformationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Invalid Data Provided: %s", ex.getMessage()));
    }

    @ExceptionHandler(ActionForbiddenException.class)
    public ResponseEntity<String> handleActionForbiddenException(ActionForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(String.format("Permission denied: %s", ex.getMessage()));
    }


    /**
     * Exception handling for when the provided HTTP message doesn't reach Controller
     *
     * @param ex The exception
     * @return 401 - Remove information about what went wrong and display generic 'doesnt match API'
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleBadHttpMessage(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request does not match API requirements");
    }

    /**
     * Generic 404 Exception Handler
     *
     * @param ex The error thrown
     * @return 404 error
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleMissingResource(Exception ex) {
        logger.info("Missing Resource Exception {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("API endpoint not found");
    }

    /**
     * {@link EntityNotFoundException} is caused when a findByX doesn't find the entity unexpectedly
     *
     * @param ex The error thrown
     * @return 404 error
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Resource not found: %s", ex.getMessage()));
    }

    /**
     * {@link Exception} should never be caught here as specific mappers should catch this instead
     *
     * @param ex The error thrown
     * @return 500 error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedException(Exception ex) {
        logger.error("Unhandled error caught {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Generic Error Thrown");
    }

}
