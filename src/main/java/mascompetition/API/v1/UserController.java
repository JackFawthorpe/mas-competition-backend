package mascompetition.API.v1;

import jakarta.validation.Valid;
import mascompetition.API.BaseController;
import mascompetition.BLL.UserService;
import mascompetition.DTO.ChangePasswordDTO;
import mascompetition.Entity.User;
import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller to handle the user-based endpoints
 */
@RestController
@RequestMapping("/api/v1/")
public class UserController extends BaseController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    /**
     * Endpoint for changing password
     *
     * @param userId        The id of the password to change
     * @param bindingResult The results of the validation against the {@link mascompetition.DTO.ChangePasswordDTO}
     * @return 200 if the password is successfully changed
     * @throws BadInformationException  Thrown if any of the passwords provided don't match password requirements
     * @throws ActionForbiddenException Thrown if attempting to change someone else's password
     */
    @PostMapping("/users/{user_id}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable(value = "user_id") UUID userId,
            @RequestBody @Valid ChangePasswordDTO changePasswordDTO, BindingResult bindingResult) throws BadInformationException, ActionForbiddenException, EntityNotFoundException {
        User user = userService.getCurrentUser();
        logger.info("POST /api/v1/users/{} by user {}", userId, user.getId());

        validateEndpoint(bindingResult);

        if (!userId.equals(user.getId())) {
            throw new ActionForbiddenException("You cannot change someone else's password");
        }

        userService.changePassword(changePasswordDTO, user.getId());

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                user.getHashedPassword(),
                                user.getAuthorities()));

        return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully");
    }
}
