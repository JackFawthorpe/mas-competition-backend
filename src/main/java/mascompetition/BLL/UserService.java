package mascompetition.BLL;

import jakarta.validation.constraints.NotNull;
import mascompetition.DTO.ChangePasswordDTO;
import mascompetition.DTO.UserLoginDTO;
import mascompetition.DTO.UserToTeamDTO;
import mascompetition.Entity.Team;
import mascompetition.Entity.User;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import mascompetition.Repository.TeamRepository;
import mascompetition.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Context that can be inherited by all controllers to provide information about the request
 */
@Service
public class UserService {

    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Gets the user that has made the request
     *
     * @return The user logged in or null if there is no user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AnonymousAuthenticationToken
                ? null
                : userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> {
                    logger.error("Unexpectedly failed to find user in repository {}", authentication.getName());
                    return new RuntimeException(String.format("Unexpectedly failed to find user in repository %s", authentication.getName()));
                });
    }

    /**
     * Creates a transaction to create the users that are given to persist
     *
     * @param users The list of users to create
     * @return The list of UUID's of the users created
     * @throws DataIntegrityViolationException thrown when database transaction is unsuccessful. This will primarily be duplicate email
     */
    @Transactional
    public List<UUID> batchCreateUsers(@NotNull List<UserLoginDTO> users) throws DataIntegrityViolationException {
        logger.info("Adding users");
        List<UUID> ids = new ArrayList<>();
        for (UserLoginDTO user : users) {
            ids.add(createUser(user));
        }
        return ids;
    }

    /**
     * Attempts to persist the User that is provided
     *
     * @param toCreate A validated DTO to persist
     * @return The UUID of the user created
     */
    private UUID createUser(@NotNull UserLoginDTO toCreate) {
        User createdUser = userRepository.save(
                User.builder()
                        .email(toCreate.getEmail())
                        .hashedPassword(passwordEncoder.encode(toCreate.getPassword()))
                        .build()
        );
        logger.info("Created new user {}", createdUser.getId());
        return createdUser.getId();
    }

    /**
     * Logic for changing the password for a user
     *
     * @param changePasswordDTO The new password information
     * @param uuid              The ID of the user to change
     * @throws EntityNotFoundException Thrown if the given UUID isn't in the database
     * @throws BadInformationException Thrown if the currentPassword is wrong or the new password doesn't match the current
     */
    public void changePassword(@NotNull ChangePasswordDTO changePasswordDTO, @NotNull UUID uuid) throws EntityNotFoundException, BadInformationException {
        User user = userRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("The user supplied doesn't exist"));
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getHashedPassword())) {
            throw new BadInformationException("Invalid current password");
        }

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BadInformationException("Confirmation Password doesn't match New Password");
        }

        if (changePasswordDTO.getNewPassword().equals(changePasswordDTO.getCurrentPassword())) {
            throw new BadInformationException("The new password cannot match the old one");
        }

        user.setHashedPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }


    /**
     * Transactional wrapper around batch adding users to teams
     *
     * @param teamUserMapping The list of users and the teams they will join
     * @throws EntityNotFoundException Thrown if one of the entities provided doesn't exist
     */
    @Transactional
    public void batchAddUsersToTeams(List<UserToTeamDTO> teamUserMapping) throws EntityNotFoundException {
        logger.info("Adding users to teams");
        for (UserToTeamDTO dto : teamUserMapping) {
            addUser(dto.getTeamId(), dto.getUserEmail());
        }
    }

    /**
     * Responsible for adding users to teams
     *
     * @param teamID The ID of the team to add the user to
     * @param email  The email of the user
     * @throws EntityNotFoundException Thrown if the user or the team doesnt exist
     */
    private void addUser(@NotNull UUID teamID, @NotNull String email) throws EntityNotFoundException {
        Team team = teamRepository.findById(teamID)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Team %s doesn't exist", teamID)));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(String.format("User with email %s doesn't exist", email)));

        logger.info("Adding {} to team {}", email, teamID);
        user.setTeam(team);
        userRepository.save(user);
    }
}
