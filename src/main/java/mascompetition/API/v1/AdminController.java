package mascompetition.API.v1;

import jakarta.validation.Valid;
import mascompetition.API.BaseController;
import mascompetition.BLL.TeamService;
import mascompetition.BLL.UserService;
import mascompetition.DTO.CreateTeamDTO;
import mascompetition.DTO.TeamDTO;
import mascompetition.DTO.UserLoginDTO;
import mascompetition.Exception.BadInformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller to handle the administrative endpoints
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController extends BaseController {

    Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    UserService userService;

    @Autowired
    TeamService teamService;

    /**
     * Endpoint for adding users to the database
     *
     * @param users         The users to add to the database
     * @param bindingResult The results of the validation against the UserDTO's
     * @return 201 if the users are created successfully otherwise handled by errorMapper
     * @throws BadInformationException Thrown if the emails or passwords are invalid
     */
    @PostMapping("/users")
    public ResponseEntity<List<UUID>> addUsers(@RequestBody @Valid List<UserLoginDTO> users, BindingResult bindingResult) throws BadInformationException {
        logger.info("POST /api/v1/admin/users by user {}", userService.getCurrentUser().getId());

        validateEndpoint(bindingResult);

        try {
            List<UUID> userIds = userService.createUsers(users);
            return ResponseEntity.status(HttpStatus.CREATED).body(userIds);
        } catch (DataIntegrityViolationException e) {
            logger.info("Duplicate email present, rolling back user creation");
            throw new BadInformationException("Duplicate email detected");
        }
    }

    /**
     * Endpoint for adding teams to the database
     *
     * @param team          The team to add to the database
     * @param bindingResult The results of the validation against the CreateTeamDTO's
     * @return 201 if the team is created successfully, otherwise the errorMapper handles the error code
     * @throws BadInformationException Thrown if an invalid name is provided
     */
    @PostMapping(value = "/teams")
    public ResponseEntity<TeamDTO> createTeam(@RequestBody @Valid CreateTeamDTO team, BindingResult bindingResult) throws BadInformationException {
        logger.info("POST /api/v1/admin/teams by user {}", userService.getCurrentUser().getId());

        validateEndpoint(bindingResult);

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(team));
        } catch (DataIntegrityViolationException e) {
            logger.info("Duplicate team name, team creation discarded");
            throw new BadInformationException("Duplicate team name");
        }
    }

}
